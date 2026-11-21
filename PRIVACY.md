# Privacy Policy

RollerSkater is an offline-first, privacy-focused Android music player. The app does not include analytics, telemetry, advertising SDKs, Firebase, Crashlytics, or Google Play Services runtime dependencies.

## Data Stored On Device

All data created or processed by RollerSkater remains strictly on your device:

- **Library & Metadata**: Audio metadata scanned from your device's storage (titles, artists, albums, durations, genres, embedded artwork, and lyrics).
- **Playlists & Favorites**: User-created playlists, favorite song markings, playback order, and custom playlist descriptions.
- **Listening History & Stats**: Local listening timestamps, total play counts, skip counts, and generated listening recaps.
- **Audio & App Preferences**: Custom equalizer presets, audio transitions (crossfade / gapless), theme colors, and layout configurations.
- **Backups**: Encrypted or plain-text user backup archives saved directly to user-chosen directories via the Android Storage Access Framework (SAF).

## Network Access & Optional Features

RollerSkater is designed to function 100% offline. Network features are completely optional, disabled by default, and activate only when explicitly configured by the user:

- **Metadata Enrichment (MusicBrainz)**: Runs only when the user explicitly triggers metadata lookup for a track. The app sends the track title, artist, and album to the public MusicBrainz API. Applied results are saved locally.
- **Online Lyrics (LRCLIB)**: If enabled by the user in settings, the app queries the public LRCLIB API to fetch synchronized or plain lyrics when local lyrics (.lrc files or embedded tags) are unavailable.
- **Artist Artwork (Deezer API)**: If enabled by the user in settings, the app queries the Deezer public API solely to download artist profile photos for local display.
- **Scrobbling (ListenBrainz)**: Optional and disabled by default. Activates only if you connect a ListenBrainz account using your personal user token. When active, listening submissions (title, artist, album, duration, timestamp) are sent to `listenbrainz.org` or your custom self-hosted ListenBrainz-compatible server (e.g., Maloja). Disconnecting removes all credentials and purges any queued submissions.
- **Update Checks**: The app checks the F-Droid package repository (Stable builds) or GitHub Releases API (Alpha builds) only when the user opens the Updates screen or taps "Check for updates", or periodically if the user enables automatic update checks. No identifying information or library data is ever transmitted.

## File and Storage Access

RollerSkater requests standard Android media permissions (`READ_MEDIA_AUDIO` on Android 13+, `READ_EXTERNAL_STORAGE` on older versions) strictly to index and play local audio files and read embedded cover art.

- The app accesses only the audio directories and media files you permit.
- Tag editing and album cover writing require explicit user confirmation or Android MediaStore write permission.
- Backups and export files are written strictly to user-selected folders via the Storage Access Framework (SAF).

## Crash Reports & Logs

RollerSkater does not automatically upload crash reports or error logs to any server. In-app crash logs remain saved locally on your device for manual review and troubleshooting.

## Contact & Inquiries

For questions or concerns regarding privacy, please open an issue on the [RollerSkater GitHub repository](https://github.com/dhyandevp/RollerSkater/issues).
