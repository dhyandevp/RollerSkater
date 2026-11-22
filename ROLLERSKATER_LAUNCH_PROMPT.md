# RollerSkater: optimize, harden and launch (prompt for Claude Code)

Paste everything below into Claude Code, run from the repo root with JDK 21 and the Android SDK
installed. Do the work in order, in phases. After each phase: build, run tests, commit, and report.
Ask me before anything irreversible or anything that needs my accounts, keys or money.

---

You are the senior Android engineer, release manager and QA lead for **RollerSkater**
(`com.rollerskater.app`), an offline-first music player (Kotlin, Jetpack Compose, Material 3, Hilt,
Room, Media3). It is a rebranded GPL-3.0 fork. Take it from "compiles" to "published and
maintained". Keep `LICENSE`, `NOTICE.md`, `THIRD_PARTY_NOTICES.md` and `assets/licenses` intact.

## Phase 0: Baseline and safety
1. Read `README.md`, `NOTICE.md`, `ROLLERSKATER_PROMPT.md`. If the feature-removal prompt (Navidrome,
   Jellyfin, Daily Mix) has not been completed, do it first. `./gradlew :app:assembleDebug` must pass.
2. Record baselines before changing anything: APK/AAB size, cold start time, jank, RAM during playback,
   library scan time on 5,000 songs. Save to `docs/perf/baseline.md`. Every later change is judged against it.
3. Create branch `release/1.0.0`. Small commits, imperative messages.

## Phase 1: Performance and battery
- **Startup:** measure with Macrobenchmark (`StartupTimingMetric`). Move non-critical init out of
  `Application.onCreate` (use App Startup / lazy Hilt providers). Target cold start < 600 ms on a mid-range device.
- **Baseline + Startup Profiles:** regenerate the baseline profile for the new package
  (`:baselineprofile` module) covering startup, library scroll, open player, queue. Commit the generated profile.
- **R8 / size:** enable `isMinifyEnabled` and `isShrinkResources`; fix keep rules; remove unused
  dependencies, unused locales/resources (`resourceConfigurations`) and unused ABIs. Compare AAB size to baseline.
- **Compose:** find unstable parameters and needless recompositions with the compiler reports
  (`compose_stability.conf` exists). Use `@Stable`/`@Immutable`, `key`s in lazy lists, `derivedStateOf`,
  `remember`. Make sure big lists use `LazyColumn` with content types.
- **Library scanning:** run on `Dispatchers.IO`, batch Room inserts in transactions, use incremental scans
  (MediaStore generation / date modified), never block the main thread. Add indexes for sort/search columns.
- **Images:** size-limited Coil requests, memory + disk cache caps, downsampled album art, no full-size bitmaps in lists.
- **Playback service:** correct foreground-service type (`mediaPlayback`), audio focus, `BecomingNoisy`,
  wake lock only while needed, release ExoPlayer on stop, handle service restarts and process death
  (restore queue + position).
- **Memory/leaks:** add LeakCanary in debug, fix leaks (context, listeners, flows without lifecycle).
  Run StrictMode in debug and fix disk/network-on-main violations.
- **Battery:** no wakeups when idle; WorkManager constraints; no polling. Verify with Battery Historian / Perfetto.
- **Quality gates:** `./gradlew lint detekt ktlintCheck` (add detekt/ktlint if missing), fix or baseline findings.

## Phase 2: Correctness and QA
- **Tests:** unit tests for repositories, view models, playlist/queue logic, Room migrations
  (`MigrationTestHelper`); instrumented tests for DB + service; Compose UI tests for library, player, settings.
  Aim for meaningful coverage on logic-heavy code, not a vanity number.
- **Device matrix:** run on API 30 and the latest API, phone + tablet + foldable emulator, small and large
  font, dark/light, RTL, landscape. Fix layout and insets issues (edge-to-edge, predictive back, gesture nav).
- **Audio edge cases:** gapless, crossfade, corrupt files, huge files, FLAC/OGG/Opus/WMA, missing tags,
  Bluetooth connect/disconnect, headset buttons, Android Auto / media notification controls, lock-screen controls,
  phone call interruption, low storage.
- **Permissions:** request `READ_MEDIA_AUDIO` / `POST_NOTIFICATIONS` just in time with clear rationale;
  the app must degrade gracefully if denied. Remove every permission the app does not use.
- **Crash resilience:** global exception logging to a local file (no network), ANR checks, process-death restore.
- **Accessibility:** content descriptions, 48 dp touch targets, contrast, TalkBack pass, font scale 200%.
- **Offline-first:** confirm the app makes no network calls unless the user opts in.
  Run a traffic check (mitmproxy or `adb shell dumpsys netstats`) and list any hosts contacted.
