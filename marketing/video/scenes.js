/*
 * Ridly — "Every seat moves us forward". Scene timings follow MOTION_VIDEO_STORY.md.
 * All times inside a scene are seconds from the scene start; inside a .scr they are
 * seconds from that screen's data-from.
 */
window.PROGRESS = { from: 9, to: 172 };
const { ic, star, stars, appbar, nav, logo, toggle, av, field, stepper, phone, scr, copy, tripCard, scrim } = UI;

// ── shared bits ────────────────────────────────────────────────────────────────
const sw = (t, off, on, cls = '') => `<span class="swap ${cls}" data-on="${t}"><span class="when-off">${off}</span><span class="when-on">${on}</span></span>`;
const PX = 1170, PY = 104;                       // single-phone position
const DUO = [{ x: 990, y: 196 }, { x: 1420, y: 196 }]; // two-phone positions (zoom .8)
const roleTag = (cls, label, x, y, at = 0.2) => `<div class="role-tag ${cls}" style="left:${x}px;top:${y}px" data-in="${at},down">${label}</div>`;
const sectionTitle = (t, right = '') => `<div class="row mt20" style="margin-bottom:10px"><div class="b t16 sp">${t}</div>${right}</div>`;
const AN = (s = 44) => av('AN', '#1E88E5', s);
const SL = (s = 44) => av('SL', '#8E44AD', s);
const MA = (s = 44) => av('MA', '#E67E22', s);
const ONB = window.ONBOARDING_SVGS;

function topbar({ toggleKf = '', toggleOn = '', tapP = '', tapD = '', bellDot = true } = {}) {
  return `<div class="abs row" style="left:0;right:0;top:0;height:56px;padding:0 14px 0 18px;z-index:5">
    ${logo(24)}<div class="sp"></div>${toggle(toggleKf, toggleOn, tapP, tapD)}
    <div class="iconbtn" style="margin-left:4px">${ic('bell')}${bellDot ? '<span style="position:absolute;top:8px;right:9px;width:8px;height:8px;border-radius:4px;background:#EF4444"></span>' : ''}</div></div>`;
}

function searchCard(o = {}) {
  const fromV = o.fromAt !== undefined ? sw(o.fromAt, '<span style="color:#B8B8B8">Where from?</span>', o.from || 'Copenhagen H') : (o.from ? o.from : '<span style="color:#B8B8B8">Where from?</span>');
  const toV = o.toAt !== undefined ? sw(o.toAt, '<span style="color:#B8B8B8">Where to?</span>', o.to || 'Aarhus C') : (o.to ? o.to : '<span style="color:#B8B8B8">Where to?</span>');
  const dateV = o.dateAt !== undefined ? sw(o.dateAt, 'Today', '16 Oct') : (o.date || 'Today');
  return `<div class="card shadow" style="border-radius:24px;padding:4px 16px 16px" ${o.attrs || ''}>
    <div class="row" style="align-items:center">
      <div class="route sp" style="padding:10px 0 4px"><div class="rail" style="padding-top:26px;padding-bottom:22px"><i></i><b></b><i class="end"></i></div>
        <div class="sp">
          <div style="padding:8px 0" ${o.tapFrom ? `data-tap="${o.tapFrom}"` : ''}><div class="t12" style="color:#888">From</div><div class="t16 b mt4">${fromV}</div></div>
          <div style="height:1px;background:#EEE;margin:2px 0"></div>
          <div style="padding:8px 0" ${o.tapTo ? `data-tap="${o.tapTo}"` : ''}><div class="t12" style="color:#888">To</div><div class="t16 b mt4">${toV}</div></div>
        </div></div>
      <div class="iconbtn" style="width:36px;height:36px;border:1px solid #DDD;color:#666">${ic('swap')}</div>
    </div>
    <div style="height:1px;background:#EEE;margin:6px 0 12px"></div>
    <div class="row">
      <span class="chip" ${o.tapDate ? `data-tap="${o.tapDate}"` : ''}>${ic('cal', '', 'width:16px;height:16px')} ${dateV}</span>
      <span class="chip" ${o.tapSeats ? `data-tap="${o.tapSeats}"` : ''}>${ic('users', '', 'width:16px;height:16px')} 1 seat</span>
    </div>
    <div class="btn mt12" ${o.tapSearch ? `data-tap="${o.tapSearch}"` : ''}>${ic('search')} ${o.loadAt !== undefined ? sw(o.loadAt, 'Search', 'Finding rides...') : 'Search'}</div>
  </div>`;
}

function passengerBody(o = {}) {
  return `
  <div class="abs" style="left:0;right:0;top:-54px;height:330px;background:linear-gradient(rgba(200,241,53,.30),rgba(255,255,255,0))"></div>
  ${topbar({ toggleKf: o.toggleKf, toggleOn: o.toggleOn, tapP: o.tapP, tapD: o.tapD })}
  <div class="abs" style="left:0;right:0;top:56px;bottom:88px;overflow:hidden" ${o.passOp ? `data-op="${o.passOp}"` : ''}>
    <div class="content" ${o.scroll ? `data-scroll="${o.scroll}"` : ''} style="padding-top:6px">
      <div class="row" style="padding:6px 0 14px">
        <div class="av" style="background:rgba(255,255,255,.7);color:#0D0D0D;width:38px;height:38px">S</div>
        <div><div class="b" style="font-size:20px">Good morning, Sofie 👋</div><div class="t14" style="color:rgba(13,13,13,.6)">Where are you headed today?</div></div>
      </div>
      ${o.banner === false ? '' : `<div class="card dark row" style="border-radius:18px;padding:14px 16px;margin-bottom:14px">
        <div style="width:10px;height:10px;border-radius:5px;background:#C8F135;box-shadow:0 0 0 5px rgba(200,241,53,.2)"></div>
        <div class="sp"><div class="t12" style="color:#B3B3B3">Departs in 23h 14m 05s</div><div class="b t16 mt4">Roskilde St → Copenhagen H</div></div>${ic('fwd')}</div>`}
      ${searchCard(o.search || {})}
      ${sectionTitle('Saved places')}
      <div class="row" style="gap:8px">
        <span class="chip" ${o.tapSaved ? `data-tap="${o.tapSaved}"` : ''}>${ic('home', '', 'width:15px;height:15px')} Home · Valby</span>
        <span class="chip">${ic('wallet', '', 'width:15px;height:15px')} Work · Ørestad</span>
        <span class="chip" style="color:#167A30">${ic('plus', '', 'width:15px;height:15px')} Add</span>
      </div>
      ${sectionTitle('Recent searches')}
      <div class="col" style="gap:8px">
        <div class="card row" style="padding:12px 14px" ${o.tapRecent ? `data-tap="${o.tapRecent}"` : ''}>${ic('clock', 'muted')}<div class="b t14 sp">Copenhagen H → Aarhus C</div><span class="badge s">×3</span></div>
        <div class="card row" style="padding:12px 14px">${ic('clock', 'muted')}<div class="b t14 sp">Odense St → Aalborg St</div><span class="badge s">×1</span></div>
      </div>
      ${sectionTitle('Popular routes')}
      <div class="row" style="gap:10px">
        <div class="card" style="flex:1;padding:12px"><div class="b t14">København → Aarhus</div><div class="t12 muted mt4">Most-booked corridor in DK</div></div>
        <div class="card" style="flex:1;padding:12px"><div class="b t14">Roskilde → København</div><div class="t12 muted mt4">Daily commuters welcome</div></div>
      </div>
      <div class="card muted-bg mt16 row" style="gap:14px">
        <div style="width:44px;height:44px;border-radius:14px;background:#E9FBC9;display:flex;align-items:center;justify-content:center;color:#167A30;flex:none">${ic('leaf')}</div>
        <div class="sp"><div class="b t14">Travel greener with Ridly</div><div class="t12 muted mt4">Carpooling 100 km saves about 12 kg of CO₂ per seat.</div></div></div>
      <div class="row mt12" style="gap:10px">
        <div class="card" style="flex:1;text-align:center"><div class="b" style="font-size:22px">4.9 ${star(true, 18)}</div><div class="t12 muted mt4">Rating</div></div>
        <div class="card" style="flex:1;text-align:center"><div class="b" style="font-size:22px">12</div><div class="t12 muted mt4">Trips</div></div>
      </div>
      <div style="height:60px"></div>
    </div>
  </div>
  ${o.driverOp ? `<div class="abs" style="left:0;right:0;top:56px;bottom:88px;overflow:hidden;background:#fff" data-op="${o.driverOp}">${driverContent({})}</div>` : ''}
  ${nav('home', { chatBadge: 1, tap: o.navTap || {} })}`;
}

function driverContent(o = {}) {
  return `<div class="content" ${o.scroll ? `data-scroll="${o.scroll}"` : ''} style="padding-top:10px">
      <div class="card dark" style="border-radius:22px;padding:18px;background:linear-gradient(135deg,#1A1A1A,#24321A)">
        <div class="row"><div class="t13" style="color:#B3B3B3">Earnings this month</div><div class="sp"></div><span class="badge l">October</span></div>
        <div class="mono mt8" style="font-size:34px;color:#C8F135">DKK <span ${o.countAt !== undefined ? `data-count="${o.countAt},1.4,0,1140"` : ''}>1.140</span></div>
        <div class="t12 mt4" style="color:#B3B3B3">Est. tax: DKK 341,66</div>
        <svg viewBox="0 0 300 50" style="width:100%;height:46px;margin-top:10px"><path d="M0 42 L40 36 L80 40 L120 26 L160 30 L200 16 L240 20 L300 6" fill="none" stroke="#C8F135" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" pathLength="1" ${o.countAt !== undefined ? `data-draw="${o.countAt + 0.2},1.2"` : ''}/></svg>
        <div class="t11" style="color:#888">Last 7 days</div>
      </div>
      <div class="btn mt16" ${o.tapPost ? `data-tap="${o.tapPost}"` : ''}>${ic('plus')} Post a Trip</div>
      ${sectionTitle('Upcoming trips')}
      <div class="card shadow" style="padding:14px 16px">
        <div class="row"><span class="badge b">Commute</span><span class="badge l">New booking</span><div class="sp"></div><span class="t12 b">2/3 seats</span></div>
        <div class="b t16 mt8">Roskilde St → Copenhagen H</div>
        <div class="t13 muted mt4">Departs Mon 12 Oct · 07:15</div>
      </div>
      <div class="card shadow mt12" style="padding:14px 16px">
        <div class="row"><span class="badge s">Long Trip</span><span class="badge g">Threshold met</span><div class="sp"></div><span class="t12 b">3/3 seats</span></div>
        <div class="b t16 mt8">Copenhagen H → Aarhus C</div>
        <div class="t13 muted mt4">Departs Fri 16 Oct · 07:30</div>
      </div>
      ${sectionTitle('Repost a recent trip')}
      <div class="card row" style="padding:12px 14px" ${o.repostAttrs || ''}>
        <div class="sp"><div class="b t14">Copenhagen H → Odense St</div><div class="t12 muted mt4">DKK 154,00/seat</div></div>
        <span class="btn sm dark" ${o.tapRepost ? `data-tap="${o.tapRepost}"` : ''}>${ic('repeat', '', 'width:16px;height:16px')} Repost</span></div>
      ${sectionTitle('Demand near you')}
      <div class="row" style="gap:10px">
        <div class="card" style="flex:1;padding:12px"><div class="b t14">Roskilde → CPH</div><div class="t12 muted mt4">MON · WED · FRI</div></div>
        <div class="card" style="flex:1;padding:12px"><div class="b t14">Odense → Aarhus</div><div class="t12 muted mt4">Weekends</div></div>
      </div>
      <div style="height:60px"></div>
    </div>`;
}

function driverBody(o = {}) {
  return `${topbar({ toggleKf: '0:84', toggleOn: '-99' })}
    <div class="abs" style="left:0;right:0;top:56px;bottom:88px;overflow:hidden">${driverContent(o)}</div>
    ${nav('home', { tap: o.navTap || {} })}`;
}

