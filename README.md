<p align="center">
  <img src="assets/RollerSkater.svg" alt="RollerSkater Logo" height="180"/>
</p>

<h1 align="center">RollerSkater</h1>

<p align="center">
  <strong>A high-performance, offline-first Android music player built with Jetpack Compose, Material 3, and AndroidX Media3.</strong>
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-blue.svg" alt="GPL-3.0 License"/></a>
  <img src="https://img.shields.io/badge/Platform-Android%2011%2B%20(API%2030%2B)-green.svg" alt="Platform: Android 11+"/>
  <img src="https://img.shields.io/badge/Language-Kotlin%202.x-purple.svg" alt="Language: Kotlin 2.x"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-blueviolet.svg" alt="UI: Jetpack Compose / Material 3"/>
  <img src="https://img.shields.io/badge/Privacy-100%25%20On--Device-success.svg" alt="Privacy: 100% On-Device"/>
</p>

---

## Overview

**RollerSkater** is an independent, local-first Android music player designed for audiophiles and privacy-conscious listeners. Engineered around Android's modern Jetpack Compose toolkit and AndroidX Media3, RollerSkater delivers fluid 60–120 FPS animations, gapless playback, customizable crossfade transitions, embedded & synchronized `.lrc`/`.ttml` lyrics, native tag editing via TagLib, and dynamic Material You personalization—completely free of analytics, telemetry, or remote crash uploaders.

---

## Key Features

### 🎧 Precision Audio Engine
- **DualPlayerEngine**: Dual-player architecture utilizing primary and secondary ExoPlayer instances for seamless gapless playback and crossfades.
- **Parametric Transitions**: Configurable crossfade durations with smooth mathematical volume curve envelopes.
- **Audio Offload Support**: Low-power hardware audio offload with automated fallback detection to ensure uninterrupted playback.
- **Equalizer Integration**: Custom 5-to-10 band equalizer with bass boost, virtualizer, and custom user presets.
- **Extended Audio Codec Decoding**: FFmpeg software decoding extension (`media3-ffmpeg-decoder`) alongside system decoders for broad audio format coverage (MP3, FLAC, M4A/AAC, OGG, OPUS, WAV).

### 🎨 Modern Material 3 UI & Personalization
- **Dynamic Theming**: Dynamic color palette extraction from album artwork via Palette API, with full support for system dynamic themes (Material You).
- **AMOLED True Black**: Deep-black theme mode optimized for OLED and AMOLED displays.
- **Adaptive Layouts**: Responsive navigation across mobile screens, foldable displays, and tablets using AndroidX Window Adaptive APIs.
- **Parallax Carousels & Waveforms**: Hardware-accelerated transitions and interactive seekbars.

### 📚 Comprehensive Library & Tag Management
- **Local Library Organization**: Instant sorting and filtering by Tracks, Albums, Artists, Genres, and Folder hierarchies.
- **Fast Search**: In-memory and indexed database search with recent query history.
- **Native Metadata Editing**: Direct file-level tag editing (ID3v1, ID3v2, FLAC Vorbis comments, MP4 tags) backed by native TagLib JNI (`libtaglib.so`).
- **Flexible Playlists**: Custom playlists, drag-to-reorder queue management, and batch multi-selection tools.

### 🎤 Synchronized Lyrics Engine
- **Multi-Format Parsing**: Instant support for embedded tag lyrics, external `.lrc` synchronized files, and `.ttml` timed text files.
- **Word-Level Highlights**: Real-time karaoke-style word highlighting and smooth auto-scrolling with manual time-offset adjustment.

### 🔒 Offline-First Privacy & Local Backup
- **Zero Telemetry**: Completely devoid of tracking SDKs, Firebase, Crashlytics, and proprietary analytics.
- **Storage Access Framework (SAF)**: Secure local backups and directory access without requiring broad filesystem permissions.
- **JSON Backup & Restore**: One-tap export and import of playlists, favorites, equalizer configurations, listening statistics, and preferences.

### 📱 Android System Integration
- **Glance Home-Screen Widgets**: Interactive Material 3 widgets with live playback controls and reactive artwork updates.
- **MediaSession & Notifications**: Full lockscreen playback controls, fast-forward/rewind actions, and Android Auto support.
- **Sleep Timer**: System-level sleep timer with gradual volume fade-out.

---

## Architecture at a Glance

RollerSkater follows Clean Architecture and Unidirectional Data Flow (UDF) design principles:

