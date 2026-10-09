# Ridly — landing-page product film

A 3-minute, 1920×1080 motion video built from [MOTION_VIDEO_STORY.md](../../MOTION_VIDEO_STORY.md). The app screens are rebuilt in HTML using the app's own fonts, colours, copy, and pricing rules, and animated on a deterministic timeline, so every frame renders the same way each time.

| File | Purpose |
|---|---|
| `index.html` | Stage. Open it in Chrome to preview in real time (Space = pause, ←/→ = ±5 s, `#t=60` = start at 60 s). |
| `scenes.js` | The 13 storyboard beats, with timings and screen choreography. |
| `ui.js` | HTML versions of the app's Compose components (buttons, fields, trip cards, nav bar, etc.). |
| `engine.js` | Timeline engine. All `data-*` animation attributes are documented at the top of the file. |
| `illustrations.js` | Onboarding illustrations, generated from `hop_onboarding_illustrations.html`. |
| `voiceover.vtt` | Voice-over script with timings, for narration or captions. |
| `render.js` | Renders frames in headless Chrome and encodes them with ffmpeg. |

## Render

```sh
cd marketing/video
npm install
node render.js                      # out/hop-story-1080p.mp4, out/hop-story-720p.mp4, out/hop-story-poster.jpg
node render.js --stills 12,60,150   # PNG stills, for checking a change quickly
node render.js --from 36 --to 53    # render just one section
```

A full render takes roughly 10–15 minutes. The video has no audio. The voice-over lines appear as on-screen copy so the film works when autoplayed muted.

## Landing-page embed

```html
<video autoplay muted loop playsinline preload="metadata" poster="hop-story-poster.jpg">
  <source src="hop-story-1080p.mp4" type="video/mp4">
  <track kind="captions" src="voiceover.vtt" srclang="en" label="English">
</video>
```