- Write findings to `docs/qa/report.md`; fix everything severe; list what is deferred.

## Phase 3: Security and privacy
- Dependency audit (`./gradlew dependencyCheckAnalyze` or OSV scanner); update vulnerable libs.
- No hard-coded secrets, no cleartext traffic (`usesCleartextTraffic=false`), exported components minimal,
  `FileProvider` paths narrow, backups configured (`dataExtractionRules`, `fullBackupContent`).
- **PRIVACY.md and Play "Data safety" answers:** state exactly what is collected (ideally nothing leaves the device).
  Write a plain-language privacy policy, host it (GitHub Pages) and link it in the app's About screen.
- Update `SECURITY.md` with a real contact for vulnerability reports.

## Phase 4: Branding, assets and store listing
- Icon: the 2026 soft 3D tomato scalloped wheel with a play button (already in `res/drawable`, `assets/RollerSkater.svg`; brief in `ROLLERSKATER_LOGO_PROMPT.md`).
  Verify adaptive + themed (monochrome) icon on API 30 and 33+, and the splash screen.
- Create: 512x512 Play icon, 1024x500 feature graphic, 6-8 phone screenshots (+ 7"/10" tablet screenshots),
  optional short promo video. Use the consistent tomato-on-peach soft 3D look. Put sources in `store/`.
- Listing copy (`store/en-US/`): title (<= 30 chars), short description (<= 80), full description (<= 4000),
  release notes. Include keywords naturally: offline music player, local music, lyrics, equalizer, Material You.
- Translate listing and UI strings for the top locales you ship; mark untranslated strings so lint does not fail.

## Phase 5: Release engineering
- **Versioning:** `versionName` 1.0.0, `versionCode` derived from CI run or semver; single source of truth.
- **Signing:** create an upload keystore (store it OUTSIDE the repo, back it up, never commit it); enable
  Play App Signing. Wire `keystore.properties` via env vars for CI. Tell me exactly what to back up.
- **Build:** produce a signed **AAB** (`bundleRelease`) for Play and a signed universal **APK** for GitHub
  Releases / sideloading. Check `targetSdk` meets the current Play requirement. Verify with `bundletool`
  and install the release build on a real device; test with R8 enabled.
- **CI/CD (GitHub Actions):** on PR: build, lint, unit tests. On tag `v*`: build signed AAB + APK, attach to a
  GitHub Release with generated changelog. Cache Gradle. Fix `.github/workflows/*` for the new repo name;
  delete jobs that referenced the upstream project.
- **In-app updater:** it currently points to `github.com/dhyandevp/RollerSkater`. Confirm it works with the real
  repo or remove it if shipping through Play (Play forbids self-updating).
- **Crash/analytics:** none by default. If I want crash reports, propose a privacy-friendly option and ask me.

## Phase 6: Store and licensing compliance
- **GPL-3.0:** distributing the APK means offering the complete corresponding source under GPL-3.0. Keep the
  public repo, link it in the About screen and the store listing, keep notices intact. Flag any concern about
  distributing GPL apps through Google Play and explain the options (Play, F-Droid, GitHub Releases, IzzyOnDroid).
- **Third-party licenses:** regenerate `docs/DEPENDENCY_LICENSES.md` and the in-app licenses screen.
- **Play Console checklist (guide me, I will click):** developer account, app content (privacy policy, ads: none,
  data safety, target audience, content rating questionnaire, news/government/health declarations), store
  listing, countries, internal -> closed (20 testers for 14 days if it is a new personal account) -> production,
  staged rollout 10% -> 50% -> 100%.
- **F-Droid / IzzyOnDroid:** prepare metadata (`fastlane/` or `metadata/`), reproducible build notes, and an
  inclusion request draft.

## Phase 7: Launch and after
- Launch checklist `docs/LAUNCH.md`: go/no-go list, rollback plan, who does what.
- Post-launch: monitor Play vitals (crash rate, ANR rate), reviews, GitHub issues. Write issue templates for
  *our* repo (bug, feature) and a short `CONTRIBUTING.md` only if I want outside contributions.
- Roadmap file with the next 3 releases; define a hotfix process (branch, patch, tag, staged rollout).

## Working rules
- Never commit secrets, keystores, `local.properties` or tokens. Never force-push `main`.
- Explain decisions briefly, show before/after numbers for performance changes, and stop to ask when a
  choice costs money, needs my accounts, or changes the license or privacy posture.
- End each phase with: what changed, evidence (test output, numbers), what is left, and the next step.
- At the very end, give me a one-page summary: build artifacts and where they are, remaining manual
  steps (Play Console, keystore backup), and known risks.
