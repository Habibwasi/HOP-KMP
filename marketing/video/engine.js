/*
 * Deterministic timeline engine.
 *
 * Every visual property is a pure function of the global time T (seconds), so the
 * renderer can seek to any frame and capture it. Times in data-* attributes are
 * relative to the nearest time context: a .scene (data-start) or any element with
 * data-from (screens inside a phone, or .ctx groups).
 *
 *   data-from / data-to / data-enter / data-exit   screen visibility (+ push|up|fade|none)
 *   data-in="t[,fx]"        fade/slide in (fx: up|down|left|right|scale|pop|fade)
 *   data-out="t"            fade out
 *   data-on="t1[,t2]"       toggles class "on" inside [t1, t2)
 *   data-vis="t1[,t2]"      display only inside [t1, t2)
 *   data-type="t,dur"       types data-text (placeholder: data-ph)
 *   data-count="t,dur,from,to[,dec]"   counts a number, Danish formatting
 *   data-tap="t1 t2 ..."    touch ripple
 *   data-tx / ty / sc / rot / op = "t:v t:v ..."   keyframed transform/opacity
 *   data-draw="t,dur"       SVG stroke draw (element needs pathLength="1")
 *   data-bar="t,dur,from,to"  width in %
 *   data-scroll="t:y t:y"   scroll a content column
 */