// ═════════════════════════════════════════════════════════════════════════════
// 00:00–00:09  Dawn. Two phones wake, a lime route joins them and draws the title.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  // Deterministic skyline: low Danish roofs, a few spires and the odd crane.
  let sky = '', x = 0, seed = 7;
  const rnd = () => (seed = (seed * 9301 + 49297) % 233280) / 233280;
  while (x < 1920) {
    const w = 40 + rnd() * 90, h = 70 + rnd() * 120;
    sky += `<rect x="${x}" y="${1080 - h}" width="${w + 1}" height="${h}"/>`;
    if (rnd() > 0.55) sky += `<polygon points="${x + 6},${1080 - h} ${x + w / 2},${1080 - h - 30 - rnd() * 30} ${x + w - 6},${1080 - h}"/>`;
    if (rnd() > 0.86) sky += `<rect x="${x + w / 2 - 3}" y="${1080 - h - 120}" width="6" height="120"/><polygon points="${x + w / 2 - 7},${1080 - h - 120} ${x + w / 2},${1080 - h - 175} ${x + w / 2 + 7},${1080 - h - 120}"/>`;
    x += w;
  }
  const pA = [470, 520], pB = [1450, 520];
  Engine.scene({
    start: 0, end: 9,
    html: `
    <div class="abs" style="inset:0;background:linear-gradient(#0B1026 0%,#1E2350 38%,#5A4A7A 62%,#E58E5E 86%,#F6C177 100%)"></div>
    <div class="abs" style="inset:0;background:#06070F" data-op="0:1 6:0.15"></div>
    <div class="abs" style="left:760px;top:760px;width:400px;height:400px;border-radius:50%;background:radial-gradient(circle,#FFE6A3 0%,#F7B267 35%,rgba(247,178,103,0) 70%)" data-ty="0:160 8:0"></div>
    <svg class="abs" viewBox="0 0 1920 1080" style="inset:0;width:1920px;height:1080px"><g fill="#0B0B12" opacity=".92">${sky}</g></svg>

    ${phone(scr(0, 99, passengerBody({ banner: false })), { x: 260, y: 236, zoom: 0.62, wake: '0.5:1 1.2:0', attrs: 'data-in="0.2,up"' })}
    ${phone(scr(0, 99, driverBody()), { x: 1400, y: 236, zoom: 0.62, wake: '1.0:1 1.7:0', attrs: 'data-in="0.5,up"' })}
    <div class="role-tag pass" style="left:315px;top:180px" data-in="1.3,down">Passenger</div>
    <div class="role-tag drv" style="left:1475px;top:180px" data-in="1.7,down">Driver</div>

    <svg class="abs" viewBox="0 0 1920 1080" style="inset:0;width:1920px;height:1080px;overflow:visible">
      <path d="M${pA[0]} ${pA[1]} C 720 ${pA[1]}, 700 380, 960 380 S 1200 ${pB[1]}, ${pB[0]} ${pB[1]}" fill="none" stroke="#C8F135" stroke-width="6" stroke-linecap="round" pathLength="1" data-draw="2.0,2.0" style="filter:drop-shadow(0 0 12px rgba(200,241,53,.6))"/>
    </svg>
    ${[pA, pB].map((p, i) => `<svg class="abs" viewBox="0 0 24 24" style="left:${p[0] - 26}px;top:${p[1] - 56}px;width:52px;height:52px" data-in="${1.6 + i * 0.4},pop">
      <path d="M12 23s-8-7.2-8-12.5a8 8 0 0 1 16 0C20 15.8 12 23 12 23z" fill="#C8F135" stroke="#0B0B0B" stroke-width="1.2"/><circle cx="12" cy="10.5" r="3" fill="#0B0B0B"/></svg>`).join('')}

    <div class="abs" id="introTitle" style="left:0;right:0;top:250px;text-align:center">
      <div style="display:inline-block;filter:drop-shadow(0 10px 40px rgba(0,0,0,.4))">${logo(150, '#fff')}</div>
    </div>
    <div class="abs" style="left:0;right:0;top:470px;text-align:center" data-in="4.6,up">
      <div style="font:700 34px Inter;color:#fff">Denmark’s carpooling platform</div>
    </div>
    <div class="abs" style="left:260px;right:260px;top:820px;text-align:center;font:700 44px/1.2 Syne;color:#fff;text-shadow:0 4px 24px rgba(0,0,0,.6)" data-in="0.8,up">
      Somewhere between where we are and where we’re going, <span style="color:#C8F135">there’s a seat to share.</span></div>
    `,
    frame(t, el) {
      const p = Engine.easeInOut(Engine.clamp((t - 3.4) / 1.2));
      const tt = el.querySelector('#introTitle');
      tt.style.clipPath = `inset(0 ${((1 - p) * 100).toFixed(2)}% 0 0)`;
    },
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 00:09–00:21  Splash → onboarding → account glimpses on a progress route.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const slides = [
    ['Fewer cars.<br>Better Journeys.', 'Denmark’s carpooling platform.<br>Share a ride, shrink your footprint'],
    ['Every seat<br>filled matters.', 'One shared trip can cut CO₂<br>emissions in half. Small change,<br>big difference.'],
    ['Move together.<br>Live lighter.', 'Join thousands of Danes choosing<br>smarter, greener travel<br>one ride at a time.'],
  ];
  // Splash: a 1:1 port of SplashScreen.kt's Canvas (320x703 reference, stretched to the screen like Compose's sx/sy).
  const rr = (c, x, y, w, h, r, extra = '') => `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${r}" fill="${c}" ${extra}/>`;
  const ov = (c, x, y, w, h, extra = '') => `<ellipse cx="${x + w / 2}" cy="${y + h / 2}" rx="${w / 2}" ry="${h / 2}" fill="${c}" ${extra}/>`;
  const wheel = cx => `<g class="spw" style="transform-origin:${cx}px 394px">
      <circle cx="${cx}" cy="394" r="16" fill="#37474F"/><circle cx="${cx}" cy="394" r="9" fill="#78909C"/><circle cx="${cx}" cy="394" r="4" fill="#B0BEC5"/>
      <path d="M${cx} 383V405M${cx - 11} 394H${cx + 11}" stroke="#546E7A" stroke-width="2.5"/></g>`;
  const splash = `
    <svg class="abs" viewBox="0 0 320 703" preserveAspectRatio="none" style="left:0;top:-54px;width:390px;height:844px">
      <style>
        @keyframes spCloudL { from { transform: translateX(0) } to { transform: translateX(18px) } }
        @keyframes spCloudR { from { transform: translateX(0) } to { transform: translateX(-14px) } }
        @keyframes spBob { from { transform: translateY(0) } to { transform: translateY(-3px) } }
        @keyframes spSpin { to { transform: rotate(360deg) } }
        @keyframes spRoad { to { transform: translateX(-80px) } }
        @keyframes spTreeL { from { transform: rotate(-2deg) } to { transform: rotate(2deg) } }
        @keyframes spTreeR { from { transform: rotate(2deg) } to { transform: rotate(-2deg) } }
        @keyframes spSun { from { transform: scale(1) } to { transform: scale(1.05) } }
        @keyframes spBird { from { transform: translateX(0) } to { transform: translateX(60px) } }
        @keyframes spDot { from { transform: scale(1); opacity: .6 } to { transform: scale(1.4); opacity: 1 } }
        .spCL { animation: spCloudL 6s ease-in-out infinite alternate; }
        .spCR { animation: spCloudR 7s ease-in-out infinite alternate; }
        .spCar { animation: spBob .8s ease-in-out infinite alternate; }
        .spw { animation: spSpin .9s linear infinite; }
        .spRoad { animation: spRoad 1s linear infinite; }
        .spTL { transform-origin: 18px 370px; animation: spTreeL 3s ease-in-out infinite alternate; }
        .spTR { transform-origin: 294px 370px; animation: spTreeR 3.4s ease-in-out infinite alternate; }
        .spSun { transform-origin: 260px 90px; animation: spSun 4s ease-in-out infinite alternate; }
        .spBird { animation: spBird 5s ease-in-out infinite alternate; }
        .spDot { transform-origin: 160px 648px; animation: spDot .7s ease-in-out infinite alternate; }
      </style>
      <rect width="320" height="703" fill="#E3F2FD"/>
      <g class="spSun"><circle cx="260" cy="90" r="38" fill="#FFF9C4"/><circle cx="260" cy="90" r="26" fill="#FFF176"/></g>
      <g class="spBird" fill="none" stroke="#78909C" stroke-opacity=".6" stroke-width="1.5" stroke-linecap="round"><path d="M50 120Q54 116 58 120"/><path d="M62 118Q66 114 70 118"/></g>
      <g class="spCL">${ov('#fff', 32, 50, 76, 36, 'fill-opacity=".92"')}${ov('#fff', 74, 44, 52, 32, 'fill-opacity=".92"')}${ov('#fff', 26, 51, 40, 26, 'fill-opacity=".92"')}</g>
      <g class="spCR">${ov('#fff', 168, 37, 64, 30, 'fill-opacity=".75"')}${ov('#fff', 200, 31, 44, 26, 'fill-opacity=".75"')}${ov('#fff', 162, 40, 32, 20, 'fill-opacity=".75"')}</g>
      ${rr('#B2DFDB', 10, 210, 28, 120, 3, 'fill-opacity=".55"')}${rr('#A5D6A7', 42, 195, 22, 135, 3, 'fill-opacity=".5"')}${rr('#B2DFDB', 68, 220, 18, 110, 3, 'fill-opacity=".5"')}
      ${rr('#A5D6A7', 240, 200, 24, 130, 3, 'fill-opacity=".5"')}${rr('#B2DFDB', 268, 215, 30, 115, 3, 'fill-opacity=".5"')}${rr('#A5D6A7', 292, 205, 22, 125, 3, 'fill-opacity=".5"')}
      ${[[16, 222], [26, 222], [16, 234], [246, 212], [256, 212]].map(([x, y]) => rr('#fff', x, y, 6, 5, 1, 'fill-opacity=".45"')).join('')}
      ${rr('#80CBC4', 0, 258, 40, 112, 4)}${rr('#4DB6AC', 44, 246, 34, 124, 4)}${rr('#80CBC4', 82, 266, 28, 104, 4)}
      ${rr('#4DB6AC', 210, 253, 36, 117, 4)}${rr('#80CBC4', 250, 263, 30, 107, 4)}${rr('#4DB6AC', 284, 248, 36, 122, 4)}
      ${[[6, 270], [18, 270], [6, 284], [18, 284], [50, 258], [64, 258], [216, 264], [230, 264]].map(([x, y]) => rr('#fff', x, y, 8, 7, 1, 'fill-opacity=".55"')).join('')}
      <rect y="358" width="320" height="345" fill="#A5D6A7"/>${ov('#81C784', -60, 322, 440, 76)}
      <rect y="390" width="320" height="36" fill="#546E7A"/>
      <svg x="0" y="390" width="320" height="36" overflow="hidden"><g class="spRoad">${[-1, 0, 1, 2, 3, 4, 5, 6].map(i => rr('#ECEFF1', i * 80, 15, 44, 5, 2.5, 'fill-opacity=".65"')).join('')}</g></svg>
      <g class="spTL"><rect x="14" y="338" width="9" height="54" fill="#5D4037"/>${ov('#388E3C', -6, 292, 48, 56)}${ov('#43A047', 1, 288, 34, 40)}
        <rect x="50" y="350" width="8" height="42" fill="#5D4037"/>${ov('#2E7D32', 35, 311, 38, 46)}${ov('#388E3C', 40, 307, 28, 34)}</g>
      <g class="spTR"><rect x="290" y="340" width="9" height="52" fill="#5D4037"/>${ov('#388E3C', 270, 294, 48, 56)}${ov('#43A047', 277, 290, 34, 40)}
        <rect x="257" y="348" width="8" height="44" fill="#5D4037"/>${ov('#2E7D32', 242, 309, 38, 46)}${ov('#388E3C', 247, 305, 28, 34)}</g>
      <g class="spCar">
        ${rr('#26C6DA', 88, 356, 144, 38, 10)}${rr('#00ACC1', 106, 333, 100, 34, 10)}
        ${rr('#E0F7FA', 114, 339, 36, 22, 5, 'fill-opacity=".9"')}${rr('#E0F7FA', 156, 339, 36, 22, 5, 'fill-opacity=".9"')}
        <circle cx="132" cy="348" r="9" fill="#FFCC80"/><circle cx="174" cy="348" r="9" fill="#FFAB91"/>
        <path d="M129 350Q132 354 135 350M171 350Q174 354 177 350" fill="none" stroke="#E64A19" stroke-width="1.3" stroke-linecap="round"/>
        ${rr('#0097A7', 88, 386, 144, 8, 4)}${rr('#FFF9C4', 224, 364, 10, 9, 3)}${rr('#EF9A9A', 90, 364, 8, 9, 3)}
        ${wheel(116)}${wheel(204)}
      </g>
      <rect y="422" width="320" height="281" fill="#66BB6A"/>${ov('#81C784', -60, 400, 440, 44)}
      <circle cx="40" cy="436" r="4" fill="#FFF9C4"/><circle cx="280" cy="440" r="3" fill="#FFF9C4"/>
      <circle cx="72" cy="446" r="3" fill="#fff" fill-opacity=".7"/><circle cx="248" cy="432" r="4" fill="#fff" fill-opacity=".7"/>
      <rect y="472" width="320" height="231" fill="#1B4332" fill-opacity=".94"/>
      <g data-in="0.4,up">
        <rect x="88" y="508" width="44" height="44" rx="11" fill="#C5FF45"/>
        <path d="M102 516L118 530L102 544" fill="none" stroke="#0B0B0B" stroke-width="7" stroke-linecap="round" stroke-linejoin="round"/>
        <text x="142" y="531" dominant-baseline="central" font-family="Nunito" font-weight="900" font-size="39.6" letter-spacing="-1.2" fill="#fff">ridly</text>
      </g>
      <text x="160" y="587" text-anchor="middle" font-family="Inter" font-size="9.2" letter-spacing="1.7" fill="#95D5B2" data-in="1.2,fade">SHARE THE RIDE</text>
      <text x="160" y="607" text-anchor="middle" font-family="Inter" font-size="7.5" letter-spacing=".8" fill="#52B788" data-in="1.6,fade">less carbon · more journey</text>
      <circle class="spDot" cx="160" cy="648" r="4" fill="#52B788"/>
    </svg>`;
  // OnboardingScreen.kt: logo top-start, centred 280×200 illustration + headline + subheadline,
  // pinned dots / Get Started / Log In (ghost = lime outline) at the bottom.
  const D = 0.85;
  const onboarding = `
    <div class="abs" style="left:16px;top:8px">${logo(38)}</div>
    <div class="abs" style="left:24px;right:24px;top:0;height:550px">
      ${slides.map(([h, b], i) => `<div class="abs" style="inset:0"
          data-vis="${i ? i * D : -9},${i < 2 ? (i + 1) * D : 99}" data-in="${i ? i * D : 0.05},${i ? 'left' : 'scale'}">
        <div style="height:100%;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center">
          <div style="width:280px;height:200px;border-radius:16px;overflow:hidden;flex:none">${ONB[i]}</div>
          <div style="font:700 32px/40px Syne;color:#0D0D0D;margin-top:32px">${h}</div>
          <div style="font:400 16px/24px Inter;letter-spacing:.5px;color:#5F6368;margin-top:8px">${b}</div>
        </div>
      </div>`).join('')}
    </div>
    <div class="abs" style="left:24px;right:24px;bottom:40px" data-in="0.1,up">
      <div class="row" style="justify-content:center;gap:8px">
        ${[0, 1, 2].map(i => `<span class="dotp" style="height:8px;width:8px;border-radius:4px;background:rgba(95,99,104,.35)" data-on="${i ? i * D : -9},${i < 2 ? (i + 1) * D : 99}"></span>`).join('')}
      </div>
      <div class="btn" style="margin-top:24px" data-tap="2.35">Get Started</div>
      <div class="btn" style="margin-top:16px;background:transparent;border:1px solid #C8F135;color:#C8F135">Log In</div>
    </div>`;

  const mini = (inner, i, label, x) => `
    <div class="pwrap" style="left:${x}px;top:250px" data-in="${5.6 + i * 1.3},up">
      <div class="phone" style="zoom:.46"><div class="island"></div><div class="screen">${inner}</div></div>
    </div>
    <div class="abs" style="left:${x}px;width:192px;top:700px;text-align:center" data-in="${5.8 + i * 1.3}">
      <div style="width:16px;height:16px;border-radius:8px;margin:0 auto;background:#C8F135;box-shadow:0 0 0 6px rgba(200,241,53,.18)"></div>
      <div class="b" style="font-size:17px;margin-top:16px;color:#fff">${label}</div></div>`;
  const s0 = 5.6;
  const signup = scr(s0, 99, `<div class="content" style="padding-top:18px">
      <div class="h1">Create your account</div><div class="muted mt4">Start your Ridly journey</div>
      <div class="row mt20" style="gap:10px"><div class="sp">${field('First name', 'Sofie', { type: '0.4,0.4' })}</div><div class="sp">${field('Last name', 'Larsen', { type: '0.8,0.4' })}</div></div>
      <div class="mt12">${field('Email address', 'sofie@mail.dk', { type: '1.2,0.7', focus: '1.1,2' })}</div>
      <div class="mt12">${field('Phone number', '+45 20 98 76 54')}</div>
      <div class="mt12">${field('Password', '••••••••••')}</div>
      <div class="row mt12 t13"><span style="width:20px;height:20px;border-radius:5px;background:#167A30;color:#fff;display:flex;align-items:center;justify-content:center">${ic('check', '', 'width:14px;height:14px;stroke-width:3')}</span><span>I agree to the <b class="green">Terms of Service</b> and <b class="green">Privacy Policy</b></span></div>
      <div class="btn mt20" data-tap="2.2">Continue</div></div>`, { enter: 'none' });
  const login = scr(s0 + 1.3, 99, `<div class="content" style="padding-top:18px">
      <div class="h1">Welcome back</div><div class="muted mt4">Log in to your Ridly account</div>
      <div class="mt24">${field('Email address', 'sofie@mail.dk', { type: '0.3,0.6' })}</div>
      <div class="mt12">${field('Password', '••••••••••', { type: '0.9,0.4' })}</div>
      <div class="linkbtn mt12" style="text-align:right">Forgot password?</div>
      <div class="btn mt20" data-tap="1.5">Log In</div>
      <div class="row mt20 t13 muted" style="justify-content:center">or</div>
      <div class="btn ghost mt12">Continue with Apple</div><div class="btn ghost mt12">Continue with Google</div></div>`, { enter: 'none' });
  const verify = scr(s0 + 2.6, 99, `<div class="content" style="padding-top:110px;text-align:center">
      <div class="swap" data-on="1.0">
        <div class="when-off"><div style="width:96px;height:96px;border-radius:48px;margin:0 auto;background:#E9FBC9;display:flex;align-items:center;justify-content:center;color:#167A30">${ic('mail', '', 'width:44px;height:44px')}</div>
          <div class="h1 mt24">Check your email</div><div class="muted mt12">We sent a confirmation link to</div><div class="b mt4">sofie@mail.dk</div>
          <div class="btn ghost mt24">Resend email</div></div>
        <div class="when-on"><div style="width:110px;height:110px;border-radius:55px;margin:0 auto;background:#C8F135;display:flex;align-items:center;justify-content:center">${ic('check', '', 'width:56px;height:56px;stroke-width:3')}</div>
          <div class="h1 mt24">Email verified!</div><div class="muted mt12">Your account is ready. Taking you home…</div></div>
      </div></div>`, { enter: 'none' });
  const reset = scr(s0 + 3.9, 99, `<div class="content" style="padding-top:18px">
      <div class="h1">Reset password</div><div class="muted mt8" style="line-height:1.45">Enter the email address linked to your account and we'll send you a reset link.</div>
      <div class="mt24">${field('Email address', 'sofie@mail.dk', { type: '0.2,0.5' })}</div>
      <div class="btn mt20" data-tap="0.9">${sw(1.1, 'Send reset link', 'Check your email ✓')}</div>
      <div class="linkbtn mt20" style="text-align:center">Back to Log In</div></div>`, { enter: 'none' });

  const xs = [880, 1110, 1340, 1570];
  Engine.scene({
    start: 9, end: 21,
    html: `
    ${copy({ y: 330, eyebrow: 'Getting started', vo: 'Ridly brings passengers and drivers together for the trips <em>already happening.</em>', sup: 'One account. Two ways to ride.' })}
    <div class="ctx" data-from="0">
      ${phone(scr(0, 2.6, splash, { enter: 'none', exit: '', bg: '#E3F2FD' }) + scr(2.6, 99, onboarding, { enter: 'fade' }), { x: PX, y: PY, attrs: 'data-in="0.1,up" data-out="5.3"' })}
    </div>
    <svg class="abs" viewBox="0 0 1920 1080" style="inset:0;width:1920px;height:1080px">
      <path d="M${xs[0] + 96} 708 L ${xs[3] + 96} 708 L 1920 708" fill="none" stroke="#C8F135" stroke-width="4" stroke-linecap="round" pathLength="1" data-draw="5.6,6.4"/>
    </svg>
    ${mini(signup, 0, 'Sign up', xs[0])}${mini(login, 1, 'Log in', xs[1])}${mini(verify, 2, 'Confirm email', xs[2])}${mini(reset, 3, 'Reset password', xs[3])}
    `,
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 00:21–00:36  Passenger Home: role toggle, search entry, saved places, recents.
// ═════════════════════════════════════════════════════════════════════════════
Engine.scene({
  start: 21, end: 36,
  html: `
  ${copy({ y: 330, eyebrow: 'For passengers', vo: 'Start with a destination, a familiar place, or <em>a search you’ve made before.</em>' })}
  ${phone(scr(0, 99, passengerBody({
    toggleKf: '0:0 1.0:0 1.35:84 3.4:84 3.75:0', toggleOn: '1.15,3.55', tapD: '0.9', tapP: '3.3',
    driverOp: '0:0 1.2:0 1.5:1 3.5:1 3.8:0',
    scroll: '0:0 6:0 7.2:330 10:330 11.4:520',
    search: { attrs: 'data-sc="0:1 4.4:1 4.7:1.03 5.1:1"' },
    tapSaved: '8.0', tapRecent: '9.0',
  }), { enter: 'none' }), { x: PX, y: PY, attrs: 'data-in="0.1,up"' })}
  `,
});

// ═════════════════════════════════════════════════════════════════════════════
// 00:36–00:53  Search: autocomplete, date, seats, results, filters, map motif.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const picker = (q, results, typeAt, tapAt, label) => `
    ${appbar(`<span class="t16 muted">${label}</span>`, { back: true })}
    <div class="content">
      <div class="field focus row" style="min-height:52px;padding:12px 14px">${ic('search', 'muted')}<div class="v sp" style="margin:0" data-type="${typeAt},0.6" data-text="${q}" data-ph="Search for a place"></div>${ic('x', 'muted')}</div>
      <div class="row mt16 green b t14" style="padding:6px 2px">${ic('pin')} Use current location</div>
      <div class="col mt8" style="gap:0">
        ${results.map((r, i) => `<div class="row" style="padding:14px 4px;border-bottom:1px solid #EEE" data-in="${typeAt + 0.5 + i * 0.12}" ${i === 0 ? `data-tap="${tapAt}"` : ''}>
          <div style="width:36px;height:36px;border-radius:18px;background:var(--input);display:flex;align-items:center;justify-content:center">${ic(i === 1 ? 'route' : 'pin', 'muted', 'width:18px;height:18px')}</div>
          <div class="sp"><div class="b t15">${r[0]}</div><div class="t13 muted mt4">${r[1]}</div></div></div>`).join('')}
      </div></div>`;

  // Material 3 DatePickerDialog in HopTheme's dark scheme (PassengerHomeScreen.kt). Opens on today (Fri 9 Oct).
  const DP_TAP = 0.9;
  const dayCell = d => {
    const today = d === 9, pick = d === 16;
    const base = 'width:40px;height:40px;border-radius:20px;margin:4px auto;display:flex;align-items:center;justify-content:center;font:400 14px Inter;position:relative';
    if (today) return `<div style="${base};border:1px solid #C8F135;color:#C8F135"><span>9</span>
      <div class="abs" style="inset:-1px" data-vis="-9,${DP_TAP}"><div style="width:100%;height:100%;border-radius:20px;background:#C8F135;color:#1A1A1A;display:flex;align-items:center;justify-content:center">9</div></div></div>`;
    if (pick) return `<div style="${base};color:#E6E1E5" data-tap="${DP_TAP - 0.1}"><span class="swap" data-on="${DP_TAP}"><span class="when-off">16</span><span class="when-on"></span></span>
      <div class="abs" style="inset:0;border-radius:20px;background:#C8F135;color:#1A1A1A;display:flex;align-items:center;justify-content:center" data-in="${DP_TAP},pop">16</div></div>`;
    return `<div style="${base};color:#E6E1E5">${d}</div>`;
  };
  const cal = `
    <div style="padding:16px 12px 0 24px">
      <div style="font:500 14px Inter;color:#B3B3B3">Select date</div>
      <div class="row" style="margin-top:28px"><div class="sp" style="font:700 32px/40px Syne;color:#E6E1E5">${sw(DP_TAP, 'Fri, Oct 9', 'Fri, Oct 16')}</div>${ic('edit', '', 'color:#B3B3B3;width:24px;height:24px;margin-right:12px')}</div>
    </div>
    <div style="height:1px;background:#49454F;margin-top:12px"></div>
    <div class="row" style="padding:10px 12px 0 24px;height:56px;color:#B3B3B3">
      <span style="font:700 14px Inter;color:#B3B3B3">October 2026</span>${ic('back', '', 'transform:rotate(-90deg);width:18px;height:18px')}
      <div class="sp"></div>${ic('back', '', 'width:24px;height:24px;margin-right:22px')}${ic('fwd', '', 'width:24px;height:24px;margin-right:8px')}</div>
    <div style="display:grid;grid-template-columns:repeat(7,1fr);padding:0 12px;text-align:center">
      ${['M', 'T', 'W', 'T', 'F', 'S', 'S'].map(d => `<div style="font:400 14px/48px Inter;color:#E6E1E5">${d}</div>`).join('')}
      ${[0, 0, 0].map(() => '<div></div>').join('')}
      ${Array.from({ length: 31 }, (_, i) => dayCell(i + 1)).join('')}
    </div>
    <div class="row" style="justify-content:flex-end;gap:8px;padding:4px 12px 14px">
      <span style="font:700 14px Inter;color:#C8F135;padding:10px 12px">Cancel</span>
      <span style="font:700 14px Inter;color:#C8F135;padding:10px 12px;position:relative" data-tap="1.4">OK</span></div>`;

  const results = `
    ${appbar(`<div><div class="b t16">Copenhagen H → Aarhus C</div><div class="t12 muted">Fri 16 Oct · 1 seat</div></div>`, { right: `<div class="iconbtn">${ic('filter')}</div>` })}
    <div class="content" style="padding-top:2px">
      <div class="row" style="gap:8px"><span class="chip" data-on="-9,2.6">All</span><span class="chip">Commute</span><span class="chip" data-on="2.6" data-tap="2.4">Long Trip</span><span class="chip">${ic('cal', '', 'width:14px;height:14px')} Fri 16 Oct</span></div>
      <div class="row mt12" style="gap:8px"><span class="t12 muted">Sort</span><span class="chip on" style="height:28px">Earliest</span><span class="chip" style="height:28px">Cheapest</span></div>
      <div class="card" style="margin-top:12px;padding:0;overflow:hidden;border-radius:18px;height:0" data-in="4.2,fade" id="resMap">
        <svg viewBox="0 0 354 130" style="display:block;width:354px;height:130px;background:#EEF3E8">
          <path d="M0 30 H354 M0 74 H354 M0 108 H354 M60 0 V130 M150 0 V130 M250 0 V130" stroke="#fff" stroke-width="6"/>
          <path d="M0 52 C80 60 120 20 200 40 S 300 90 354 70" stroke="#DCE6D0" stroke-width="14" fill="none"/>
          <path d="M300 100 C 240 96, 210 60, 160 64 S 80 40, 50 30" fill="none" stroke="#167A30" stroke-width="5" stroke-linecap="round" pathLength="1" data-draw="4.3,1.2"/>
          <circle cx="300" cy="100" r="8" fill="#fff" stroke="#167A30" stroke-width="4"/><circle cx="50" cy="30" r="8" fill="#167A30"/>
          <text x="262" y="124" font-family="Inter" font-weight="700" font-size="11" fill="#333">Copenhagen H</text>
          <text x="64" y="26" font-family="Inter" font-weight="700" font-size="11" fill="#333">Aarhus C</text>
        </svg></div>
      <div class="col mt12" style="gap:12px">
        ${tripCard({ from: 'Copenhagen H', to: 'Aarhus C', dep: '07:30', arr: '10:45', price: '228,00', seats: '2 seats left', driver: 'Anders Nielsen', rating: '4.9', attrs: 'data-in="0.5,up"' })}
        <div data-vis="-9,3.0" data-out="2.65">${tripCard({ from: 'Copenhagen H', to: 'Aarhus C', dep: '06:40', arr: '09:55', price: '228,00', seats: '1 seat left', driver: 'Lars Eriksen', rating: '4.7', type: 'Commute', initials: 'LE', color: '#E67E22', attrs: 'data-in="0.7,up"' })}</div>
        ${tripCard({ from: 'Valby St', to: 'Aarhus C', dep: '08:15', arr: '11:30', price: '219,00', seats: '3 seats left', driver: 'Mette Hansen', rating: '4.8', initials: 'MH', color: '#16A085', attrs: 'data-in="0.9,up"' })}
        ${tripCard({ from: 'Copenhagen H', to: 'Aarhus C', dep: '13:00', arr: '16:15', price: '228,00', seats: '2 seats left', driver: 'Emma Larsen', rating: '5.0', initials: 'EL', color: '#C0392B', attrs: 'data-in="1.1,up"' })}
      </div></div>`;

  Engine.scene({
    start: 36, end: 53,
    html: `
    ${copy({ y: 320, eyebrow: 'Search', vo: 'Choose where and when. Browse available rides, dates, seats, and prices <em>in one place.</em>', sup: 'From · To · Date · Seats' })}
    ${phone(
      scr(0, 99, passengerBody({
        banner: false,
        search: { fromAt: 3.0, toAt: 5.6, dateAt: 7.6, tapFrom: '0.5', tapTo: '3.3', tapDate: '6.0', tapSeats: '8.3', tapSearch: '9.7', loadAt: 9.8 },
      }), { enter: 'none' }) +
      scr(0.8, 3.0, picker('Københ', [['Copenhagen H', 'Central Station, København V'], ['Copenhagen Airport (CPH)', 'Kastrup'], ['Københavns Universitet', 'Nørre Campus, København N']], 0.3, 1.5, 'Where from?'), { enter: 'up', exit: 'down' }) +
      scr(3.5, 5.6, picker('Aarhus', [['Aarhus C', 'Central Station, Aarhus'], ['Aarhus Airport (AAR)', 'Tirstrup'], ['Aarhus Universitet', 'Nordre Ringgade, Aarhus C']], 0.3, 1.5, 'Where to?'), { enter: 'up', exit: 'down' }) +
      `<div class="ctx" data-from="6.1">${scrim(0, 1.6)}<div class="abs" style="left:15px;right:15px;top:62px;border-radius:28px;background:#2B2930;z-index:36;box-shadow:0 20px 50px rgba(0,0,0,.35);overflow:hidden" data-in="0,scale" data-out="1.6">${cal}</div></div>` +
      `<div class="ctx" data-from="8.5">${scrim(0, 1.0)}<div class="sheet" style="padding:12px 24px 40px" data-in="0,up" data-out="1.0"><div style="font:700 16px Inter;color:#0D0D0D">How many seats?</div>
         <div style="margin-top:16px">${[1, 2, 3, 4].map(n => `<div class="row" style="padding:8px 16px;border-radius:12px;${n === 1 ? 'background:rgba(200,241,53,.18)' : ''}" ${n === 1 ? 'data-tap="0.55"' : ''}>
           <div style="width:32px;height:32px;border-radius:16px;background:${n === 1 ? '#C8F135' : '#F7F8FA'};display:flex;align-items:center;justify-content:center;color:${n === 1 ? '#1A1A1A' : '#5F6368'}">${ic('user', 'f', 'width:16px;height:16px')}</div>
           <span style="margin-left:8px;font:${n === 1 ? 700 : 400} 16px Inter;color:#0D0D0D">${n === 1 ? '1 seat' : n + ' seats'}</span></div>
           ${n < 4 ? '<div style="height:1px;background:#F0F0F0;margin-left:48px"></div>' : ''}`).join('')}</div></div></div>` +
      scr(10.3, 99, results, { enter: 'push' }),
      { x: PX, y: PY, attrs: 'data-in="0.1,up"' })}
    `,
    frame(t, el) {
      // the map card grows open (height animation keeps cards gliding down beneath it)
      const m = el.querySelector('#resMap');
      const p = Engine.easeInOut(Engine.clamp((t - 10.3 - 4.2) / 0.7));
      m.style.height = (p * 130).toFixed(1) + 'px';
    },
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 00:53–01:08  Trip detail → seats → threshold → booking success.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const detail = `
    ${appbar('Trip Details', { right: `<div class="iconbtn">${ic('more')}</div>` })}
    <div class="abs" style="left:0;right:0;top:56px;bottom:96px;overflow:hidden"><div class="content" data-scroll="0:0 1.4:0 2.4:250 3.6:250 4.6:560">
      <div class="card">
        <div class="row"><span class="badge s">Long Distance</span><div class="sp"></div><span class="t13 muted">300 km</span></div>
        <div class="t13 muted mt12">Departs at Fri 16 Oct · 07:30</div>
        <div class="route mt12"><div class="rail"><i></i><b style="min-height:34px"></b><i class="end"></i></div>
          <div class="col" style="gap:22px"><div><div class="b t16">Copenhagen H</div><div class="t12 muted">07:30</div></div><div><div class="b t16">Aarhus C</div><div class="t12 muted">~10:45</div></div></div></div>
      </div>
      <div class="card mt12">
        <div class="t13 muted">Price per seat</div><div class="mono mt4" style="font-size:30px">DKK 228,00</div>
        <div class="t12 muted mt8">SKAT-suggested rate · pay driver via MobilePay after the ride</div>
      </div>
      <div class="card mt12">
        <div class="row"><div class="b t14 sp">1 of 3 seats confirmed</div><span class="badge y">Min 3 to run</span></div>
        <div class="progressbar mt12"><i data-bar="0.6,0.8,0,33"></i></div>
        <div class="t12 muted mt8">Trip only runs if at least 3 passengers book</div>
      </div>
      <div class="card mt12">
        <div class="row">${AN()}<div class="sp"><div class="b t16">Anders Nielsen</div><div class="row t13 muted" style="gap:4px">${stars(5, 13)} 4.9 · 38 ratings</div></div></div>
        <div class="row mt12" style="gap:8px"><span class="badge g">${ic('shield', '', 'width:12px;height:12px')} Phone verified</span><span class="linkbtn sp" style="text-align:right">View driver profile</span></div>
      </div>
      <div class="card mt12 row">
        <div style="width:44px;height:44px;border-radius:12px;background:var(--input);display:flex;align-items:center;justify-content:center">${ic('car')}</div>
        <div class="sp"><div class="b t15">Volkswagen Golf · White</div><div class="t13 muted mt4">AB 12 345 · 3 passenger seats</div></div></div>
      ${sectionTitle('Reviews', '<span class="t13 muted">38</span>')}
      <div class="card"><div class="row">${stars(5, 13)}<div class="sp"></div><span class="t12 muted">Emma L.</span></div><div class="t14 mt8">Very clean car, great conversation!</div></div>
      <div class="card mt12"><div class="row">${stars(5, 13)}<div class="sp"></div><span class="t12 muted">Mikkel S.</span></div><div class="t14 mt8">Punctual and friendly.</div></div>
      <div style="height:40px"></div>
    </div></div>
    <div class="abs" style="left:0;right:0;bottom:0;padding:14px 18px 28px;background:#fff;border-top:1px solid #EEE">
      <div class="btn" data-tap="5.9">Book Seat · DKK 228,00</div></div>`;

  const confirm = `
    ${appbar('Confirm Booking')}
    <div class="content">
      <div class="t13 muted b" style="letter-spacing:.06em">TRIP SUMMARY</div>
      <div class="card mt8">
        <div class="row"><div class="b t16 sp">Copenhagen H → Aarhus C</div><span class="badge s">Long Distance Trip</span></div>
        <div class="row mt12 t14"><span class="muted sp">Departs</span><span class="b">Fri 16 Oct · 07:30</span></div>
        <div class="row mt8 t14"><span class="muted sp">Driver</span><span class="b">Anders Nielsen</span></div>
      </div>
      <div class="card mt12 row"><div class="sp"><div class="b t16">Seats</div><div class="t12 muted mt4">Up to 2 seats available</div></div>${stepper([1, 2], [1.3])}</div>
      <div class="card mt12" style="padding:12px 14px;border-color:transparent" >
        <div class="swap" data-on="1.4">
          <div class="when-off row t13" style="color:#92600A">${ic('users')} This trip needs 1 more seat to be confirmed</div>
          <div class="when-on row t13" style="color:#15803D">${ic('check')} Your booking meets the minimum — trip confirms soon</div>
        </div></div>
      <div class="t13 muted b mt12" style="letter-spacing:.06em">PRICE SUMMARY</div>
      <div class="card mt8">
        <div class="row t14"><span class="muted sp">DKK 228,00 × ${sw(1.4, '1 seat', '2 seats')}</span><span class="mono">DKK <span data-count="1.4,0.6,228,456,2">228,00</span></span></div>
        <div class="divider"></div>
        <div class="row"><span class="b t16 sp">Total</span><span class="mono" style="font-size:24px">DKK <span data-count="1.4,0.6,228,456,2">228,00</span></span></div>
        <div class="row mt12 t12 muted" style="gap:6px">${ic('wallet', '', 'width:16px;height:16px')} Pay driver via MobilePay after ride</div>
      </div>
      <div class="btn mt20" data-tap="3.7">Confirm Booking</div>
    </div>`;

  const success = `
    <div class="content" style="padding-top:70px;text-align:center">
      <div style="width:120px;height:120px;border-radius:60px;margin:0 auto;background:#C8F135;display:flex;align-items:center;justify-content:center;box-shadow:0 0 0 14px rgba(200,241,53,.25)" data-in="0.2,pop">${ic('check', '', 'width:62px;height:62px;stroke-width:3')}</div>
      <div class="h1 mt24" data-in="0.5">Booking Confirmed!</div>
      <div class="muted mt12" style="line-height:1.45" data-in="0.65">Your seats are reserved. You’ll pay Anders directly via MobilePay after the ride.</div>
      <div class="card mt24" style="text-align:left" data-in="0.8">
        <div class="row t14"><span class="muted sp">From</span><span class="b">Copenhagen H → Aarhus C</span></div>
        <div class="row t14 mt8"><span class="muted sp">Departs</span><span class="b">Fri 16 Oct · 07:30</span></div>
        <div class="row t14 mt8"><span class="muted sp">Driver</span><span class="b">Anders Nielsen</span></div>
        <div class="row t14 mt8"><span class="muted sp">Seats</span><span class="b">2</span></div>
      </div>
      <div class="btn mt24" data-in="1.0">View My Trips</div>
      <div class="btn ghost mt12" data-in="1.1">Back to Home</div>
    </div>`;

  Engine.scene({
    start: 53, end: 68,
    html: `
    ${copy({ y: 320, eyebrow: 'Book with confidence', vo: 'Get to know the trip and <em>the person driving</em> before you book.', sup: 'Review the details. Choose your seat.' })}
    ${phone(scr(0, 6.3, detail, { enter: 'none' }) + scr(6.3, 11.0, confirm) + scr(11.0, 99, success, { enter: 'fade' }), { x: PX, y: PY, attrs: 'data-in="0.1,up"' })}
    `,
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 01:08–01:18  No results → search alert.
// ═════════════════════════════════════════════════════════════════════════════
Engine.scene({
  start: 68, end: 78,
  html: `
  ${copy({ y: 340, eyebrow: 'Search alerts', vo: 'And if the right ride isn’t there yet, ask Ridly to <em>let you know when one appears.</em>' })}
  ${phone(scr(0, 99, `
    ${appbar(`<div><div class="b t16">Odense St → Aalborg St</div><div class="t12 muted">Sat 17 Oct · 1 seat</div></div>`)}
    <div class="content">
      <div class="row" style="gap:8px"><span class="chip on">All</span><span class="chip">Commute</span><span class="chip">Long Trip</span></div>
      <div class="col mt16" style="gap:12px" data-out="1.4">
        ${[0, 1, 2].map(() => `<div style="height:120px;border-radius:18px;background:linear-gradient(90deg,#EDEFF2,#F8F9FB,#EDEFF2)"></div>`).join('')}
      </div>
      <div class="abs" style="left:18px;right:18px;top:150px;text-align:center" data-in="1.7">
        <div style="width:150px;height:150px;border-radius:75px;margin:0 auto;background:#F1F8E9;display:flex;align-items:center;justify-content:center;color:#167A30">
          <svg viewBox="0 0 24 24" style="width:70px;height:70px;fill:none;stroke:currentColor;stroke-width:1.5;stroke-linecap:round;stroke-linejoin:round">${UI.P.route}</svg></div>
        <div class="h2 mt24">No rides on this route yet</div>
        <div class="muted mt8 t14">Odense St → Aalborg St · Sat 17 Oct</div>
        <div class="btn mt24" data-tap="4.0">${sw(4.25, `${ic('bell')} Alert me when one appears`, `${ic('check')} Alert set`)}</div>
      </div>
    </div>
    <div class="toast" data-in="4.4,up">${ic('bell', '', 'color:#C8F135')}<span>Alert set! We'll notify you when a ride appears.</span></div>
  `, { enter: 'none' }), { x: PX, y: PY, attrs: 'data-in="0.1,up"' })}
  `,
});

// ═════════════════════════════════════════════════════════════════════════════
// 01:18–01:32  Driver setup: car, MobilePay number, driver home.
// ═════════════════════════════════════════════════════════════════════════════
Engine.scene({
  start: 78, end: 92,
  html: `
  ${copy({ y: 320, eyebrow: 'For drivers', vo: 'For drivers, getting started means adding your car and <em>the number passengers can use to pay you directly.</em>' })}
  ${roleTag('drv', 'Driver', PX + 140, PY - 56)}
  ${phone(
    scr(0, 6.0, `${appbar('Become a Driver')}
      <div class="content">
        <div class="h1">Your Car Details</div><div class="muted mt8 t14" style="line-height:1.45">We need a few details about your car to set up your driver profile.</div>
        <div class="mt20">${field('Make', 'Volkswagen', { type: '0.7,0.6', focus: '0.6,1.4' })}</div>
        <div class="mt12">${field('Model', 'Golf', { type: '1.5,0.35', focus: '1.4,2.0' })}</div>
        <div class="row mt12" style="gap:10px"><div class="sp">${field('Year', '2021', { type: '2.1,0.35', focus: '2.0,2.6' })}</div><div class="sp">${field('Colour', 'White', { type: '2.7,0.4', focus: '2.6,3.2' })}</div></div>
        <div class="mt12">${field('Plate Number', 'AB 12 345', { type: '3.3,0.6', focus: '3.2,4.0' })}</div>
        <div class="card mt12 row"><div class="sp"><div class="b t16">Available Seats</div><div class="t12 muted mt4">Passenger seats you can offer</div></div>${stepper([2, 3], [4.4])}</div>
        <div class="btn mt20" data-tap="5.5">Continue</div>
      </div>`, { enter: 'none' }) +
    scr(6.0, 9.8, `${appbar('', { right: '<span class="badge s">Step 2 of 2</span>' })}
      <div class="content">
        <div style="width:64px;height:64px;border-radius:20px;background:#E9FBC9;display:flex;align-items:center;justify-content:center;color:#167A30">${ic('wallet', '', 'width:32px;height:32px')}</div>
        <div class="h1 mt20">Your MobilePay number</div>
        <div class="muted mt8 t14" style="line-height:1.45">Passengers will send money here after the ride</div>
        <div class="mt24">${field('MobilePay number', '+45 20 12 34 56', { type: '0.7,1.1', focus: '0.6,2.2' })}</div>
        <div class="btn mt24" data-tap="2.6">Save &amp; Finish</div>
        <div class="linkbtn mt16" style="text-align:center">Skip for now</div>
      </div>`) +
    scr(9.8, 99, driverBody({ countAt: 0.4, scroll: '0:0 2.2:0 3.2:330', tapRepost: '3.6' }), { enter: 'fade' }),
    { x: PX, y: PY, attrs: 'data-in="0.1,up"' })}
  `,
});

// ═════════════════════════════════════════════════════════════════════════════
// 01:32–01:53  Post a trip: Daily Commute (rolling window) or One-off (threshold).
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const days = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'];
  const select = `${appbar('Post a Trip')}
    <div class="content">
      <div class="muted t14">Choose the type of trip you want to offer.</div>
      ${[['A', 'Daily Commute', ['Same route, repeating weekly', 'Pick the days you drive', 'Passengers book per ride'], 'Best for commuters', 'repeat'],
         ['B', 'One-off Long Distance', ['Single trip on a chosen date', 'Set a minimum passengers threshold', 'Trip auto-cancels if not met'], 'Best for inter-city journeys', 'route']]
        .map(([m, t, bl, best, icn], i) => `
        <div class="card ${i === 0 ? 'shadow' : ''} mt16" style="border-radius:22px;${i === 0 ? 'border:2px solid #C8F135' : ''}" data-in="${0.2 + i * 0.25}" ${i === 0 ? 'data-tap="2.6"' : ''}>
          <div class="row"><div style="width:44px;height:44px;border-radius:14px;background:${i === 0 ? '#C8F135' : '#0D0D0D'};color:${i === 0 ? '#0D0D0D' : '#C8F135'};display:flex;align-items:center;justify-content:center">${ic(icn)}</div>
            <div class="sp"><span class="badge s">Model ${m}</span><div class="h2" style="margin-top:4px">${t}</div></div></div>
          <div class="col mt12" style="gap:6px">${bl.map(b => `<div class="row t14" style="gap:8px">${ic('check', 'green', 'width:16px;height:16px')}${b}</div>`).join('')}</div>
          <div class="row mt12"><span class="t13 muted sp">${best}</span><span class="btn sm ${i === 0 ? '' : 'dark'}">Choose</span></div>
        </div>`).join('')}
    </div>`;

  const modelA = `${appbar('Daily Commute', { right: '<span class="badge s">Model A</span>' })}
    <div class="abs" style="left:0;right:0;top:56px;bottom:0;overflow:hidden"><div class="content" data-scroll="0:0 2.9:0 3.7:300">
      <div class="b t14 muted">Route</div>
      <div class="mt8">${field('From', 'Roskilde St', { type: '0.2,0.4' })}</div>
      <div class="mt8">${field('To', 'Copenhagen H', { type: '0.6,0.45' })}</div>
      <div class="b t14 muted mt20">Recurring Days</div>
      <div class="daychips mt8">${days.map((d, i) => `<span ${i < 5 ? `data-on="${1.1 + i * 0.22}" data-tap="${1.0 + i * 0.22}"` : ''}>${d}</span>`).join('')}</div>
      <div class="b t14 muted mt20">Departure Time</div>
      <div class="mt8">${field('Departure Time', '07:15', { right: ic('clock', 'muted') })}</div>
      <div class="card mt16 row"><div class="sp"><div class="b t16">Available Seats</div></div>${stepper([3], [])}</div>
      <div class="card mt12">
        <div class="row"><div class="sp"><div class="b t16">Rolling Window</div><div class="t12 muted mt4">days ahead</div></div>${stepper([7, 14], [4.4])}</div>
        <div class="t13 mt12" style="color:#15803D">Trips are created ${sw(4.4, '7', '14')} days ahead and auto-extended.</div>
      </div>
      <div class="card mt12 row" style="background:#F1F8E9;border-color:transparent"><span class="t14 sp">Price preview</span><span class="mono t16">DKK 24,32/seat</span></div>
      <div class="btn mt16" data-tap="7.2">Next: Review Price</div>
      <div style="height:30px"></div>
    </div></div>`;

  const modelB = `${appbar('One-Off Trip', { right: '<span class="badge s">Model B</span>' })}
    <div class="content">
      <div class="b t14 muted">Route</div>
      <div class="mt8">${field('From', 'Copenhagen H')}</div>
      <div class="mt8">${field('To', 'Aarhus C')}</div>
      <div class="b t14 muted mt16">Date &amp; Time</div>
      <div class="row mt8" style="gap:10px"><div class="sp">${field('Date', 'Fri 16 Oct', { right: ic('cal', 'muted') })}</div><div class="sp">${field('Time', '07:30', { right: ic('clock', 'muted') })}</div></div>
      <div class="card mt16 row"><div class="sp"><div class="b t16">Available Seats</div></div>${stepper([3], [])}</div>
      <div class="card mt12">
        <div class="row"><div class="sp"><div class="b t16">Minimum Passengers</div><div class="t12 muted mt4">needed to run</div></div>${stepper([2, 3], [1.9])}</div>
        <div class="row mt12 t13" style="gap:8px;color:#92600A;background:rgba(251,191,36,.14);padding:10px 12px;border-radius:12px">${ic('users', '', 'width:16px;height:16px')}<span>Trip only runs if at least ${sw(1.9, '2', '3')} passengers book</span></div>
      </div>
      <div class="btn mt20" data-tap="4.4">Next: Review Price</div>
    </div>`;

  const review = `${appbar('Review Price')}
    <div class="content">
      <div class="card">
        <div class="t13 muted b" style="letter-spacing:.06em">TRIP SUMMARY</div>
        ${[['From', 'Copenhagen H'], ['To', 'Aarhus C'], ['Date', 'Fri 16 Oct'], ['Departure', '07:30'], ['Seats offered', '3'], ['Min. passengers', '3']]
          .map(([k, v]) => `<div class="row t14 mt8"><span class="muted sp">${k}</span><span class="b">${v}</span></div>`).join('')}
      </div>
      <div class="card mt12 row">${ic('route', 'green')}<span class="t14 sp">Distance</span>
        <span class="b t16">${sw(0.5, '<span class="muted t13">Calculating route…</span>', '<span data-count="0.5,1.2,0,300">0</span> km')}</span></div>
      <div class="card mt12" data-in="1.7">
        <div class="row"><div class="b t16 sp">Price Breakdown</div><span class="badge l">System-Calculated</span></div>
        <div class="row t14 mt12"><span class="muted sp">Passengers pay / seat</span><span class="mono t16">DKK 228,00</span></div>
        <div class="row t14 mt8"><span class="muted sp">You receive / seat</span><span class="mono t16">DKK 228,00</span></div>
        <div class="divider"></div>
        <div class="row"><span class="b t14 sp">Max. total earnings</span><span class="mono" style="font-size:20px;color:#167A30">DKK 684,00</span></div>
        <div class="t12 muted mt12" style="line-height:1.45">Rates follow SKAT's reimbursement rules (DKK 2.28 / km). Price is set by the system and cannot be changed.</div>
      </div>
      <div class="btn mt20" data-tap="3.9">${sw(4.1, 'Confirm &amp; Post', `${ic('check')} Trip posted`)}</div>
    </div>`;

  // Calendar strip on the stage: the rolling window keeps adding commute trips.
  const calStrip = `
    <div class="abs" style="left:120px;top:700px;width:860px" data-in="4.1" data-out="10.6">
      <div class="row" style="gap:12px;margin-bottom:14px"><span class="b" style="font-size:16px;color:#C8F135;letter-spacing:.14em">ROLLING WINDOW</span><span style="color:#888;font-size:15px">Roskilde St → Copenhagen H · 07:15</span></div>
      <div style="display:grid;grid-template-columns:repeat(21,1fr);gap:6px">
        ${Array.from({ length: 21 }, (_, i) => {
          const date = 12 + i, wd = i % 7, weekday = wd < 5;
          const at = 4.6 + (i < 7 ? i * 0.08 : i < 14 ? 2.0 + (i - 7) * 0.12 : 4.4 + (i - 14) * 0.12);
          return `<div style="height:86px;border-radius:12px;background:#171717;border:1px solid #262626;display:flex;flex-direction:column;align-items:center;justify-content:space-between;padding:10px 0">
            <span style="font:700 11px Inter;color:#777">${days[wd].slice(0, 2)}</span><span style="font:700 16px Inter;color:#ddd">${date > 31 ? date - 31 : date}</span>
            ${weekday ? `<span style="width:22px;height:8px;border-radius:4px;background:#C8F135" data-in="${at},pop"></span>` : '<span style="height:8px"></span>'}</div>`;
        }).join('')}
      </div>
      <div class="row" style="margin-top:12px;color:#888;font-size:14px">Oct 12<div class="sp"></div><span data-in="6.4">→ auto-extended</span></div>
    </div>`;

  Engine.scene({
    start: 92, end: 113,
    html: `
    ${copy({ y: 200, eyebrow: 'Offer a seat', vo: 'Offer a regular commute that keeps rolling, or post a one-off journey with <em>a minimum number of passengers.</em>', sup: 'Two ways to share a trip' })}
    ${calStrip}
    ${roleTag('drv', 'Driver', PX + 140, PY - 56)}
    ${phone(scr(0, 3.2, select, { enter: 'none' }) + scr(3.2, 11.0, modelA) + scr(11.0, 16.2, modelB) + scr(16.2, 99, review), { x: PX, y: PY, attrs: 'data-in="0.1,up"' })}
    `,
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 01:53–02:08  Driver My Trips: filter, details, edit, repost, stop, complete.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const commuteCard = (date, attrs = '', actions = '') => `<div class="card shadow" style="padding:14px 16px" ${attrs}>
      <div class="row"><span class="badge b">Commute</span><div class="sp"></div><span class="t12 b">2/3 seats</span></div>
      <div class="b t16 mt8">Roskilde St → Copenhagen H</div><div class="t13 muted mt4">Departs ${date} · 07:15</div>${actions}</div>`;
  const longCard = (attrs = '', time = '07:30') => `<div class="card shadow" style="padding:14px 16px" ${attrs}>
      <div class="row"><span class="badge s">Long Trip</span><span class="badge g">Threshold met</span><div class="sp"></div><span class="t12 b">3/3 seats</span></div>
      <div class="b t16 mt8">Copenhagen H → Aarhus C</div><div class="t13 muted mt4">Departs Fri 16 Oct · ${time}</div>
      <div class="t12 muted mt4">Min 3 to confirm</div></div>`;
  const header = (pastOn = '', tapPast = '', chips = '') => `${appbar('My Trips', { back: false })}
    <div class="content"><div class="tabs"><span class="${pastOn ? '' : 'on'}" ${pastOn ? `data-on="-9,${pastOn.split(',')[0]}"` : ''}>Upcoming</span><span ${pastOn ? `data-on="${pastOn}" data-tap="${tapPast}"` : ''}>Past</span></div>
    ${chips}`;

  const list = `${header('', '', `<div class="row mt12" style="gap:8px"><span class="chip" data-on="-9,1.0 2.0">All</span><span class="chip">Commute</span><span class="chip" data-on="1.0,2.0" data-tap="0.8">Long Trip</span><span class="chip">${ic('cal', '', 'width:14px;height:14px')} Date</span></div>`)}
      <div class="col mt16" style="gap:12px">
        <div data-op="0:1 0.9:1 1.1:0.15 1.9:0.15 2.1:1">${commuteCard('Mon 12 Oct')}</div>
        ${longCard('data-tap="2.6"')}
        <div data-op="0:1 0.9:1 1.1:0.15 1.9:0.15 2.1:1">${commuteCard('Tue 13 Oct')}</div>
      </div></div>${nav('trips')}`;

  const active = `${appbar('Active Trip', { right: `<div class="iconbtn" data-tap="2.7">${ic('edit')}</div>` })}
    <div class="content">
      <div class="card"><div class="row"><span class="badge s">Long Trip</span><span class="badge g">Threshold met</span></div>
        <div class="b t16 mt12">Copenhagen H → Aarhus C</div><div class="t13 muted mt4">Departs: Fri 16 Oct · 07:30</div>
        <div class="progressbar mt12"><i style="width:100%"></i></div><div class="t12 muted mt8">3 of 3 seats booked</div></div>
      ${sectionTitle('Passengers')}
      ${[['SL', '#8E44AD', 'Sofie Larsen', '4.8', '2 seats'], ['MA', '#E67E22', 'Mette Andersen', '4.9', '1 seat']].map(([i, c, n, r, s], k) => `
        <div class="card row ${k ? 'mt12' : ''}" data-in="${0.3 + k * 0.2}">${av(i, c)}<div class="sp"><div class="b t15">${n}</div><div class="row t13 muted" style="gap:4px">${star(true, 13)} ${r} · ${s}</div></div>
          <div class="iconbtn" style="background:var(--input)">${ic('chat')}</div></div>`).join('')}
      <div class="btn dark mt20">Mark Trip Complete</div>
    </div>`;

  const edit = `${appbar('Edit Trip')}
    <div class="content">
      <div class="b t14 muted">Route</div>
      <div class="mt8">${field('From', 'Copenhagen H')}</div><div class="mt8">${field('To', 'Aarhus C')}</div>
      <div class="b t14 muted mt16">Departure</div>
      <div class="row mt8" style="gap:10px"><div class="sp">${field('Date', 'Fri 16 Oct')}</div><div class="sp">${field('Time', '07:45', { type: '0.5,0.5', ph: '07:30', focus: '0.4,1.3' })}</div></div>
      <div class="btn mt24" data-tap="1.4">${sw(1.6, 'Save Changes', `${ic('check')} Saved`)}</div>
    </div>`;

  const past = `${header('0.2', '0.1')}
      <div class="col mt16" style="gap:12px" data-in="0.3">
        <div class="card shadow" style="padding:14px 16px">
          <div class="row"><span class="badge s">Long Trip</span><span class="badge k">Completed</span><div class="sp"></div><span class="mono t14">DKK 462,00</span></div>
          <div class="b t16 mt8">Copenhagen H → Odense St</div><div class="t13 muted mt4">Sat 3 Oct · 09:00 · 3 passengers</div>
          <div class="row mt12"><div class="sp"></div><span class="btn sm dark" data-tap="1.0">${ic('repeat', '', 'width:16px;height:16px')} ${sw(1.2, 'Repost', 'Reposted')}</span></div></div>
        <div class="card" style="padding:14px 16px"><div class="row"><span class="badge b">Commute</span><span class="badge k">Completed</span></div>
          <div class="b t16 mt8">Roskilde St → Copenhagen H</div><div class="t13 muted mt4">Fri 9 Oct · 07:15</div></div>
      </div></div>${nav('trips')}`;

  const stop = `${header()}
      <div class="col mt16" style="gap:12px">${commuteCard('Mon 12 Oct', '', `<div class="row mt12" style="gap:8px"><span class="btn sm soft">${ic('edit', '', 'width:16px;height:16px')} Edit</span><div class="sp"></div><span class="btn sm soft" style="color:#B91C1C" data-tap="0.4">Stop recurring route</span></div>`)}${longCard()}</div></div>
      ${nav('trips')}
      ${scrim(0.6, 2.1)}
      <div class="dialog" data-in="0.65,scale" data-out="2.1"><div class="h2">Stop recurring route?</div>
        <div class="t14 muted mt12" style="line-height:1.45">This will cancel all upcoming trips on this route and stop the rolling window.</div>
        <div class="row mt20" style="justify-content:flex-end;gap:22px"><span class="linkbtn" data-tap="1.8">Keep route</span><span class="b t14" style="color:#B91C1C">Stop route</span></div></div>`;

  const complete = `${appbar('Confirm trip completion')}
    <div class="content" style="padding-top:30px;text-align:center">
      <div class="swap" data-on="1.5">
        <div class="when-off"><div style="width:96px;height:96px;border-radius:48px;margin:0 auto;background:#E9FBC9;display:flex;align-items:center;justify-content:center;color:#167A30">${ic('flag', '', 'width:42px;height:42px')}</div></div>
        <div class="when-on"><div style="width:96px;height:96px;border-radius:48px;margin:0 auto;background:#C8F135;display:flex;align-items:center;justify-content:center">${ic('check', '', 'width:50px;height:50px;stroke-width:3')}</div></div>
      </div>
      <div class="h1 mt24">${sw(1.5, 'Are you sure?', 'Trip completed')}</div>
      <div class="card mt24" style="text-align:left">
        <div class="b t16">Copenhagen H → Aarhus C</div><div class="t13 muted mt4">Fri 16 Oct · 07:30 · 3 passengers</div>
        <div class="row mt12"><span class="sp t14 muted">Status</span>${sw(1.5, '<span class="badge g">Active</span>', '<span class="badge k">Completed</span>')}</div></div>
      <div class="btn mt24" data-tap="1.1">Confirm Complete</div>
      <div class="btn ghost mt12">Cancel</div>
    </div>`;

  Engine.scene({
    start: 113, end: 128,
    html: `
    ${copy({ y: 330, eyebrow: 'Manage your trips', vo: 'Keep trips up to date, see who’s coming, and <em>close the loop</em> when the journey is done.' })}
    ${roleTag('drv', 'Driver', PX + 140, PY - 56)}
    ${phone(scr(0, 3.0, list, { enter: 'none' }) + scr(3.0, 6.2, active) + scr(6.2, 8.4, edit, { enter: 'up' }) + scr(8.4, 10.4, past, { enter: 'fade' }) + scr(10.4, 12.8, stop, { enter: 'fade' }) + scr(12.8, 99, complete), { x: PX, y: PY, attrs: 'data-in="0.1,up"' })}
    <div class="abs" style="left:120px;top:690px" data-in="3.0">
      ${[['Trip details', 3.0], ['Edit', 6.2], ['Repost', 8.4], ['Stop recurring', 10.4], ['Complete', 12.8]].map(([l, t]) =>
        `<span class="chip" style="margin:0 10px 10px 0;height:40px;font-size:15px;background:transparent;color:#888;border-color:#333" data-on="${t - 3.0}"><span>${l}</span></span>`).join('')}
    </div>
    <style>.scene .abs .chip.on{background:#C8F135;color:#0B0B0B;border-color:#C8F135}</style>
    `,
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 02:08–02:24  Passenger & driver: trips, cancellation, chat, notification, trust.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const pTrips = `${appbar('My Trips', { back: false })}
    <div class="content"><div class="tabs"><span data-on="-9,0.8 1.6">Upcoming</span><span data-on="0.8,1.6" data-tap="0.7">Past</span></div>
      <div class="mt16" style="position:relative">
        <div data-vis="-9,0.8 1.6">
          <div class="card shadow" style="padding:14px 16px">
            <div class="row"><span class="badge s">Long Trip</span><span class="badge g">Confirmed</span><div class="sp"></div><span class="t12 b">2 seats</span></div>
            <div class="b t16 mt8">Copenhagen H → Aarhus C</div><div class="t13 muted mt4">Fri 16 Oct · 07:30 · Anders Nielsen</div>
            <div class="row mt12" style="gap:8px"><span class="btn sm soft" style="flex:1" data-tap="2.0">Cancel</span><span class="btn sm dark" style="flex:1" data-tap="4.3">${ic('chat', '', 'width:16px;height:16px')} Chat</span></div></div>
          <div class="card mt12" style="padding:14px 16px"><div class="row"><span class="badge b">Commute</span><span class="badge g">Confirmed</span></div>
            <div class="b t16 mt8">Roskilde St → Copenhagen H</div><div class="t13 muted mt4">Mon 12 Oct · 07:15</div></div>
        </div>
      </div>
      <div class="abs" style="left:18px;right:18px;top:80px" data-vis="0.8,1.6">
        <div class="card" style="padding:14px 16px"><div class="row"><span class="badge k">Completed</span><div class="sp"></div><span class="mono t14">DKK 154,00</span></div>
          <div class="b t16 mt8">Copenhagen H → Odense St</div><div class="t13 muted mt4">Sat 3 Oct · paid</div></div>
      </div>
    </div>${nav('trips')}
    ${scrim(2.2, 3.7)}
    <div class="dialog" data-in="2.25,scale" data-out="3.7"><div class="h2">Cancel Booking?</div>
      <div class="t14 muted mt12" style="line-height:1.45">Copenhagen H → Aarhus C · Fri 16 Oct · 2 seats</div>
      <div class="row mt20" style="justify-content:flex-end;gap:22px"><span class="linkbtn" data-tap="3.4">Keep Booking</span><span class="b t14" style="color:#B91C1C">Yes, Cancel</span></div></div>`;

  const chat = (who, peer, peerAv, messages, typeAt) => `
    ${appbar(`<div class="row">${peerAv}<div><div class="b t15">${peer}</div><div class="t11" style="color:#15803D">● Connected</div></div></div>`)}
    <div class="content">
      <div class="badge s" style="margin:0 auto;display:flex;width:max-content">${ic('car', '', 'width:12px;height:12px')} Copenhagen H → Aarhus C · Fri 16 Oct</div>
      <div class="col mt16" style="gap:10px">${messages.map(([mine, txt, at]) => `<div class="msg ${mine ? 'me' : 'them'}" data-in="${at},${mine ? 'left' : 'right'}">${txt}</div>`).join('')}</div>
    </div>
    <div class="abs row" style="left:12px;right:12px;bottom:26px;background:var(--input);border-radius:24px;padding:8px 8px 8px 18px">
      <div class="sp t15" style="white-space:nowrap;overflow:hidden" ${typeAt ? `data-vis="-9,${+typeAt.split(',')[0] + 1.5}" data-type="${typeAt}" data-text="Hi Anders! Could you pick me up at the main entrance?" data-ph="Type a message…"` : ''}><span class="ph">Type a message…</span></div>
      ${typeAt ? `<div class="sp t15" data-vis="${+typeAt.split(',')[0] + 1.5}"><span class="ph">Type a message…</span></div>` : ''}
      <div class="iconbtn" style="background:#167A30;color:#fff;width:38px;height:38px" ${typeAt ? `data-tap="${+typeAt.split(',')[0] + 1.4}"` : ''}>${ic('send', '', 'width:18px;height:18px')}</div></div>`;

  const pChat = chat('p', 'Anders Nielsen', AN(34), [
    [true, 'Hi Anders! Could you pick me up at the main entrance?', 2.2],
    [false, 'Of course — see you at 07:30 by the main entrance 👍', 3.9],
  ], '0.6,1.2');

  const otherProfile = `${appbar('Profile', { right: `<div class="iconbtn" data-tap="1.9">${ic('more')}</div>` })}
    <div class="content" style="text-align:center">
      <div style="display:flex;justify-content:center">${av('AN', '#1E88E5', 84)}</div>
      <div class="h2 mt12">Anders Nielsen</div>
      <div class="row t14 muted" style="justify-content:center;gap:6px;margin-top:6px">${stars(5, 15)} 4.9 · 38 ratings</div>
      <div class="row mt12" style="justify-content:center;gap:8px"><span class="badge g">${ic('shield', '', 'width:12px;height:12px')} Phone verified</span><span class="badge s">Volkswagen Golf</span></div>
      <div class="tabs mt16"><span class="on">As driver</span><span>As passenger</span></div>
      <div class="card mt12" style="text-align:left"><div class="row">${stars(5, 13)}<div class="sp"></div><span class="t12 muted">Sofie L.</span></div><div class="t14 mt8">Super smooth ride, very punctual.</div></div>
      <div class="card mt12" style="text-align:left"><div class="row">${stars(5, 13)}<div class="sp"></div><span class="t12 muted">Tobias B.</span></div><div class="t14 mt8">Comfortable car and good music.</div></div>
    </div>
    <div class="card shadow abs" style="right:14px;top:96px;padding:6px;z-index:20;width:190px" data-in="2.1,scale" data-out="2.9">
      <div class="row t14" style="padding:10px;color:#B91C1C" data-tap="2.6">${ic('flag', '', 'width:18px;height:18px')} Report user</div></div>
    ${scrim(3.0)}
    <div class="sheet" data-in="3.0,up"><div class="h2">Report user</div>
      <div class="t13 muted mt8" style="line-height:1.45">Describe the issue. Our team will review your report within 24 hours.</div>
      <div class="mt16">${field('Reason', '<span class="ph">Select a reason</span>')}</div>
      <div class="btn danger mt16">Submit</div></div>`;

  const notifs = `${appbar('Notifications')}
    <div class="content"><div class="col" style="gap:10px">
      ${[['chat', 'New message from Sofie', 'Hi Anders! Could you pick me up at the main entrance?', 'now', true],
         ['check', 'Trip is a go!', 'Your Model B trip has reached the minimum passenger threshold.', '09:12', false],
         ['users', 'Booking confirmed', 'Sofie booked 2 seats on Copenhagen H → Aarhus C.', '09:12', false],
         ['star', 'New rating', 'Lars rated you 5 stars as a passenger. Great ride!', 'Yesterday', false]]
        .map(([i, t, b, w, unread], k) => `<div class="card row" style="align-items:flex-start;${unread ? 'background:#F1F8E9;border-color:#DDEFC5' : ''}" data-in="${0.2 + k * 0.12}">
          <div style="width:40px;height:40px;border-radius:20px;background:${unread ? '#C8F135' : 'var(--input)'};display:flex;align-items:center;justify-content:center;flex:none">${ic(i, '', 'width:18px;height:18px')}</div>
          <div class="sp"><div class="row"><span class="b t14 sp">${t}</span><span class="t11 muted">${w}</span></div><div class="t13 muted mt4" style="line-height:1.4">${b}</div></div></div>`).join('')}
    </div></div>`;

  const ownProfile = `${appbar('Profile', { back: false, right: `<div class="iconbtn">${ic('settings')}</div>` })}
    <div class="content">
      <div class="row">${av('AN', '#1E88E5', 64)}<div class="sp"><div class="h2">Anders Nielsen</div><div class="row t13 muted" style="gap:4px">${stars(5, 13)} 4.9 · 38 ratings</div></div></div>
      <div class="row mt12" style="gap:8px"><span class="badge g">${ic('shield', '', 'width:12px;height:12px')} Phone verified</span></div>
      <div class="card mt16"><div class="b t15">Car details</div>
        <div class="row t14 mt8"><span class="muted sp">Make / Model</span><span class="b">Volkswagen Golf</span></div>
        <div class="row t14 mt8"><span class="muted sp">Colour</span><span class="b">White</span></div>
        <div class="row t14 mt8"><span class="muted sp">Plate</span><span class="b">AB 12 345</span></div></div>
      <div class="card mt12"><div class="b t15">MobilePay number</div><div class="row mt8"><span class="mono t16 sp">+45 20 12 34 56</span>${ic('edit', 'muted')}</div>
        <div class="t12 muted mt4">Passengers will send money here after the ride</div></div>
      ${sectionTitle('Recent reviews')}
      <div class="card"><div class="row">${stars(5, 13)}<div class="sp"></div><span class="t12 muted">Emma L.</span></div><div class="t14 mt8">Very clean car, great conversation!</div></div>
    </div>${nav('profile')}`;

  Engine.scene({
    start: 128, end: 144,
    html: `
    ${copy({ y: 300, w: 760, eyebrow: 'Stay in touch', vo: 'Plans can change. Stay in touch about the booking, keep track of updates, and <em>build trust</em> through profiles and ratings.' })}
    ${roleTag('pass', 'Passenger', DUO[0].x + 100, DUO[0].y - 56)}
    ${roleTag('drv', 'Driver', DUO[1].x + 115, DUO[1].y - 56)}
    ${phone(scr(0, 4.5, pTrips, { enter: 'none' }) + scr(4.5, 10.2, pChat) + scr(10.2, 99, otherProfile), { x: DUO[0].x, y: DUO[0].y, zoom: 0.8, attrs: 'data-in="0.1,up"' })}
    ${phone(scr(0, 8.6, driverBody({}), { enter: 'none' }) +
      `<div class="ctx" data-from="6.7"><div class="notif" data-in="0,down" data-out="1.8">${SL(38)}<div class="sp"><div class="row"><span class="b t14 sp">New message from Sofie</span><span class="t11 muted">now</span></div><div class="t13 muted mt4">Hi Anders! Could you pick me up at the…</div></div></div></div>` +
      scr(8.6, 11.6, notifs, { enter: 'fade' }) + scr(11.6, 99, ownProfile, { enter: 'fade' }),
      { x: DUO[1].x, y: DUO[1].y, zoom: 0.8, attrs: 'data-in="0.3,up"' })}
    `,
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 02:24–02:41  Settlement: pay directly via MobilePay, mark paid, driver confirms.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const pPay = `${appbar('Pay Your Driver')}
    <div class="content">
      <div class="card" style="text-align:center;background:#F1F8E9;border-color:transparent">
        <div class="t13 muted">Amount to send</div><div class="mono mt4" style="font-size:36px">DKK 456,00</div>
        <div class="t12 muted mt8">SKAT-suggested rate - send directly to driver MobilePay</div></div>
      <div class="card mt12"><div class="t13 muted">Driver MobilePay</div>
        <div class="row mt8">${AN(40)}<div class="sp"><div class="b t15">Anders Nielsen</div><div class="mono t15 mt4">+45 20 12 34 56</div></div>
          <span class="btn sm soft" data-tap="1.2">${ic('copy', '', 'width:16px;height:16px')} ${sw(1.35, 'Copy', 'Copied!')}</span></div></div>
      <div class="card mt12" style="padding:12px 14px" data-vis="6.1">
        <div class="swap" data-on="9.4">
          <div class="when-off row t13" style="color:#92600A">${ic('clock')} You marked this as paid - waiting for driver to confirm</div>
          <div class="when-on row t13" style="color:#15803D">${ic('check')} Driver confirmed receipt <span class="badge g" style="margin-left:auto">Confirmed</span></div>
        </div></div>
      <div data-vis="-9,6.1">
        <div class="btn mp mt16" data-tap="2.4">Open MobilePay</div>
        <div class="btn mt12" data-tap="5.8">I Have Paid</div>
        <div class="btn ghost mt12">I Haven't Paid Yet</div>
      </div>
    </div>
    <div class="abs" style="inset:0;background:#fff;z-index:30;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:18px;text-align:center;padding:30px" data-in="2.7,fade" data-out="5.0">
      <div style="width:84px;height:84px;border-radius:24px;background:#5A78FF;color:#fff;display:flex;align-items:center;justify-content:center">${ic('wallet', '', 'width:40px;height:40px')}</div>
      <div class="h2">Paying in MobilePay</div>
      <div class="muted t14" style="line-height:1.45">DKK 456,00 to Anders Nielsen<br>+45 20 12 34 56</div>
      <div class="row t13" style="color:#15803D;gap:6px" data-in="3.9">${ic('check', '', 'width:16px;height:16px')} Payment sent — back to Ridly</div>
    </div>`;

  const dPay = `${appbar('Payment Confirmation')}
    <div class="content">
      <div class="card"><div class="b t16">Copenhagen H → Aarhus C</div><div class="t13 muted mt4">Fri 16 Oct · Completed</div>
        <div class="row mt12"><span class="t14 muted sp">Total earnings</span><span class="mono t16">DKK 684,00</span></div></div>
      ${sectionTitle('Passengers')}
      <div class="card">
        <div class="row">${SL(40)}<div class="sp"><div class="b t15">Sofie Larsen</div><div class="t13 muted">2 seats · DKK 456,00</div></div>
          <span class="swap" data-on="6.3"><span class="when-off badge s">Waiting</span><span class="when-on">${sw(9.2, '<span class="badge y">Marked paid</span>', '<span class="badge g">Confirmed</span>')}</span></span></div>
        <div data-vis="6.3,9.4"><div class="btn mt12" data-in="6.4" data-tap="8.9">Confirm Received</div></div>
      </div>
      <div class="card mt12" data-on="12.3" style="position:relative">
        <div class="row">${MA(40)}<div class="sp"><div class="b t15">Mette Andersen</div><div class="t13 muted">1 seat · DKK 228,00</div></div>
          ${sw(12.3, '<span class="badge s">Waiting</span>', '<span class="badge r">Dispute submitted</span>')}</div>
      </div>
    </div>`;

  Engine.scene({
    start: 144, end: 161,
    html: `
    ${copy({ y: 260, w: 760, eyebrow: 'After the ride', vo: 'After the ride, passengers pay the driver directly through MobilePay. <em>Both sides can keep the settlement status clear.</em>', sup: 'Direct MobilePay payment · Confirmed in Ridly' })}
    ${roleTag('pass', 'Passenger', DUO[0].x + 100, DUO[0].y - 56)}
    ${roleTag('drv', 'Driver', DUO[1].x + 115, DUO[1].y - 56)}
    ${phone(scr(0, 99, pPay, { enter: 'none' }), { x: DUO[0].x, y: DUO[0].y, zoom: 0.8, attrs: 'data-in="0.1,up"' })}
    ${phone(scr(0, 99, dPay, { enter: 'none' }), { x: DUO[1].x, y: DUO[1].y, zoom: 0.8, attrs: 'data-in="0.3,up"' })}
    <div class="abs" style="left:${DUO[1].x + 16}px;top:${DUO[1].y + 282}px;width:302px;height:78px;border:2px dashed #FBBF24;border-radius:18px;z-index:5" data-in="12.2,scale"></div>
    <div class="role-tag" style="left:${DUO[1].x + 82}px;top:${DUO[1].y + 376}px;background:#FBBF24;color:#0B0B0B;border-color:#FBBF24;z-index:5" data-in="12.4,up">Alternative state</div>
    `,
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 02:41–02:52  Mutual ratings, monthly tax dashboard, annual report.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const rate = (title, q, initials, color, tapsAt, comment) => `${appbar(title)}
    <div class="content" style="text-align:center;padding-top:20px">
      <div style="display:flex;justify-content:center">${av(initials, color, 84)}</div>
      <div class="h2 mt16">${q}</div>
      <div class="row mt20" style="justify-content:center;gap:8px">
        ${[0, 1, 2, 3, 4].map(i => `<span class="swap" data-on="${tapsAt + i * 0.18}" data-tap="${tapsAt - 0.05 + i * 0.18}" style="position:relative"><span class="when-off">${star(false, 40)}</span><span class="when-on">${star(true, 40)}</span></span>`).join('')}
      </div>
      <div class="field mt24" style="text-align:left;min-height:90px"><div class="v" style="white-space:normal" data-type="${tapsAt + 1.1},1.0" data-text="${comment}" data-ph="Add a comment (optional)"></div></div>
      <div class="btn mt20" data-tap="${tapsAt + 2.4}">Submit Rating</div>
      <div class="linkbtn mt16">Maybe later</div>
    </div>`;

  const months = 'October 2026';
  const tax = `${appbar('Tax Dashboard')}
    <div class="content">
      <div class="row" style="justify-content:center;gap:18px"><div class="iconbtn" style="background:var(--input)">${ic('back')}</div><span class="b t16">${months}</span><div class="iconbtn" style="background:var(--input)">${ic('fwd')}</div></div>
      <div class="card mt16" style="background:#0D0D0D;color:#fff;border-color:#0D0D0D;text-align:center">
        <div class="t13" style="color:#B3B3B3">Your estimated tax this month is</div>
        <div class="mono mt8" style="font-size:34px;color:#C8F135">DKK <span data-count="0.3,1.1,0,341.66,2">0</span></div></div>
      <div class="card mt12">
        <div class="row t14"><span class="muted sp">Gross earnings</span><span class="mono t15">DKK 1.140,00</span></div>
        <div class="row t14 mt8"><span class="muted sp">Befordringsfradrag</span><span class="mono t15">− DKK 216,60</span></div>
        <div class="divider"></div>
        <div class="row t14"><span class="b sp">Taxable amount</span><span class="mono t15">DKK 923,40</span></div></div>
      <div class="t12 muted mt12" style="line-height:1.45">Based on DKK 2.28/km — the SKAT 2026 rate. Estimates only — verify with SKAT.</div>
      <div class="btn mt16" data-tap="2.6">${ic('down')} Download Annual Report 2026</div>
    </div>`;

  const report = `${appbar('Annual Tax Report')}
    <div class="content" style="text-align:center;padding-top:20px">
      <div style="width:96px;height:96px;border-radius:26px;margin:0 auto;background:#E9FBC9;display:flex;align-items:center;justify-content:center;color:#167A30">${ic('doc', '', 'width:46px;height:46px')}</div>
      <div class="h2 mt16">Your report is ready</div><div class="muted t14 mt4">Tax year 2026</div>
      <div class="card mt20" style="text-align:left">
        <div class="row t14"><span class="muted sp">Total Earnings</span><span class="mono t15">DKK 9.880</span></div>
        <div class="row t14 mt8"><span class="muted sp">Total Deduction</span><span class="mono t15">DKK 1.877</span></div>
        <div class="row t14 mt8"><span class="b sp">Total Taxable</span><span class="mono t15">DKK 8.003</span></div></div>
      <div class="btn mt20" data-tap="1.6">${ic('down')} ${sw(1.8, 'Download PDF', 'Downloaded')}</div>
      <div class="btn ghost mt12">Share</div>
    </div>`;

  Engine.scene({
    start: 161, end: 172,
    html: `
    ${copy({ y: 270, w: 760, eyebrow: 'Rate & review', vo: 'Then leave a rating. Drivers can also review monthly earnings and <em>download an annual tax report.</em>', sup: 'Tax figures are estimates. Verify with SKAT.' })}
    ${roleTag('pass', 'Passenger', DUO[0].x + 100, DUO[0].y - 56)}
    ${roleTag('drv', 'Driver', DUO[1].x + 115, DUO[1].y - 56)}
    ${phone(scr(0, 99, rate('Rate your trip', 'How was your trip with Anders Nielsen?', 'AN', '#1E88E5', 0.6, 'Smooth ride and great company!'), { enter: 'none' }), { x: DUO[0].x, y: DUO[0].y, zoom: 0.8, attrs: 'data-in="0.1,up"' })}
    ${phone(scr(0, 3.6, rate('Rate your passenger', 'How was Sofie Larsen as a passenger?', 'SL', '#8E44AD', 0.4, 'On time and friendly.'), { enter: 'none' }) + scr(3.6, 7.4, tax) + scr(7.4, 99, report),
      { x: DUO[1].x, y: DUO[1].y, zoom: 0.8, attrs: 'data-in="0.3,up"' })}
    `,
  });
})();

// ═════════════════════════════════════════════════════════════════════════════
// 02:52–03:00  Calm close: account & support cards → two riders → logo.
// ═════════════════════════════════════════════════════════════════════════════
(() => {
  const cards = [
    ['user', 'Profile', 'Ratings, reviews, car details and your MobilePay number.'],
    ['settings', 'Settings', 'Notifications, password and account.'],
    ['help', 'Help Centre', '<span style="display:block;background:var(--input);border-radius:10px;padding:8px 10px;margin-top:4px;color:#0D0D0D">' + ic('search', 'muted', 'width:14px;height:14px;vertical-align:-2px') + ' <span data-type="0.8,0.6" data-text="payment" data-ph="Search help articles…"></span></span>'],
    ['shield', 'Privacy Policy', 'How Ridly handles your data.'],
    ['doc', 'Terms of Service', 'The rules of the road.'],
  ];
  const pos = [[150, 250], [490, 200], [830, 260], [1170, 200], [1510, 250]];
  Engine.scene({
    start: 172, end: 180,
    html: `
    <div class="ctx" data-from="0">
      ${cards.map(([i, t, s], k) => `<div class="fcard" style="left:${pos[k][0]}px;top:${pos[k][1]}px;width:270px" data-in="${0.1 + k * 0.15},up" data-out="2.9">
        <div class="icw">${ic(i)}</div><div class="ttl">${t}</div><div class="sub">${s}</div></div>`).join('')}
    </div>
    ${phone(scr(0, 99, passengerBody({ banner: false }), { enter: 'none' }), { x: 300, y: 250, zoom: 0.6, attrs: 'data-in="3.0,up" data-op="0:1 5.2:1 5.8:0"' })}
    ${phone(scr(0, 99, driverBody({}), { enter: 'none' }), { x: 1370, y: 250, zoom: 0.6, attrs: 'data-in="3.2,up" data-op="0:1 5.2:1 5.8:0"' })}
    <svg class="abs" viewBox="0 0 1920 1080" style="inset:0;width:1920px;height:1080px" data-op="0:1 5.3:1 5.9:0">
      <path d="M560 520 C 760 520, 760 400, 960 400 S 1160 520, 1360 520" fill="none" stroke="#C8F135" stroke-width="6" stroke-linecap="round" pathLength="1" data-draw="3.6,1.4" style="filter:drop-shadow(0 0 12px rgba(200,241,53,.6))"/>
    </svg>
    <div class="abs" style="left:0;right:0;top:250px;text-align:center" data-in="5.6,scale">${logo(96, '#fff')}</div>
    <div class="abs" style="left:0;right:0;top:420px;text-align:center;font:700 64px/1.1 Syne;color:#fff" data-in="5.9,up">Different journeys. <span style="color:#C8F135">One seat closer.</span></div>
    <div class="abs" style="left:0;right:0;top:530px;text-align:center" data-in="6.3,up"><span class="super" style="margin:0;font-size:28px">Find your ride. Share your route.</span></div>
    <div class="abs row" style="left:0;right:0;top:660px;justify-content:center;gap:22px" data-in="6.6,up">
      <span class="store"><svg viewBox="0 0 24 24" style="width:30px;height:30px"><path fill="#0B0B0B" d="M16.4 12.6c0-2.4 2-3.5 2-3.6-1.1-1.6-2.8-1.8-3.4-1.8-1.4-.2-2.8.9-3.5.9-.7 0-1.9-.8-3-.8-1.6 0-3 .9-3.8 2.3-1.6 2.8-.4 7 1.2 9.3.8 1.1 1.7 2.4 2.9 2.3 1.2 0 1.6-.7 3-.7s1.8.7 3 .7c1.3 0 2.1-1.1 2.8-2.3.9-1.3 1.3-2.6 1.3-2.6s-2.5-1-2.5-3.7zM14.2 5.6c.6-.8 1.1-1.9 1-3-.9 0-2.1.6-2.7 1.4-.6.7-1.1 1.8-1 2.9 1 .1 2.1-.5 2.7-1.3z"/></svg><span><small>Under development</small>App Store</span></span>
      <span class="store"><svg viewBox="0 0 24 24" style="width:28px;height:28px"><path fill="#34A853" d="M3.6 2.3 13.3 12l-9.7 9.7c-.3-.2-.5-.6-.5-1V3.3c0-.4.2-.8.5-1z"/><path fill="#FBBC04" d="m16.6 15.3-3.3-3.3 3.3-3.3 3.8 2.2c.9.5.9 1.8 0 2.3z"/><path fill="#EA4335" d="M16.6 15.3 13.3 12l-9.7 9.7c.4.3.9.3 1.4.1z"/><path fill="#4285F4" d="M16.6 8.7 5 2.2c-.5-.2-1-.2-1.4.1l9.7 9.7z"/></svg><span><small>Coming soon</small>Google Play</span></span>
    </div>
    `,
  });
})();
