# RollerSkater — Testing & Quality Assurance Manual

This document details the testing architecture, test suites, execution commands, and verification gates for **RollerSkater**.

---

## 1. Testing Philosophy & Stack

RollerSkater maintains a strict, multi-tiered testing strategy designed to verify business logic, state machines, audio transitions, database migrations, and UI performance:

| Test Tier | Frameworks & Tools | Scope & Focus | Count |
|---|---|---|---|
| **JVM Unit Tests** | JUnit 5 (Jupiter), MockK, Turbine, Google Truth | ViewModels, StateHolders, Repositories, Room DAOs, Migrations, Serializers | **197 Classes** (~1,480 tests) |
| **Instrumented Tests** | AndroidX Test, AndroidJUnitRunner, Room `MigrationTestHelper` | Database migrations on real SQLite engines, Media3 IPC | **33 Classes** |
| **Macrobenchmarks** | AndroidX Benchmark, Macrobenchmark, UI Automator | Cold startup time, library scrolling jank, player animations | **4 Classes** |
| **Static Analysis** | Android Lint, Compose Stability Compiler | Android resource leaks, accessibility, Compose recomposition stability | Continuous in CI |

---

## 2. Running JVM Unit Tests

All unit tests execute on the local JVM without requiring an emulator or physical device.

### Run All Unit Tests
```bash
./gradlew :app:testDebugUnitTest
```

### Run a Specific Test Class
```bash
./gradlew :app:testDebugUnitTest --tests "com.rollerskater.app.presentation.viewmodel.PlayerViewModelTest"
```

### Run Tests Matching a Pattern
```bash
# Run all repository tests
./gradlew :app:testDebugUnitTest --tests "com.rollerskater.app.data.repository.*"

# Run all Room migration tests
./gradlew :app:testDebugUnitTest --tests "com.rollerskater.app.data.database.RoomMigrationTest"
```

### HTML Test Reports
When unit tests complete, Gradle writes an interactive HTML report to:
`app/build/reports/tests/testDebugUnitTest/index.html`

Open in your browser to inspect test results, timings, and stack traces:
```bash
xdg-open app/build/reports/tests/testDebugUnitTest/index.html
```

---

## 3. Key Unit Test Suites & Coverage

### ViewModels & State Holders (`app/src/test/java/com/rollerskater/app/presentation/`)
- **`PlayerViewModelTest.kt`**: Tests state hydration, media controller binding, play/pause commands, and track skip orchestration.
- **`PlaybackStateHolderTest.kt`**: Validates player state transitions, buffer progress calculations, and playback speed modifiers.
- **`QueueStateHolderTest.kt`**: Tests queue shuffling, repeat modes (`OFF`, `ALL`, `ONE`), and non-destructive track reordering.
- **`SearchStateHolderTest.kt`**: Verifies FTS4 query tokenization, history persistence, and instantaneous search result filtering.
- **`RecapViewModelTest.kt`**: Validates listening stats aggregation, top artist calculations, and annual story recap slides.

### Repositories & Data Layer (`app/src/test/java/com/rollerskater/app/data/`)
- **`MusicRepositoryTest.kt`**: Tests MediaStore incremental scanning, deduplication, and Room database synchronization.
- **`TransitionRepositoryTest.kt`**: Tests parametric equal-power crossfade calculation and custom transition rule precedence.
- **`BackupManagerTest.kt`**: Verifies atomic export and restoration of playlists, favorites, EQ curves, and preferences.

### Metadata & Tag Parsers (`app/src/test/java/com/rollerskater/app/utils/`)
- **`AudioMetaUtilsTest.kt`**: Validates codec MIME type resolution, bitrate estimation, and sample rate parsing.
- **`EnhancedLrcParsingTest.kt`**: Tests parsing of synchronized `.lrc` and `.ttml` lyrics with millisecond timestamps.
- **`GenreParserTest.kt`**: Tests ID3v1 genre code mapping and custom genre string normalization.

---

## 4. Writing Asynchronous Flow & ViewModel Tests

We use **Turbine** (`app.cash.turbine`) and **MockK** for coroutines testing:

```kotlin
@Test
fun `playing track updates playback state correctly`() = runTest {
    val fakeSong = createFakeSong(id = "track_1", title = "Midnight City")
    
    viewModel.playbackState.test {
        // Initial state
        val initial = awaitItem()
        assertThat(initial.isPlaying).isFalse()

        // Action
        viewModel.playSong(fakeSong)

        // Emitted state
        val playing = awaitItem()
        assertThat(playing.isPlaying).isTrue()
        assertThat(playing.currentSong?.id).isEqualTo("track_1")
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## 5. Instrumented Tests (`app/src/androidTest/`)

Instrumented tests verify database migrations and hardware services on physical devices or emulators:

```bash
# Connect device or launch emulator, then run:
./gradlew :app:connectedDebugAndroidTest
```

- **Room Migration Tests**: Utilizes `androidx.room.testing.MigrationTestHelper` to load schemas exported in `app/schemas/`, populate test rows, execute migrations (`MIGRATION_1_2` through `MIGRATION_7_8`), and verify column integrity.

---

## 6. Macrobenchmarks & Baseline Profiles

RollerSkater includes a dedicated `:baselineprofile` module targeting startup latency and smooth 120fps UI animations.

### Generating Baseline Profiles
Connect an Android device running Android 7+ (API 24+) with root or `adb shell` privileges:
```bash
./gradlew :baselineprofile:generateBaselineProfile
```
The generator runs:
1. **`StartupBenchmarks.kt`**: Measures cold startup from app process launch to first rendered frame.
2. **`PlayerSheetAnimationBenchmarks.kt`**: Measures frame drops while swiping open the full-screen player sheet.
3. Automatically writes optimized profile rules directly to `app/src/main/baseline-prof.txt`.

---

## 7. Android Lint Static Analysis

Android Lint verifies resource usage, security flags, accessibility labels, and platform compatibility:

```bash
./gradlew :app:lintDebug
```
Reports are written to:
- HTML: `app/build/reports/lint-results-debug.html`
- SARIF: `app/build/reports/lint-results-debug.sarif`