const Engine = (() => {
  const FPS = 30;
  const scenes = [];
  let tracked = [];
  let sceneEls = [];
  let duration = 0;

  const clamp = (x, a = 0, b = 1) => Math.min(b, Math.max(a, x));
  const easeOut = p => 1 - Math.pow(1 - p, 3);
  const easeInOut = p => (p < 0.5 ? 4 * p * p * p : 1 - Math.pow(-2 * p + 2, 3) / 2);
  const easeBack = p => { const c1 = 1.70158, c3 = c1 + 1; return 1 + c3 * Math.pow(p - 1, 3) + c1 * Math.pow(p - 1, 2); };

  function dk(n, dec = 0) {
    const neg = n < 0; n = Math.abs(n);
    let [i, f] = n.toFixed(dec).split('.');
    i = i.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return (neg ? '-' : '') + i + (f ? ',' + f : '');
  }

  function keyframes(spec) {
    return spec.trim().split(/\s+/).map(s => s.split(':').map(Number));
  }
  function sampleKf(kf, t) {
    if (t <= kf[0][0]) return kf[0][1];
    for (let i = 1; i < kf.length; i++) {
      if (t <= kf[i][0]) {
        const [t0, v0] = kf[i - 1], [t1, v1] = kf[i];
        return v0 + (v1 - v0) * easeInOut((t - t0) / (t1 - t0));
      }
    }
    return kf[kf.length - 1][1];
  }

  // "a,b c" → [[a,b],[c,∞]]
  function ranges(spec) {
    return spec.trim().split(/\s+/).map(r => { const [a, b] = r.split(',').map(Number); return [a, b === undefined || isNaN(b) ? 1e9 : b]; });
  }
  const inRanges = (rs, t) => rs.some(([a, b]) => t >= a && t < b);

  function scene(def) { scenes.push(def); }

  function contextStart(el) {
    let s = 0, p = el.parentElement;
    while (p) {
      if (p.classList.contains('scene')) { s += +p.dataset.start; break; }
      if (p.dataset.from !== undefined) s += +p.dataset.from;
      p = p.parentElement;
    }
    return s;
  }

  const ATTRS = ['from', 'in', 'out', 'on', 'vis', 'type', 'count', 'tap', 'tx', 'ty', 'sc', 'rot', 'op', 'draw', 'bar', 'scroll'];

  function build() {
    const root = document.getElementById('scenes');
    scenes.forEach((s, idx) => {
      const el = document.createElement('section');
      el.className = 'scene';
      el.dataset.start = s.start;
      el.dataset.end = s.end;
      el.innerHTML = s.html;
      root.appendChild(el);
      s.el = el;
      s.idx = idx;
      duration = Math.max(duration, s.end);
    });
    sceneEls = scenes.map(s => s.el);

    tracked = [];
    scenes.forEach(s => {
      const list = [];
      s.el.querySelectorAll('*').forEach(el => {
        const d = el.dataset;
        if (!ATTRS.some(a => d[a] !== undefined)) return;
        const item = { el, base: contextStart(el), d: {} };
        if (d.from !== undefined && el.classList.contains('scr')) item.d.from = [+d.from, d.to !== undefined ? +d.to : 1e9, d.enter || 'push', d.exit || ''];
        if (d.in !== undefined) { const [t, fx] = d.in.split(','); item.d.in = [+t, fx || 'up']; }
        if (d.out !== undefined) item.d.out = +d.out;
        if (d.on !== undefined) item.d.on = ranges(d.on);
        if (d.vis !== undefined) item.d.vis = ranges(d.vis);
        if (d.type !== undefined) { const [a, b] = d.type.split(',').map(Number); item.d.type = [a, b || 0.8, d.text || '', d.ph || '']; }
        if (d.count !== undefined) item.d.count = d.count.split(',').map(Number);
        if (d.tap !== undefined) item.d.tap = d.tap.trim().split(/\s+/).map(Number);
        for (const k of ['tx', 'ty', 'sc', 'rot', 'op', 'scroll']) if (d[k] !== undefined) item.d[k] = keyframes(d[k]);
        if (d.draw !== undefined) item.d.draw = d.draw.split(',').map(Number);
        if (d.bar !== undefined) item.d.bar = d.bar.split(',').map(Number);
        if (item.d.tap && getComputedStyle(el).position === 'static') el.style.position = 'relative';
        item.last = {};
        list.push(item);
      });
      s.tracked = list;
    });

    // Progress route chapter dots
    const P = window.PROGRESS;
    const dots = document.querySelector('#progress .dots');
    scenes.forEach(s => {
      if (s.start < P.from || s.start >= P.to) return;
      const i = document.createElement('i');
      i.style.left = ((s.start - P.from) / (P.to - P.from) * 100) + '%';
      i.dataset.at = s.start;
      dots.appendChild(i);
    });
  }

  function set(item, key, value, apply) {
    if (item.last[key] !== value) { item.last[key] = value; apply(value); }
  }

  function applyItem(item, T) {
    const el = item.el, d = item.d, lt = T - item.base;
    let opacity = 1, tx = 0, ty = 0, sc = 1, rot = 0, txPct = 0, tyPct = 0, display = '';

    if (d.from) {
      const [from, to, enter, exit] = d.from;
      const vis = lt >= from && lt < to + 0.5;
      display = vis ? 'block' : 'none';
      if (vis) {
        const p = easeOut(clamp((lt - from) / 0.45));
        if (enter === 'push') txPct = (1 - p) * 100;
        else if (enter === 'up') tyPct = (1 - p) * 100;
        else if (enter === 'fade') opacity *= p;
        if (exit === 'left' && lt > to) txPct = -30 * easeOut(clamp((lt - to) / 0.45));
        if (exit === 'down' && lt > to) tyPct = 100 * easeOut(clamp((lt - to) / 0.45));
      }
    }
    if (d.vis) display = inRanges(d.vis, lt) ? '' : 'none';
    if (d.in) {
      const [t, fx] = d.in;
      const raw = clamp((lt - t) / 0.5);
      const p = easeOut(raw);
      opacity *= fx === 'pop' ? clamp(raw * 3) : p;
      if (fx === 'up') ty += (1 - p) * 26;
      else if (fx === 'down') ty -= (1 - p) * 26;
      else if (fx === 'left') tx += (1 - p) * 60;
      else if (fx === 'right') tx -= (1 - p) * 60;
      else if (fx === 'scale') sc *= 0.86 + 0.14 * p;
      else if (fx === 'pop') sc *= raw >= 1 ? 1 : 0.4 + 0.6 * easeBack(raw);
    }
    if (d.out !== undefined) opacity *= 1 - clamp((lt - d.out) / 0.35);
    if (d.op) opacity *= sampleKf(d.op, lt);
    if (d.tx) tx += sampleKf(d.tx, lt);
    if (d.ty) ty += sampleKf(d.ty, lt);
    if (d.sc) sc *= sampleKf(d.sc, lt);
    if (d.rot) rot += sampleKf(d.rot, lt);
    if (d.scroll) ty -= sampleKf(d.scroll, lt);

    if (d.on) set(item, 'on', inRanges(d.on, lt), v => el.classList.toggle('on', v));
    if (d.type) {
      const [t, dur, text, ph] = d.type;
      const p = clamp((lt - t) / dur);
      const n = Math.round(p * text.length);
      const typing = lt >= t - 0.25 && p < 1;
      const html = n === 0 && ph ? `<span class="ph">${ph}</span>` : text.slice(0, n).replace(/&/g, '&amp;').replace(/</g, '&lt;');
      set(item, 'type', html + '|' + typing, () => { el.innerHTML = html; el.classList.toggle('typing', typing); });
    }
    if (d.count) {
      const [t, dur, a, b, dec] = d.count;
      const v = a + (b - a) * easeOut(clamp((lt - t) / dur));
      set(item, 'count', dk(v, dec || 0), v => { el.textContent = v; });
    }
    if (d.tap) {
      let tp = -1;
      for (const t of d.tap) if (lt >= t && lt < t + 0.6) tp = (lt - t) / 0.6;
      set(item, 'tap', tp >= 0, v => el.classList.toggle('tapping', v));
      if (tp >= 0) { el.style.setProperty('--tp', tp.toFixed(3)); if (tp < 0.4) sc *= 1 - 0.035 * Math.sin(Math.PI * tp / 0.4); }
    }
    if (d.draw) {
      const p = easeInOut(clamp((lt - d.draw[0]) / d.draw[1]));
      set(item, 'draw', p.toFixed(4), v => { el.style.strokeDasharray = '1 1'; el.style.strokeDashoffset = (1 - v).toFixed(4); });
    }
    if (d.bar) {
      const [t, dur, a, b] = d.bar;
      const v = a + (b - a) * easeOut(clamp((lt - t) / dur));
      set(item, 'bar', v.toFixed(2), v => { el.style.width = v + '%'; });
    }

    const tf = (txPct || tyPct ? `translate(${txPct}%, ${tyPct}%) ` : '') +
      (tx || ty ? `translate(${tx.toFixed(2)}px, ${ty.toFixed(2)}px) ` : '') +
      (sc !== 1 ? `scale(${sc.toFixed(4)}) ` : '') + (rot ? `rotate(${rot.toFixed(2)}deg)` : '');
    set(item, 'tf', tf, v => { el.style.transform = v; });
    set(item, 'op', opacity.toFixed(3), v => { el.style.opacity = v; });
    if (d.from || d.vis) set(item, 'display', display, v => { el.style.display = v; });
  }

  let currentT = 0;
  function seek(T) {
    currentT = T;
    scenes.forEach((s, i) => {
      const first = i === 0;
      const visible = T >= s.start - (first ? 1 : 0) && T < s.end + 0.45;
      s.el.style.display = visible ? 'block' : 'none';
      if (!visible) return;
      let op = first ? 1 : clamp((T - s.start - 0.15) / 0.45);
      if (T > s.end) op *= 1 - clamp((T - s.end) / 0.4);
      s.el.style.opacity = op.toFixed(3);
      for (const item of s.tracked) applyItem(item, T);
      if (s.frame) s.frame(T - s.start, s.el);
    });

    // Progress route
    const P = window.PROGRESS;
    const prog = document.getElementById('progress');
    const pv = clamp((T - P.from) / (P.to - P.from));
    const pop = clamp((T - P.from) / 0.6) * (1 - clamp((T - P.to + 0.2) / 0.6));
    prog.style.opacity = pop.toFixed(3);
    prog.querySelector('.fill').style.width = (pv * 100) + '%';
    prog.querySelector('.head').style.left = (pv * 100) + '%';
    prog.querySelectorAll('.dots i').forEach(i => i.classList.toggle('on', T >= +i.dataset.at));

    // CSS keyframe animations inside illustrations follow the global clock
    document.getAnimations().forEach(a => { a.pause(); a.currentTime = T * 1000; });

    const hud = document.getElementById('hud');
    if (hud) hud.textContent = T.toFixed(2) + 's';
  }

  async function init() {
    build();
    await document.fonts.ready;
    await Promise.all([...document.fonts].map(f => f.load().catch(() => {})));
    window.DURATION = duration;
    window.FPS = FPS;
    window.seek = seek;
    window.videoReady = true;

    const render = location.search.includes('render');
    if (render) { document.getElementById('hud').remove(); seek(0); return; }

    // Preview: scale to window, autoplay, space = pause, arrows = ±5 s, #t=NN to start at NN s
    const stage = document.getElementById('stage');
    const fit = () => {
      const s = Math.min(innerWidth / 1920, innerHeight / 1080);
      stage.style.transform = `scale(${s})`; stage.style.transformOrigin = '0 0';
    };
    fit(); addEventListener('resize', fit);
    let t = parseFloat((location.hash.match(/t=([\d.]+)/) || [])[1] || 0), playing = true, last = performance.now();
    addEventListener('keydown', e => {
      if (e.code === 'Space') playing = !playing;
      if (e.code === 'ArrowRight') t += 5;
      if (e.code === 'ArrowLeft') t = Math.max(0, t - 5);
    });
    const loop = now => {
      if (playing) t += (now - last) / 1000;
      last = now;
      if (t > duration) t = 0;
      seek(t);
      requestAnimationFrame(loop);
    };
    requestAnimationFrame(loop);
  }

  return { scene, init, seek, dk, clamp, easeOut, easeInOut };
})();
