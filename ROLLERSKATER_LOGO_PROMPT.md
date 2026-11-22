# Prompt: RollerSkater logo design (2026 soft 3D, tomato)

Use Part 1 with Claude, an image generator or a designer to refine the logo. Use Part 2 with Claude Code
to implement the approved logo in the Android project.

---

## Part 1: Design brief

**App:** RollerSkater, an offline-first Android music player (Material 3 / Material You).
**Goal:** a modern 2026 mark that is instantly recognizable at 48 px, calm, premium and original.

**Chosen direction: "soft 3D scalloped wheel".**
- One idea only: a scalloped, 9-lobe "cookie" wheel (Material 3 Expressive shape language) with a recessed
  circular dish in the middle and an embossed rounded play triangle. It reads as wheel + record + play.
- Soft 3D, clay-like: two-stop gradient on the body (light top-left to deep bottom-right), a reversed gradient
  inside the dish for the recessed feel, one soft highlight, one soft double shadow under the wheel so it looks
  like it is rolling on a surface. No hard outlines, no text, no clutter.
- Palette "tomato on peach":
  - background gradient `#FFEFE4` to `#FFD0B8`
  - wheel gradient `#FF8A66` to `#D93A1F`
  - dish gradient `#B82A12` to `#FF7B58`
  - play triangle `#FFF6EF`, shadow tone `#7A1E0A`
  - dark splash: background `#2A0F08`, glyph `#FF8A66`
- Keep contrast of the mark against the background at 3:1 or more; avoid thin details under 1.5% of the canvas.
- Must be original: do not copy or resemble any existing app icon or brand.

**Ideas to push it further (give 3 variations, I pick one):**
1. Make the "rolling" idea stronger: tilt the wheel about 10 degrees, add two short curved motion arcs.
2. Replace the play triangle with a play triangle whose right corner forms a tiny music-note flag.
3. A skate-wheel profile view (hub, bearings) in the same soft 3D style.
4. A version with 2 lobes counts (7 and 11) to compare how "wheel-like" the shape feels.

**Responsive logo family (2026 best practice):**
- Full-color adaptive launcher icon (108x108 dp canvas, artwork inside the 66 dp safe zone).
- Themed monochrome layer: flat single-color silhouette (scalloped body with the dish cut out + triangle).
- Splash mark: simple silhouette, the play triangle as the last, separate path so it can pulse on load.
- Play Store icon 512x512 PNG (full square, no rounded corners, no shadow), feature graphic 1024x500,
  SVG master.
- Light and dark presentation, tested on white, black and a busy wallpaper, in circle, squircle and
  rounded-square masks, at 512, 192, 96 and 48 px.

## Part 2: Implementation prompt (Claude Code)

Implement the approved logo in `com.rollerskater.app`. Edit resources only.

1. `app/src/main/res/drawable/`: `rs_icon_background.xml` (linear gradient vector), `rs_icon_foreground.xml`
   (shadow ellipses, gradient body, gradient dish, highlight, triangle shadow, triangle),
   `new_monochrome.xml` and `rollerskater_base_monochrome.xml` (black silhouette, `evenOdd` dish cutout),
   `splash_icon.xml` (tinted with `@color/splash_glyph`; first path is the body, last path is the triangle,
   because `RollerSkaterSplash.kt` reads the first and last path).
2. `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` point at background, foreground, monochrome.
3. `values*/splash_colors.xml` (light, night, and v31 variants) use the palette above.
4. Export `app/src/main/ic_launcher-playstore.png` (512x512) and `assets/RollerSkater.svg`.
5. Validate: all XML parses, `./gradlew :app:assembleDebug` passes, install on an emulator and check the launcher
   icon, themed icon (Android 13+), splash screen (light and dark) and the About screen.
6. Optional: animate the splash with a gentle scale pulse on the triangle and a slow wheel rotation (under 600 ms).
7. Commit as `design: update RollerSkater logo`. Attach screenshots of every place the logo appears.

If the design is unclear, ask before building. Never use another app's artwork.
