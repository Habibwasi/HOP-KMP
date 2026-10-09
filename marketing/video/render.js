#!/usr/bin/env node
/*
 * Renders index.html frame-by-frame into an MP4.
 *
 *   npm install
 *   node render.js                         → out/hop-story-1080p.mp4 (+ 720p, poster)
 *   node render.js --stills 4,30,60        → out/still-<t>.png for quick review
 *   node render.js --from 36 --to 53       → render only a slice (out/slice-36-53.mp4)
 *
 * Uses the locally installed Chrome (or CHROME_PATH) through puppeteer-core and the
 * ffmpeg binary from ffmpeg-static.
 */
const path = require('path');
const fs = require('fs');
const { spawn } = require('child_process');
const puppeteer = require('puppeteer-core');
const ffmpeg = require('ffmpeg-static');

const args = process.argv.slice(2);
const arg = (name, def) => { const i = args.indexOf('--' + name); return i >= 0 ? args[i + 1] : def; };
const OUT = path.join(__dirname, 'out');
fs.mkdirSync(OUT, { recursive: true });

const chromeCandidates = [
  process.env.CHROME_PATH,
  'C:/Program Files/Google/Chrome/Application/chrome.exe',
  'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/usr/bin/google-chrome',
].filter(Boolean);
const executablePath = chromeCandidates.find(p => fs.existsSync(p));

function run(cmd, cmdArgs) {
  return new Promise((resolve, reject) => {
    const p = spawn(cmd, cmdArgs, { stdio: ['ignore', 'inherit', 'inherit'] });
    p.on('close', c => (c === 0 ? resolve() : reject(new Error(cmd + ' exited ' + c))));
  });
}

(async () => {
  const browser = await puppeteer.launch({
    executablePath,
    headless: true,
    args: ['--allow-file-access-from-files', '--force-device-scale-factor=1', '--hide-scrollbars', '--font-render-hinting=none', '--disable-lcd-text'],
    defaultViewport: { width: 1920, height: 1080, deviceScaleFactor: 1 },
  });
  const page = await browser.newPage();
  page.on('pageerror', e => console.error('page error:', e.message));
  page.on('console', m => { if (m.type() === 'error') console.error('console:', m.text()); });
  await page.goto('file://' + path.join(__dirname, 'index.html').replace(/\\/g, '/') + '?render', { waitUntil: 'load' });
  await page.waitForFunction('window.videoReady === true', { timeout: 30000 });
  const { DURATION, FPS } = await page.evaluate(() => ({ DURATION: window.DURATION, FPS: window.FPS }));

  const stills = arg('stills');
  if (stills) {
    for (const t of stills.split(',').map(Number)) {
      await page.evaluate(t => window.seek(t), t);
      await page.screenshot({ path: path.join(OUT, `still-${t}.png`) });
      console.log('still', t);
    }
    await browser.close();
    return;
  }

  const from = +arg('from', 0), to = +arg('to', DURATION);
  const sliced = from > 0 || to < DURATION;
  const master = path.join(OUT, sliced ? `slice-${from}-${to}.mp4` : 'hop-story-1080p.mp4');
  const enc = spawn(ffmpeg, [
    '-y', '-f', 'image2pipe', '-framerate', String(FPS), '-c:v', 'mjpeg', '-i', '-',
    '-c:v', 'libx264', '-preset', 'slow', '-crf', '18', '-pix_fmt', 'yuv420p', '-profile:v', 'high',
    '-movflags', '+faststart', master,
  ], { stdio: ['pipe', 'ignore', 'inherit'] });
  const done = new Promise((res, rej) => enc.on('close', c => (c === 0 ? res() : rej(new Error('ffmpeg ' + c)))));

  const first = Math.round(from * FPS), last = Math.round(to * FPS);
  const t0 = Date.now();
  for (let f = first; f < last; f++) {
    await page.evaluate(t => window.seek(t), f / FPS);
    const buf = await page.screenshot({ type: 'jpeg', quality: 95, optimizeForSpeed: true });
    if (!enc.stdin.write(buf)) await new Promise(r => enc.stdin.once('drain', r));
    if (f % 150 === 0) {
      const el = (Date.now() - t0) / 1000, n = f - first + 1;
      console.log(`frame ${f}/${last}  ${(f / FPS).toFixed(1)}s  eta ${Math.round(el / n * (last - f))}s`);
    }
  }
  enc.stdin.end();
  await done;
  await browser.close();
  console.log('wrote', master);

  if (!sliced) {
    await run(ffmpeg, ['-y', '-i', master, '-vf', 'scale=1280:720:flags=lanczos', '-c:v', 'libx264', '-preset', 'slow', '-crf', '23',
      '-pix_fmt', 'yuv420p', '-movflags', '+faststart', path.join(OUT, 'hop-story-720p.mp4')]);
    await run(ffmpeg, ['-y', '-ss', '5.5', '-i', master, '-frames:v', '1', path.join(OUT, 'hop-story-poster.jpg')]);
    console.log('wrote 720p + poster');
  }
})().catch(e => { console.error(e); process.exit(1); });
