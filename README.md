<p align="center">
  <img src="assets/RollerSkater.svg" alt="RollerSkater icon" height="200"/>
</p>

<h1 align="center">RollerSkater</h1>

<p align="center">A fast, offline-first Android music player built with Jetpack Compose and Material 3.</p>

## Features

- Local library playback (Media3 / ExoPlayer, gapless, crossfade, queue controls, sleep timer)
- Albums, artists, playlists, favorites, tag editing
- Embedded and local `.lrc` lyrics
- Material 3 UI with dynamic color, light and dark themes, home-screen widgets
- Backup and restore of preferences, playlists, favorites and stats

## Build

Requirements: JDK 21 and the Android SDK (minSdk 30).

```sh
./gradlew :app:assembleDebug
```

Package name: `com.rollerskater.app`

## License

RollerSkater is free software under the [GNU General Public License v3.0](LICENSE).
It is a modified version of an existing GPL-3.0 project. See [NOTICE.md](NOTICE.md) and
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the required copyright and modification notices.
