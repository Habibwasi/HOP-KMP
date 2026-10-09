/*
 * Onboarding illustrations, ported 1:1 from OnboardingIllustrations.kt (280×200 canvas).
 * Motion uses CSS keyframes; the engine pins every animation to the global clock.
 */
(() => {
  const ov = (c, x, y, w, h, extra = '') => `<ellipse cx="${x + w / 2}" cy="${y + h / 2}" rx="${w / 2}" ry="${h / 2}" fill="${c}" ${extra}/>`;
  const rr = (c, x, y, w, h, r, extra = '') => `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${r}" fill="${c}" ${extra}/>`;
  const smile = (cx, cy, c) => `<path d="M${cx - 3} ${cy}Q${cx} ${cy + 3} ${cx + 3} ${cy}" fill="none" stroke="${c}" stroke-width="1.2" stroke-linecap="round"/>`;
  const wheel = (cx, cy, r1, r2, r3, outer, mid) =>
    `<circle cx="${cx}" cy="${cy}" r="${r1}" fill="${outer}"/><circle cx="${cx}" cy="${cy}" r="${r2}" fill="${mid}"/><circle cx="${cx}" cy="${cy}" r="${r3}" fill="#B0BEC5"/>`;
  const sparkle = (cx, cy, r, c, cls) => `<g class="${cls}" stroke="${c}" stroke-width="${r * 0.55}" stroke-linecap="round">
      <path d="M${cx} ${cy - r * 2.2}V${cy + r * 2.2}M${cx - r * 2.2} ${cy}H${cx + r * 2.2}"/><circle cx="${cx}" cy="${cy}" r="${r}" fill="${c}" stroke="none"/></g>`;
  const open = '<svg viewBox="0 0 280 200" style="display:block;width:100%;height:100%">';

  // Slide 1 — "Fewer cars. Better journeys."
  const s1 = `${open}<style>
      @keyframes o1Sun { from { transform: scale(1) } to { transform: scale(1.07) } }
      @keyframes o1C1 { from { transform: translateX(0) } to { transform: translateX(8px) } }
      @keyframes o1C2 { from { transform: translateX(8px) } to { transform: translateX(0) } }
      @keyframes o1Car { from { transform: translateY(0) } to { transform: translateY(-6px) } }
      @keyframes o1La { from { transform: translate(0,0) rotate(-10deg) } to { transform: translate(4px,-8px) rotate(10deg) } }
      @keyframes o1Lb { from { transform: translate(0,0) rotate(5deg) } to { transform: translate(-5px,-6px) rotate(-8deg) } }
      .o1Sun { transform-origin: 240px 38px; animation: o1Sun 3s ease-in-out infinite alternate; }
      .o1C1 { animation: o1C1 4s ease-in-out infinite alternate; }
      .o1C2 { animation: o1C2 5s ease-in-out infinite alternate; }
      .o1Car { animation: o1Car 3s ease-in-out infinite alternate; }
      .o1La { transform-origin: 30px 60px; animation: o1La 2.4s ease-in-out infinite alternate; }
      .o1Lb { transform-origin: 240px 70px; animation: o1Lb 2.8s ease-in-out .4s infinite alternate; }
      .o1Lc { transform-origin: 255px 50px; animation: o1La 2.2s ease-in-out .9s infinite alternate; }
    </style>
    <rect width="280" height="200" fill="#E8F5E9"/>
    <g class="o1Sun"><circle cx="240" cy="38" r="22" fill="#FFF9C4"/><circle cx="240" cy="38" r="15" fill="#FFF176"/></g>
    <g class="o1C1">${ov('#fff', 32, 23, 56, 24, 'fill-opacity=".85"')}${ov('#fff', 62, 19, 36, 22, 'fill-opacity=".85"')}${ov('#fff', 28, 23, 28, 18, 'fill-opacity=".85"')}</g>
    <g class="o1C2">${ov('#fff', 138, 18, 44, 20, 'fill-opacity=".7"')}${ov('#fff', 161, 15, 28, 18, 'fill-opacity=".7"')}${ov('#fff', 136, 18, 24, 16, 'fill-opacity=".7"')}</g>
    <rect y="158" width="280" height="42" fill="#A5D6A7"/><rect y="146" width="280" height="20" fill="#78909C"/>
    ${[20, 75, 130, 185, 240].map(x => rr('#ECEFF1', x, 154, 30, 4, 2, 'fill-opacity=".7"')).join('')}
    <rect x="18" y="112" width="7" height="36" fill="#6D4C41"/>${ov('#388E3C', 3, 78, 36, 44)}${ov('#43A047', 8, 76, 26, 32)}
    <rect x="45" y="120" width="6" height="28" fill="#6D4C41"/>${ov('#2E7D32', 34, 92, 28, 36)}${ov('#388E3C', 38, 92, 20, 24)}
    <rect x="232" y="114" width="7" height="34" fill="#6D4C41"/>${ov('#388E3C', 217, 80, 36, 44)}${ov('#43A047', 222, 78, 26, 32)}
    <rect x="256" y="120" width="6" height="28" fill="#6D4C41"/>${ov('#2E7D32', 245, 92, 28, 36)}
    <g class="o1Car">
      ${rr('#26C6DA', 62, 112, 130, 34, 8)}${rr('#00ACC1', 82, 94, 90, 28, 8)}
      ${rr('#E0F7FA', 88, 99, 32, 18, 4, 'fill-opacity=".9"')}${rr('#E0F7FA', 126, 99, 32, 18, 4, 'fill-opacity=".9"')}
      <circle cx="104" cy="106" r="7" fill="#FFCC80"/><circle cx="142" cy="106" r="7" fill="#FFAB91"/>
      ${smile(104, 108, '#E64A19')}${smile(142, 108, '#E64A19')}
      ${rr('#0097A7', 62, 138, 130, 8, 4)}${rr('#FFF9C4', 184, 120, 10, 8, 3)}${rr('#EF9A9A', 64, 120, 8, 8, 3)}
      ${wheel(92, 146, 14, 9, 4, '#37474F', '#546E7A')}${wheel(168, 146, 14, 9, 4, '#37474F', '#546E7A')}
    </g>
    <g class="o1La">${ov('#66BB6A', 20, 54, 20, 12)}</g>
    <g class="o1Lb">${ov('#81C784', 231, 65, 18, 10)}</g>
    <g class="o1Lc">${ov('#A5D6A7', 248, 46, 14, 8)}</g>
  </svg>`;

  // Slide 2 — "Every seat filled matters."
  const s2 = `${open}<style>
      @keyframes o2Bob { from { transform: translateY(0) } to { transform: translateY(-5px) } }
      @keyframes o2Leaf { from { transform: scale(1) } to { transform: scale(1.15) } }
      @keyframes o2Smoke { from { transform: translateY(0); opacity: .7 } to { transform: translateY(-30px); opacity: 0 } }
      @keyframes o2Sp { from { opacity: 0 } to { opacity: 1 } }
      .o2Bob { animation: o2Bob 2.8s ease-in-out infinite alternate; }
      .o2Leaf { transform-origin: 140px 48px; animation: o2Leaf 2s ease-in-out infinite alternate; }
      .o2Smoke { animation: o2Smoke 2.5s linear infinite; }
      .o2Sp1 { animation: o2Sp 1.8s ease-in-out infinite alternate; }
      .o2Sp2 { animation: o2Sp 1.8s ease-in-out .6s infinite alternate; opacity: 0; }
      .o2Sp3 { animation: o2Sp 1.8s ease-in-out 1.2s infinite alternate; opacity: 0; }
    </style>
    <rect width="280" height="200" fill="#F1F8E9"/>
    <rect y="158" width="280" height="42" fill="#C8E6C9"/><rect y="146" width="280" height="18" fill="#90A4AE"/>
    ${[10, 60, 115, 170, 225].map(x => rr('#fff', x, 153, 25, 3, 1.5, 'fill-opacity=".6"')).join('')}
    ${ov('#CFD8DC', 10, 46, 56, 32, 'fill-opacity=".55"')}${ov('#CFD8DC', 6, 46, 32, 24, 'fill-opacity=".55"')}${ov('#CFD8DC', 38, 44, 28, 22, 'fill-opacity=".55"')}
    <g class="o2Smoke"><circle cx="38" cy="38" r="6" fill="#B0BEC5" fill-opacity=".5"/><circle cx="44" cy="34" r="5" fill="#B0BEC5" fill-opacity=".4"/><circle cx="32" cy="36" r="4" fill="#B0BEC5" fill-opacity=".3"/></g>
    <g class="o2Leaf">${ov('#66BB6A', 102, 24, 76, 48)}${ov('#81C784', 112, 32, 56, 32)}
      <path d="M110 48H170M140 30V66" stroke="#388E3C" stroke-width="1.5" stroke-linecap="round"/></g>
    ${sparkle(200, 35, 4, '#FFF176', 'o2Sp1')}${sparkle(220, 55, 3, '#A5D6A7', 'o2Sp2')}${sparkle(178, 28, 3, '#80DEEA', 'o2Sp3')}
    <g class="o2Bob">
      ${rr('#42A5F5', 52, 110, 148, 36, 9)}${rr('#1E88E5', 74, 91, 104, 30, 9)}
      ${[80, 112, 144].map(x => rr('#E3F2FD', x, 97, 26, 18, 4, 'fill-opacity=".92"')).join('')}
      <circle cx="93" cy="104" r="7" fill="#FFCC80"/><circle cx="125" cy="104" r="7" fill="#FFAB91"/><circle cx="157" cy="104" r="7" fill="#CE93D8"/>
      ${smile(93, 106, '#BF360C')}${smile(125, 106, '#BF360C')}${smile(157, 106, '#6A1B9A')}
      ${rr('#1565C0', 52, 138, 148, 8, 4)}${rr('#FFF9C4', 194, 118, 8, 7, 3)}
      ${wheel(90, 148, 13, 8, 3.5, '#37474F', '#546E7A')}${wheel(166, 148, 13, 8, 3.5, '#37474F', '#546E7A')}
    </g>
  </svg>`;

  // Slide 3 — "Move together. Live lighter."
  const stars = [[30, 22, 2, 0], [68, 14, 1.5, 0.4], [110, 30, 2, 0.8], [155, 18, 1.5, 1.2], [185, 35, 2, 1.6], [50, 45, 1.5, 0], [200, 20, 1.5, 0.4]];
  const s3 = `${open}<style>
      @keyframes o3Float { from { transform: translateY(0) } to { transform: translateY(-4px) } }
      @keyframes o3Dash { to { transform: translateX(-60px) } }
      @keyframes o3Tw { from { opacity: .2 } to { opacity: 1 } }
      @keyframes o3Moon { from { opacity: .9 } to { opacity: 1 } }
      .o3Float { animation: o3Float 3s ease-in-out infinite alternate; }
      .o3Dash { animation: o3Dash 1.8s linear infinite; }
      .o3Moon { animation: o3Moon 4s ease-in-out infinite alternate; }
    </style>
    <rect width="280" height="200" fill="#1A237E"/><rect y="100" width="280" height="100" fill="#283593"/>
    ${ov('#00796B', -20, 100, 320, 80, 'fill-opacity=".35"')}
    <g class="o3Moon"><circle cx="230" cy="40" r="24" fill="#FFF9C4" fill-opacity=".15"/><circle cx="230" cy="40" r="18" fill="#FFF9C4" fill-opacity=".2"/></g>
    <circle cx="230" cy="40" r="13" fill="#FFFDE7"/>
    ${stars.map(([x, y, r, d]) => `<circle cx="${x}" cy="${y}" r="${r}" fill="#fff" style="opacity:.2;animation:o3Tw 2s ease-in-out ${d}s infinite alternate"/>`).join('')}
    ${ov('#1B5E20', -15, 110, 110, 70)}${ov('#1B5E20', 185, 118, 110, 60)}${ov('#1B5E20', 60, 124, 160, 56)}
    <rect x="12" y="110" width="6" height="30" fill="#0D3B0D"/><polygon points="15,85 3,118 27,118" fill="#0D3B0D"/>
    <rect x="255" y="112" width="6" height="28" fill="#0D3B0D"/><polygon points="258,88 246,120 270,120" fill="#0D3B0D"/>
    <rect y="148" width="280" height="28" fill="#37474F"/>
    <g class="o3Dash">${[-60, 0, 60, 120, 180, 240, 300].map(x => rr('#ECEFF1', x, 159, 40, 4, 2, 'fill-opacity=".5"')).join('')}</g>
    <g class="o3Float">
      ${rr('#00897B', 68, 118, 130, 34, 9)}${rr('#00695C', 88, 100, 90, 28, 9)}
      ${rr('#B2EBF2', 94, 105, 30, 17, 4, 'fill-opacity=".85"')}${rr('#B2EBF2', 130, 105, 30, 17, 4, 'fill-opacity=".85"')}
      <circle cx="109" cy="112" r="7" fill="#FFCC80"/><circle cx="145" cy="112" r="7" fill="#FFAB91"/>
      ${smile(109, 114, '#E64A19')}${smile(145, 114, '#E64A19')}
      ${rr('#004D40', 68, 144, 130, 8, 4)}${rr('#FFF9C4', 192, 124, 10, 8, 3)}${rr('#EF5350', 68, 124, 8, 8, 3)}
      ${wheel(98, 152, 12, 7, 3, '#263238', '#546E7A')}${wheel(170, 152, 12, 7, 3, '#263238', '#546E7A')}
    </g>
  </svg>`;

  window.ONBOARDING_SVGS = [s1, s2, s3];
})();
