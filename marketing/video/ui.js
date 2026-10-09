/* Small HTML builders that mirror the app's Compose components. */
const UI = (() => {
  const P = {
    home: '<path d="M3 10.5 12 3l9 7.5V21h-6v-6H9v6H3z"/>',
    car: '<path d="M5 11l1.6-4.4A2 2 0 0 1 8.5 5.3h7a2 2 0 0 1 1.9 1.3L19 11"/><rect x="3" y="11" width="18" height="6" rx="2"/><path d="M6 17v2M18 17v2"/><circle cx="7.5" cy="14" r=".6"/><circle cx="16.5" cy="14" r=".6"/>',
    chat: '<path d="M21 12a8.5 8.5 0 0 1-12.3 7.6L3.5 21l1.4-4.8A8.5 8.5 0 1 1 21 12z"/>',
    user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
    users: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0"/><path d="M16 4.6a3.5 3.5 0 0 1 0 6.8M18 13.6a6.5 6.5 0 0 1 3.5 6.4"/>',
    bell: '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.9 1.9 0 0 0 3.4 0"/>',
    search: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
    swap: '<path d="M7 4v16M7 4 3.5 7.5M7 4l3.5 3.5M17 20V4m0 16-3.5-3.5M17 20l3.5-3.5"/>',
    cal: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/>',
    back: '<path d="M15 18l-6-6 6-6"/>',
    fwd: '<path d="M9 18l6-6-6-6"/>',
    check: '<path d="M5 12.5l4.5 4.5L19 7.5"/>',
    pin: '<path d="M12 21s-7-6.2-7-11a7 7 0 0 1 14 0c0 4.8-7 11-7 11z"/><circle cx="12" cy="10" r="2.5"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    minus: '<path d="M5 12h14"/>',
    copy: '<rect x="9" y="9" width="11" height="11" rx="2"/><path d="M5 15V6a2 2 0 0 1 2-2h9"/>',
    flag: '<path d="M5 21V4h11l-1.8 4L16 12H5"/>',
    more: '<circle cx="12" cy="5" r="1.3"/><circle cx="12" cy="12" r="1.3"/><circle cx="12" cy="19" r="1.3"/>',
    send: '<path d="M21 3 10 14M21 3l-7 18-4-7-7-4z"/>',
    down: '<path d="M12 3v12m0 0-4.5-4.5M12 15l4.5-4.5M4 21h16"/>',
    leaf: '<path d="M5 19c0-8 5-14 15-14 0 10-6 15-14 15"/><path d="M5 19l8-8"/>',
    shield: '<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/><path d="M8.5 12l2.5 2.5 4.5-4.5"/>',
    repeat: '<path d="M17 2l4 4-4 4"/><path d="M3 11V9a3 3 0 0 1 3-3h15"/><path d="M7 22l-4-4 4-4"/><path d="M21 13v2a3 3 0 0 1-3 3H3"/>',
    edit: '<path d="M4 20h4L19 9l-4-4L4 16z"/><path d="M13.5 6.5l4 4"/>',
    phone: '<path d="M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2"/>',
    doc: '<path d="M14 3H6v18h12V7z"/><path d="M14 3v4h4M9 12h6M9 16h6"/>',
    help: '<circle cx="12" cy="12" r="9"/><path d="M9.5 9.5a2.5 2.5 0 1 1 3.5 2.3c-.6.3-1 .9-1 1.6V14"/><circle cx="12" cy="17.3" r=".4"/>',
    lock: '<rect x="4" y="11" width="16" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/>',
    settings: '<path d="M4 7h9M17 7h3M4 17h3M11 17h9"/><circle cx="15" cy="7" r="2"/><circle cx="9" cy="17" r="2"/>',
    filter: '<path d="M4 5h16l-6 7.5V19l-4 2v-8.5z"/>',
    x: '<path d="M6 6l12 12M18 6 6 18"/>',
    wallet: '<rect x="3" y="6" width="18" height="14" rx="2"/><path d="M3 10h18M16 15h2"/>',
    eye: '<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/>',
    mail: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 7 9 6 9-6"/>',
    trend: '<path d="M3 17l6-6 4 4 8-8"/><path d="M15 7h6v6"/>',
    route: '<circle cx="6" cy="18" r="2.5"/><circle cx="18" cy="6" r="2.5"/><path d="M8.5 18H15a3.5 3.5 0 0 0 0-7H9a3.5 3.5 0 0 1 0-7h6.5"/>',
    star: '<path d="M12 2.8l2.8 5.8 6.4.9-4.6 4.5 1.1 6.4L12 17.4l-5.7 3 1.1-6.4-4.6-4.5 6.4-.9z"/>',
    seat: '<path d="M7 4v9a2 2 0 0 0 2 2h7"/><path d="M6 19h10l1.5-4"/><path d="M9 15v4"/>',
    sun: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>',
  };
  const ic = (n, cls = '', style = '') =>
    `<svg class="i ${cls}" viewBox="0 0 24 24" style="${style}">${P[n]}</svg>`;
  const star = (filled = true, size = 15) =>
    `<svg viewBox="0 0 24 24" style="width:${size}px;height:${size}px;fill:${filled ? '#F5B700' : 'none'};stroke:${filled ? '#F5B700' : '#C8C8C8'};stroke-width:1.6;stroke-linejoin:round">${P.star}</svg>`;
  const stars = (n = 5, size = 15) => `<span class="stars">${[1, 2, 3, 4, 5].map(i => star(i <= n, size)).join('')}</span>`;

  const sbar = (dark = false, time = '7:42') => `
    <div class="sbar" style="color:${dark ? '#fff' : '#0D0D0D'}"><span>${time}</span>
      <span class="ic">
        <svg viewBox="0 0 18 12" style="width:18px;height:12px"><rect x="0" y="8" width="3" height="4" rx="1" fill="currentColor"/><rect x="5" y="5.5" width="3" height="6.5" rx="1" fill="currentColor"/><rect x="10" y="3" width="3" height="9" rx="1" fill="currentColor"/><rect x="15" y="0" width="3" height="12" rx="1" fill="currentColor"/></svg>
        <svg viewBox="0 0 16 12" style="width:16px;height:12px"><path d="M8 11.5 1 4.6a10 10 0 0 1 14 0z" fill="currentColor"/></svg>
        <svg viewBox="0 0 27 13" style="width:27px;height:13px"><rect x=".5" y=".5" width="23" height="12" rx="3.5" fill="none" stroke="currentColor" opacity=".45"/><rect x="2.5" y="2.5" width="17" height="8" rx="2" fill="currentColor"/><rect x="24.5" y="4.5" width="2" height="4" rx="1" fill="currentColor" opacity=".45"/></svg>
      </span></div>`;

  const appbar = (title, { back = true, right = '', tapBack = '' } = {}) => `
    <div class="appbar">${back ? `<div class="iconbtn" ${tapBack ? `data-tap="${tapBack}"` : ''}>${ic('back')}</div>` : '<div style="width:8px"></div>'}
      <div class="t">${title}</div>${right}</div>`;

  const nav = (active = 'home', { chatBadge = 0, tap = {} } = {}) => {
    const items = [['home', 'Home', 'home'], ['trips', 'My Trips', 'car'], ['chat', 'Chat', 'chat'], ['profile', 'Profile', 'user']];
    return `<div class="navbar">${items.map(([k, l, i]) => `
      <div class="it ${k === active ? 'a' : ''}" ${tap[k] ? `data-tap="${tap[k]}"` : ''}><div class="pill">${ic(i)}</div>${l}
      ${k === 'chat' && chatBadge ? `<span class="dot">${chatBadge}</span>` : ''}</div>`).join('')}</div>`;
  };

  const logo = (h = 28, text = '#0B0B0B', bg = '#C5FF45', stroke = '#0B0B0B') => `
    <span style="display:inline-flex;align-items:center;gap:${h * 0.35}px">
      <svg viewBox="0 0 44 44" style="width:${h}px;height:${h}px"><rect width="44" height="44" rx="11" fill="${bg}"/><path d="M14 8 L30 22 L14 36" fill="none" stroke="${stroke}" stroke-width="7" stroke-linecap="round" stroke-linejoin="round"/></svg>
      <span style="font:900 ${h * 0.9}px/1 Nunito;letter-spacing:-0.03em;color:${text}">ridly</span></span>`;

  // kf: keyframes for the lime indicator (0 = Passenger, 84 = Driver); on: when the Driver label is active
  const toggle = (kf = '', on = '', tapP = '', tapD = '') => `<div class="toggle" ${on ? `data-on="${on}"` : ''}>
      <div class="ind" ${kf ? `data-tx="${kf}"` : ''}></div><span ${tapP ? `data-tap="${tapP}"` : ''}>Passenger</span><span ${tapD ? `data-tap="${tapD}"` : ''}>Driver</span></div>`;

  const av = (initials, color = '#167A30', size = 44) =>
    `<div class="av" style="background:${color};width:${size}px;height:${size}px;font-size:${size * 0.36}px">${initials}</div>`;

  const field = (label, value, { type = '', ph = '', focus = '', cls = '', right = '' } = {}) => `
    <div class="field ${cls}" ${focus ? `data-on="${focus}"` : ''}>
      <div class="l">${label}</div>
      <div class="row" style="gap:6px"><div class="v sp" ${type ? `data-type="${type}" data-text="${value}" data-ph="${ph}"` : ''}>${type ? '' : value}</div>${right}</div>
    </div>`;

  const stepper = (values, times, label = '') => {
    // values: [1,2,...]; times: when each subsequent value appears
    const n = values.map((v, i) => `<span class="n" data-vis="${i === 0 ? -99 : times[i - 1]},${i < times.length ? times[i] : 999}">${v}</span>`).join('');
    const plusTaps = times.join(' ');
    return `<div class="stepper"><div class="sb2">${ic('minus')}</div>${n}<div class="sb2" data-tap="${plusTaps}">${ic('plus')}</div>${label}</div>`;
  };

  const phone = (screens, { x, y, zoom = 0.96, cls = '', attrs = '', wake = '' } = {}) => `
    <div class="pwrap ${cls}" style="left:${x}px;top:${y}px" ${attrs}>
      <div class="phone" style="zoom:${zoom}">
        <div class="island"></div>
        <div class="screen">${screens}${wake ? `<div class="sleep" data-op="${wake}"></div>` : ''}</div>
      </div></div>`;

  // `from`/`to` relative to the scene (or parent context); content inside uses screen-local time.
  const scr = (from, to, inner, { enter = 'push', exit = 'left', dark = false, bg = '', time = '7:42' } = {}) => `
    <div class="scr ${dark ? 'dark' : ''}" data-from="${from}" data-to="${to}" data-enter="${enter}" data-exit="${exit}" style="${bg ? `background:${bg}` : ''}">
      ${sbar(dark, time)}<div class="body">${inner}</div></div>`;

  const copy = ({ x = 120, y = 300, w = 660, eyebrow = '', vo = '', sup = '', at = 0.3, out = '' }) => `
    <div class="copy" style="left:${x}px;top:${y}px;width:${w}px">
      ${eyebrow ? `<div class="eyebrow" data-in="${at},right" ${out ? `data-out="${out}"` : ''}>${eyebrow}</div>` : ''}
      <div class="vo" data-in="${at + 0.15}" ${out ? `data-out="${out}"` : ''}>${vo}</div>
      ${sup ? `<div class="super" data-in="${at + 1.1},scale" ${out ? `data-out="${out}"` : ''}>${sup}</div>` : ''}
    </div>`;

  const tripCard = ({ from, to, dep, arr, date = '', price, seats = '', driver = '', rating = '4.9', type = 'Long Trip', initials = 'AN', color = '#1E88E5', extra = '', attrs = '' }) => `
    <div class="card shadow" ${attrs} style="padding:14px 16px">
      <div class="row" style="align-items:flex-start">
        <div class="route sp"><div class="rail"><i></i><b></b><i class="end"></i></div>
          <div class="col" style="gap:14px">
            <div><div class="b t16">${dep} · ${from}</div>${date ? `<div class="t12 muted mt4">${date}</div>` : ''}</div>
            <div class="b t16">${arr ? `${arr} · ` : ''}${to}</div></div></div>
        <div style="text-align:right"><div class="mono t16">DKK ${price}</div><div class="t11 muted mt4">per seat</div></div>
      </div>
      <div class="divider"></div>
      <div class="row">${av(initials, color, 30)}<div class="t13 b">${driver}</div><span class="t12 muted row" style="gap:3px">${star(true, 13)} ${rating}</span>
        <div class="sp"></div>${type ? `<span class="badge ${type === 'Commute' ? 'b' : 's'}">${type}</span>` : ''}${seats ? `<span class="badge g">${seats}</span>` : ''}</div>
      ${extra}
    </div>`;

  const scrim = (at, out = '') => `<div class="scrim" data-in="${at},fade" ${out ? `data-out="${out}"` : ''}></div>`;

  return { ic, P, star, stars, sbar, appbar, nav, logo, toggle, av, field, stepper, phone, scr, copy, tripCard, scrim };
})();
