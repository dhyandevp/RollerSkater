# RollerSkater — AI Agent Guidelines & Architecture Manual

Welcome to **RollerSkater**, a fast, offline-first Android music player built with Jetpack Compose, Material 3, and Kotlin Coroutines/Flow.

This file serves as the definitive reference for AI coding agents (and human contributors) working on this repository.

---

## 1. Project Philosophy & Core Principles

- **Offline-First & Privacy-Focused**: RollerSkater does not include analytics, telemetry, crash report uploaders (like Firebase or Crashlytics), or proprietary advertising SDKs. All listening statistics, playlists, favorites, and metadata are stored strictly on-device.
- **Performance Budget**: Target smooth 60–120fps UI rendering across both flagship and mid-to-low tier Android devices. Prevent unnecessary Jetpack Compose recompositions through state slicing with `distinctUntilChanged()`.
- **Clean Architecture**: Follow unidirectional data flow (UDF) with clean boundaries between UI (`presentation`), Domain/Data (`data`), Audio Service (`service`), and native/audio utilities (`utils`).
- **GPL-3.0 Compliance**: RollerSkater is free software licensed under the **GNU General Public License v3.0**. Always preserve required notices (`NOTICE.md`, `THIRD_PARTY_NOTICES.md`, `LICENSE`, and `app/src/main/assets/licenses/`).

---

## 2. Tech Stack & Key Libraries

- **Language & Runtime**: Kotlin 2.x, targeting Android minSdk 30 (Android 11+), compileSdk 35.
- **UI Toolkit**: Jetpack Compose, Material 3, Material Icons Extended, Accompanist / Smooth Corners.
- **Audio Engine**: AndroidX Media3 / ExoPlayer with `DualPlayerEngine` (gapless playback, crossfade, sleep timer), extended codec decoding via `org.jellyfin.media3:media3-ffmpeg-decoder`.
- **Dependency Injection**: Dagger Hilt.
- **Persistence**: Room Database (`RollerSkaterDatabase`), Jetpack DataStore (`UserPreferencesRepository`, `EqualizerPreferencesRepository`).
- **Paging**: AndroidX Paging 3 (`MediaStorePagingSource`, Room Paging).
- **Metadata & Audio Tags**: Kyant0 TagLib native bindings (`libtaglib.so`), `AudioMetadataReader`.
- **Image Loading**: Coil 3 with custom decoders and memory caches.
- **Benchmarking & Optimization**: AndroidX Benchmark, Baseline Profiles (`baselineprofile` module).

---

## 3. Directory Layout

```
rollerskater/
├── app/
│   ├── schemas/                          # Room database schema export files
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/                   # Bundled fonts, license notices, offline tables
│   │   │   ├── java/com/rollerskater/app/
│   │   │   │   ├── data/                 # Database, Room DAOs, DataStore, Repositories, Backup
│   │   │   │   ├── di/                   # Hilt DI modules (AppModule, DatabaseModule, etc.)
│   │   │   │   ├── presentation/         # Jetpack Compose UI (Screens, Sheets, Components, ViewModels)
│   │   │   │   ├── service/              # Media3 playback service (MusicService), notification handlers
│   │   │   │   ├── ui/                   # Theme, Typography, Color roles, Glance widgets
│   │   │   │   └── utils/                # Audio metadata, artwork caching, lyrics parsers
│   │   │   └── res/                      # Android resources (drawables, values, XMLs)
│   │   └── test/                         # Unit tests
│   └── build.gradle.kts
├── baselineprofile/                      # Macrobenchmarks & Baseline Profile generators
├── gradle/                               # Wrapper and version catalogs (libs.versions.toml)
├── graphify-out/                         # Precomputed Knowledge Graph (graph.html, graph.json, GRAPH_REPORT.md)
├── NOTICE.md                             # GPL-3.0 attribution and modification notice
├── THIRD_PARTY_NOTICES.md                # Third-party library and asset licenses
├── README.md                             # Public documentation
└── AGENTS.md                             # Agent guidelines and architectural manual
```

---

## 4. Build, Test, and Verification Commands

Execute all commands from the repository root:

### Compilation & Build
```bash
# Compile Kotlin debug sources (fast feedback)
./gradlew :app:compileDebugKotlin

# Assemble debug APK
./gradlew :app:assembleDebug

# Assemble release APK (unsigned if keystore is not provided)
./gradlew :app:assembleRelease
```

### Testing & Verification
```bash
# Run all unit tests
./gradlew :app:testDebugUnitTest

# Run specific unit test class
./gradlew :app:testDebugUnitTest --tests "com.rollerskater.app.presentation.viewmodel.PlayerViewModelTest"

# Run Android lint
./gradlew lint
```

### Baseline Profile Generation
```bash
# Generate baseline profile rules
./gradlew :baselineprofile:generateBaselineProfile
```

---

## 5. Coding & Architectural Standards

### Compose UI & ViewModels
1. **Prevent God Object Recomposition**:
   - Do **NOT** observe giant monolithic state objects (such as raw `PlayerUiState` with 30+ fields) inside individual library items or search lists.
   - Use derived state flows sliced with `distinctUntilChanged()` (e.g., `currentPlayingSongIdFlow = playerUiState.map { it.currentSong?.id }.distinctUntilChanged()`).
2. **Stable Keys in Lazy Lists**:
   - Always supply explicit `key` parameters to `items(...)` in `LazyColumn` and `LazyRow` using persistent IDs (e.g., `song.id`, `album.id`).
3. **Consolidate Animations**:
   - Avoid nesting multiple individual `animate*AsState` calls per list item. Use `updateTransition` or consolidated animatables to minimize per-frame callback overhead.

### Data Layer & Database
1. **Room Migrations**:
   - Any modification to database entities (`SongEntity`, `PlaylistEntity`, etc.) requires updating `RollerSkaterDatabase` version and providing a clean migration or schema update.
   - Export schemas to `app/schemas/` to keep track of version histories.
2. **Repository Pattern**:
   - ViewModels should interact exclusively with repositories (`MusicRepository`, `UserPreferencesRepository`), never directly with raw DAOs or DataStore files.

### Media & Playback Service
1. **Audio Service Lifecycle**:
   - Playback logic lives in `MusicService` backed by `DualPlayerEngine`.
   - Manage sleep timers inside the playback service (`service/` layer) to avoid timer drift during device CPU sleep / audio offload states.

---

## 6. Knowledge Graph & Graphify Navigation

This repository is indexed by `graphify`. When navigating or understanding unfamiliar subsystems:
- **Interactive Visualizer**: Open `graphify-out/graph.html` in a web browser for an interactive map of communities and module hubs.
- **Graph Report**: Review `graphify-out/GRAPH_REPORT.md` for detected God Nodes, cross-community bridges, and cohesion metrics.
- **Graph Queries**:
  ```bash
  # Query architecture or tracing paths via CLI
  graphify query "How does playback state propagate to the UI?"
  graphify path "MusicService" "PlayerViewModel"
  graphify explain "DualPlayerEngine"
  ```

---

## 7. Rules for Autonomous Agents

1. **Standalone Identity**: Never reintroduce old upstream names, links, or external cloud accounts. The project is strictly **RollerSkater**.
2. **Incremental Verification**: After modifying Kotlin source files, always run `./gradlew :app:compileDebugKotlin` and `./gradlew :app:testDebugUnitTest` to guarantee the build remains clean.
3. **Preserve Documentation**: Maintain comments, license headers, and required legal notices across all changes.
