# Contributing to RollerSkater

Thank you for your interest in contributing to **RollerSkater**! We welcome bug reports, feature suggestions, code contributions, and documentation improvements.

Please take a few moments to review these guidelines before getting started.

---

## 1. Prime Directive: Standalone Repository Mandate

RollerSkater (`com.rollerskater.app`) is an independent, offline-first Android music player. All contributors and automated agents must adhere to the following principles:

1. **Strict Repository Independence**:
   - The canonical remote repository is `origin: https://github.com/dhyandevp/RollerSkater.git`.
   - Never add, reference, link, or sync with upstream or predecessor repositories.
   - Do not reintroduce legacy URLs, issue tracker links, or remote telemetry/analytics packages.
2. **Unified Naming & Identity**:
   - The application name is strictly **RollerSkater**.
   - The application ID and package namespace is strictly `com.rollerskater.app`.
   - Legacy names must never appear in user-facing UI, notifications, file providers, or database identifiers.
3. **Legal Notice Preservation**:
   - In accordance with GPL-3.0 Section 5, historical copyright notices in `NOTICE.md`, `THIRD_PARTY_NOTICES.md`, `LICENSE`, and `app/src/main/assets/licenses/` must remain preserved.

---

## 2. Development Environment Setup

### System Requirements
- **JDK**: Java Development Kit 21 (Eclipse Temurin, OpenJDK, or Amazon Corretto).
- **Android SDK**: `minSdk 30`, `compileSdk 37`, `targetSdk 37`.
- **Android Studio**: Android Studio Koala Feature Drop / Ladybug / Meerkat or later.
- **Android NDK**: Required for compiling native TagLib components (`libtaglib.so`).

### Initial Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/dhyandevp/RollerSkater.git
   cd RollerSkater
   ```
2. Verify that only the official repository remote is configured:
   ```bash
   git remote -v
   ```
3. Verify local compilation:
   ```bash
   ./gradlew :app:compileDebugKotlin
   ```

---

## 3. Coding & Architectural Standards

### Jetpack Compose UI
1. **Prevent Monolithic Recomposition**:
   - Do not observe large, monolithic state objects (such as `PlayerUiState` containing 30+ properties) inside individual list items or search results.
   - Derive granular state flows using `distinctUntilChanged()`, for example:
     ```kotlin
     val currentSongIdFlow = playerUiState
         .map { it.currentSong?.id }
         .distinctUntilChanged()
     ```
2. **Stable Keys in Lazy Layouts**:
   - Always provide explicit, stable keys in `LazyColumn`, `LazyRow`, and `LazyVerticalGrid`:
     ```kotlin
     items(songs, key = { it.id }) { song ->
         SongListItem(song = song)
     }
     ```
3. **Consolidate Animations**:
   - Avoid creating multiple independent `animate*AsState` instances inside every item of a lazy list.
   - Use `updateTransition` or consolidated animatables to minimize per-frame callback overhead.

### Data Layer & Persistence
1. **Repository Pattern**:
   - ViewModels and UI state holders interact exclusively with Repositories (`MusicRepository`, `UserPreferencesRepository`), never directly with Room DAOs or DataStore files.
2. **Room Database Migrations**:
   - Any modification to database entities (`SongEntity`, `PlaylistEntity`, etc.) requires incrementing the database version in `RollerSkaterDatabase` and providing an explicit migration strategy.
   - Room schema export files must be committed to `app/schemas/` to preserve migration history.
3. **Offline & Privacy Integrity**:
   - Never introduce proprietary analytics, tracking SDKs, or cloud synchronization without explicit on-device, opt-in designs. All user data (playlists, listening stats, favorites) must remain stored locally.

### Audio & Playback Service
1. **DualPlayerEngine Lifecycle**:
   - All playback logic, transitions, and ExoPlayer instance management belong in `service/` and `data/service/player/`.
   - Never hold long-lived references to Android `Context` or `MediaController` inside background threads or static singletons.
2. **Sleep Timer Accuracy**:
   - Sleep timers must be managed inside the playback service (`MusicService`) to prevent timer drift during CPU sleep states.

---

## 4. Git & Commit Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) format:

```text
<type>(<optional scope>): <short summary in present tense>

[optional body explaining rationale]

[optional footer(s)]
```

### Common Types:
- `feat`: A new user-facing feature or capability.
- `fix`: A bug fix.
- `docs`: Documentation updates or additions.
- `refactor`: Code changes that neither fix a bug nor add a feature.
- `perf`: Performance optimizations (e.g., recomposition reduction, database index tuning).
- `test`: Adding or updating unit tests.
- `chore`: Build scripts, dependencies, or tool configuration.

### Examples:
- `feat(player): add logarithmic volume curve for crossfade transitions`
- `fix(lyrics): resolve word-sync alignment for offset timestamps`
- `perf(library): optimize album list recomposition using stable keys`

---

## 5. Verification Checklist

Before submitting a Pull Request, ensure that all verification checks pass locally:

```bash
# 1. Compile all Kotlin sources
./gradlew :app:compileDebugKotlin

# 2. Run the full unit test suite
./gradlew :app:testDebugUnitTest

# 3. Assemble debug APK
./gradlew :app:assembleDebug

# 4. Run static analysis & linting
./gradlew lint
```

---

## 6. Submitting a Pull Request

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   ```
2. Commit your changes following the commit conventions above.
3. Push your branch to your fork:
   ```bash
   git push origin feat/your-feature-name
   ```
4. Open a Pull Request against the `main` branch of `dhyandevp/RollerSkater`.
5. Complete the Pull Request template checklist describing what changed, testing performed, and any non-obvious rationale.