```mermaid
flowchart TD
    subgraph UI ["Presentation Layer (Jetpack Compose)"]
        Activity["MainActivity"]
        Screens["Screens & BottomSheets\n(HomeScreen, PlayerSheet, Library, Lyrics)"]
        VM["ViewModels & StateHolders\n(PlayerViewModel, LibraryStateHolder, etc.)"]
    end

    subgraph Service ["Audio Service Layer (Media3)"]
        MusicService["MusicService\n(MediaSessionService)"]
        DualEngine["DualPlayerEngine\n(ExoPlayer A + ExoPlayer B)"]
        EqSink["EqualizerAudioSink & AudioOffload"]
    end

    subgraph Data ["Data & Storage Layer"]
        Repo["MusicRepository & PreferencesRepositories"]
        RoomDB["Room Database\n(RollerSkaterDatabase v7)"]
        DataStore["Jetpack DataStore Preferences"]
        MediaStore["Android MediaStore & SAF"]
        TagLib["Native TagLib (libtaglib.so)"]
    end

    Screens --> VM
    VM --> Repo
    VM -.->|"MediaController"| MusicService
    MusicService --> DualEngine
    DualEngine --> EqSink
    Repo --> RoomDB
    Repo --> DataStore
    Repo --> MediaStore
    Repo --> TagLib
```

For an in-depth breakdown of architectural patterns, threading models, and engine state machines, refer to [ARCHITECTURE.md](ARCHITECTURE.md).

---

## Directory Structure

```text
rollerskater/
├── app/
│   ├── schemas/              # Exported Room database schema history (v1 through v7)
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/       # Fonts, license notices, bundled assets
│   │   │   ├── java/com/rollerskater/app/
│   │   │   │   ├── data/     # Database entities, DAOs, repositories, DataStore preferences
│   │   │   │   ├── di/       # Dagger Hilt dependency injection modules
│   │   │   │   ├── presentation/ # Jetpack Compose UI (screens, sheets, components, viewmodels)
│   │   │   │   ├── service/  # Media3 playback service (MusicService, notification, audio engine)
│   │   │   │   ├── ui/       # Material 3 theme definitions, typography, Glance widgets
│   │   │   │   └── utils/    # TagLib wrappers, lyric parsers, artwork caching utilities
│   │   │   └── res/          # Android drawables, localized strings, XML resources
│   │   └── test/             # Comprehensive JUnit 5 unit test suites
│   └── build.gradle.kts      # Application build configuration
├── baselineprofile/          # Macrobenchmark & Baseline Profile generators
├── docs/                     # Detailed architectural, audio engine, and release documentation
├── gradle/                   # Gradle wrapper and version catalog (libs.versions.toml)
├── NOTICE.md                 # GPL-3.0 attribution and modification notice
├── THIRD_PARTY_NOTICES.md    # Third-party dependency licenses and font notices
├── PRIVACY.md                # Local-first privacy policy
├── CONTRIBUTING.md           # Guidelines for contributors and coding standards
├── ARCHITECTURE.md           # Complete architectural manual
└── README.md                 # Project documentation
```

---

## Building from Source

### Prerequisites
- **JDK**: Java Development Kit 21 (OpenJDK recommended)
- **Android SDK**: `minSdk 30` (Android 11), `compileSdk 37`, `targetSdk 37`
- **Android NDK**: Required for compiling native TagLib bindings if building from source

### Compilation Commands

Execute commands from the repository root:

```bash
# 1. Compile Kotlin debug sources
./gradlew :app:compileDebugKotlin

# 2. Assemble Debug APK (outputs to app/build/outputs/apk/debug/)
./gradlew :app:assembleDebug

# 3. Assemble Release APK (unsigned verification build)
./gradlew :app:assembleRelease -Prollerskater.disableReleaseSigning=true

# 4. Run Unit Test Suite
./gradlew :app:testDebugUnitTest

# 5. Run Android Lint Checks
./gradlew lint
```

---

## Tech Stack & Dependencies

| Category | Libraries / Components |
|---|---|
| **Language & Concurrency** | Kotlin 2.x, Kotlin Coroutines, Kotlin Flow |
| **UI Framework** | Jetpack Compose, Material 3, Material Icons Extended |
| **Audio Engine** | AndroidX Media3 / ExoPlayer, `DualPlayerEngine`, `media3-ffmpeg-decoder` |
| **Dependency Injection** | Dagger Hilt |
| **Persistence** | AndroidX Room (SQLite), Jetpack DataStore (Preferences) |
| **Pagination** | AndroidX Paging 3 |
| **Image Loading** | Coil 3 with custom memory cache decoders |
| **Native Metadata** | Kyant0 TagLib (`libtaglib.so`) |
| **Home Widgets** | AndroidX Glance (Compose for App Widgets) |
| **Testing** | JUnit 5 (Jupiter), MockK, Google Truth, Kotlinx Coroutines Test |

---

## Contributing

We welcome community contributions, bug fixes, and improvements! Please review [CONTRIBUTING.md](CONTRIBUTING.md) before submitting pull requests.

> [!IMPORTANT]
> RollerSkater is an independent, standalone project. All contributions must maintain repository isolation and adhere strictly to the project guidelines defined in [AGENTS.md](AGENTS.md) and [CONTRIBUTING.md](CONTRIBUTING.md).

---

## License & Attribution

RollerSkater is free software licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for terms and conditions.

In accordance with GPL-3.0 Section 5, this project preserves historical copyright and license notices of predecessor open-source components. For detailed legal notices and third-party library attributions, see [NOTICE.md](NOTICE.md) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
