# RollerSkater — Developer Manual & Engineering Guide

This guide provides deep technical instructions for configuring your local development environment, navigating the codebase, understanding dependency injection graphs, and adhering to RollerSkater's architectural standards.

---

## 1. Prerequisites & Toolchain

| Tool / Runtime | Minimum Version | Recommended Version | Purpose |
|---|---|---|---|
| **JDK** | JDK 21 | Eclipse Temurin 21 (`21.0.x`) | Kotlin 2.4 compilation, desugaring, and JVM target 21 |
| **Android Studio** | Ladybug (2024.2.1+) | Ladybug Feature Drop / Meerkat | IDE, Compose Previews, and Gradle integration |
| **Android SDK Platform** | API 30 (Android 11) | API 37 | Target SDK: 37, Compile SDK: 37, Min SDK: 30 |
| **Android NDK** | 27.x+ | Bundled via SDK Manager | Native TagLib (`libtaglib.so`) and FFmpeg decoders |
| **Git** | 2.30+ | Latest stable | Version control |

> [!IMPORTANT]
> RollerSkater targets **Java 21**. Ensure your Android Studio Gradle JDK is explicitly configured to Java 21 (**Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**).

---

## 2. Setting Up the Local Workspace

```bash
# 1. Clone the repository
git clone https://github.com/dhyandevp/RollerSkater.git
cd RollerSkater

# 2. Verify git remote configuration (Standalone Mandate)
git remote -v
# Output must ONLY contain: https://github.com/dhyandevp/RollerSkater.git

# 3. Test rapid incremental compilation
./gradlew :app:compileDebugKotlin

# 4. Assemble the debug APK
./gradlew :app:assembleDebug
# Output APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. Architecture & Dependency Injection (Hilt)

RollerSkater employs Dagger Hilt for type-safe compile-time dependency injection.

### DI Module Structure (`com.rollerskater.app.di`)
- **`AppModule.kt`**: Provides application-wide singletons including `ApplicationContext`, `CoroutineScope`, `RollerSkaterDatabase` (v8 with migrations `MIGRATION_1_2` through `MIGRATION_7_8`), Room DAOs, DataStore repositories, Coil `ImageLoader`, sanitized `OkHttpClient` with SSL verification for opt-in endpoints (LRCLIB, Deezer, MusicBrainz), and ExoPlayer audio dependencies.
- **`BackupModule.kt`**: Constructs `BackupManager` and binds modular serialization handlers (`PlaylistsModuleHandler`, `FavoritesModuleHandler`, `TransitionsModuleHandler`, `PreferencesModuleHandler`, etc.).
- **`DispatcherProvider.kt`**: Injected coroutine dispatchers separating `Dispatchers.IO`, `Dispatchers.Default`, and `Dispatchers.Main`.

---

## 4. Jetpack Compose UI & State Architecture

### Unidirectional Data Flow (UDF)
State flows downwards through immutable data classes; user events bubble upwards as lambda callbacks:

```kotlin
// Example: SongListItem Composable with granular state slicing
@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSongClick(song) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArtThumbnail(uri = song.albumArtUri, contentDescription = song.title)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

### Critical Rules for Compose Performance
1. **Prevent Monolithic State Observation**:
   Never pass the monolithic `PlayerUiState` (which contains 30+ properties) into individual list items. Slice state with `distinctUntilChanged()`:
   ```kotlin
   val isPlayingCurrentSongFlow = playerViewModel.playbackState
       .map { it.currentSongId == song.id && it.isPlaying }
       .distinctUntilChanged()
   ```
2. **Stable Keys in Lazy Lists**:
   Always provide explicit `key` parameters using unique entity IDs:
   ```kotlin
   LazyColumn {
       items(items = songs, key = { it.id }) { song ->
           SongListItem(song = song, ...)
       }
   }
   ```
3. **Compose Stability Configuration**:
   The compiler uses [`app/compose_stability.conf`](../app/compose_stability.conf) to mark immutable models and third-party classes as stable. To inspect stability metrics:
   ```bash
   ./gradlew :app:assembleRelease -Prollerskater.enableComposeCompilerReports=true
   # Reports generated in: app/build/compose_compiler_reports/
   ```

---

## 5. Room Database Migrations

Whenever modifying database entities (`SongEntity`, `PlaylistEntity`, `ListeningStatsEntity`):

1. Increment `version` in [`RollerSkaterDatabase.kt`](../app/src/main/java/com/rollerskater/app/data/database/RollerSkaterDatabase.kt).
2. Write a clear migration object in [`Migrations.kt`](../app/src/main/java/com/rollerskater/app/data/database/Migrations.kt).
3. Register the migration in `DatabaseModule.kt`.
4. Compile debug Kotlin to generate the new schema JSON in `app/schemas/`.
5. Add a unit test verifying schema migration in `app/src/test/java/com/rollerskater/app/data/database/`.

---

## 6. Debugging & StrictMode

In debug builds:
- **Timber Logging**: Structured debug logs print to logcat with caller class and method names. Release builds automatically substitute `ReleaseTree`, sanitizing all logs.
- **LeakCanary**: Automatically watches Activity, Fragment, and ViewModel lifecycles to detect memory leaks.
- **Android StrictMode**: Configured in `RollerSkaterApplication` to detect accidental disk reads or network operations on the main UI thread.
