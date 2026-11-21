# Prompt for Claude Code (run in the root of this repo, with Android Studio / SDK installed)

You are working on RollerSkater, a rebranded GPL-3.0 fork of an Android music player
(Kotlin, Compose, Hilt, Room, Media3). The rename (package `com.rollerskater.app`), logo and
upstream-link cleanup are already done. Your job is to remove features and get a clean build.

## Rules
- Keep `LICENSE`, `NOTICE.md`, `THIRD_PARTY_NOTICES.md`, `CHANGELOG.md` and `app/src/main/assets/licenses/` as they are.
- Do not reintroduce the old name or upstream URLs. The app is called RollerSkater.
- Work in small steps. After each step run `./gradlew :app:compileDebugKotlin` and fix errors before moving on.
- Never leave the app non-compiling at the end.

## Remove these features completely
1. **Navidrome / Subsonic** and **Jellyfin** (self-hosted servers): `data/navidrome`, `data/jellyfin`,
   `data/network/navidrome`, `data/network/jellyfin`, `presentation/navidrome`, `presentation/jellyfin`,
   their Hilt providers in `di/AppModule.kt`, `CloudSyncCoordinator`, `CloudTrackDownloadWorker`,
   `CloudLibraryPickerSheet`, `StreamingProviderSheet`, `StreamingProviderLoginViewModel`, `AccountsScreen/ViewModel`,
   `CloudPlaybackUriResolver`, `CloudStreamProxy`, `CloudStreamSecurity`, `CloudOfflineRepository`,
   `SharedCloudArtworkMappers`, `RemoteArtworkCache` cloud parts, and the Cloud/streaming branches in
   `MusicService`, `DualPlayerEngine`, `MediaItemBuilder`, `MusicRepositoryImpl`, `HomeScreen`,
   `PlaylistContainer`, `SongPickerBottomSheet`, `AppNavigation`/`Screen`, `SettingsRegistry`, `ListenBrainz*`.
   Remove cloud-only fields from `Song`, `SongEntity`, `OfflineTrackEntity` and related DAOs.
   Keep `org.jellyfin.media3:media3-ffmpeg-decoder` (it is only the FFmpeg audio decoder, not Jellyfin).
2. **Daily Mix / smart mix / NLP playlist generator**: `DailyMixManager`, `DailyMixStateHolder`, `DailyMixScreen`,
   `DailyMixSection`, `SmartMix*`, `MixComposerSheet`, `MixPreview`, `data/playlist/nlp/*`, `NlpPlaylistGenerator`,
   `SmartPlaylistBuilder`, plus related settings, nav routes and asset files in `app/src/main/assets/nlp`.
3. **Leftover strings**: remove unused Telegram, Netease, Gemini/AI, cloud, Navidrome and Jellyfin strings from
   `res/values*/` (all locales). Remove tests (`src/test`, `src/androidTest`) that only cover removed features.
4. Drop dependencies that become unused from `app/build.gradle.kts` and `gradle/libs.versions.toml`.

## Database
Removing columns/entities changes the Room schema. Bump the database version in `RollerSkaterDatabase`,
add a migration (or, if this app has no installed users yet, reset to version 1 and regenerate `app/schemas`).
Make sure backup/restore handlers (`data/backup/module/*`) no longer reference removed data.

## Finish
- `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest` must pass.
- `git grep -i -E "navidrome|jellyfin|dailymix|telegram|netease|gemini"` should return nothing
  except the FFmpeg decoder dependency line and the preserved license files.
- Run the app and check: library scan, playback, queue, playlists, lyrics, settings, widget, About screen.
- Summarize what was removed and anything you were unsure about.
