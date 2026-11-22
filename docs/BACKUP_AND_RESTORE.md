# Backup and Restore Specification

This document details the on-device backup format, serialization mechanisms, and restoration protocols in **RollerSkater**.

---

## 1. Architectural Principles

1. **Strictly On-Device**: Backups are written directly to user-selected locations using the Android Storage Access Framework (SAF). No cloud uploads or external sync servers are used.
2. **Human-Readable & Portable**: Backups are serialized as formatted JSON archives or zipped `.rsk` containers containing reviewable text structures.
3. **Modular Architecture**: Different data domains (Playlists, Favorites, Preferences, Equalizer, Listening Stats) are isolated in independent handler modules located under `data/backup/module/`.

---

## 2. Backup Modules & Handlers

The backup system delegates serialization to specialized handlers implementing `BackupModuleHandler`:

| Module | Handler Class | Data Preserved |
|---|---|---|
| **Playlists** | `PlaylistsModuleHandler` | User playlist definitions, custom descriptions, track URIs, and ordering |
| **Favorites** | `FavoritesModuleHandler` | Favorite track IDs, marked timestamp records |
| **Preferences** | `PreferencesModuleHandler` | User preferences from Jetpack DataStore (theme, layout, audio rules) |
| **Equalizer** | `EqualizerModuleHandler` | Custom equalizer curves, user presets, bass boost / virtualizer settings |
| **Listening Stats** | `ListeningStatsModuleHandler` | Play counts, skip counts, total listen duration, and recap records |
| **Audio Bookmarks**| `AudioBookmarksModuleHandler` | Track progress markers and audio resume checkpoints |

---

## 3. Serialization Format

A typical backup manifest adheres to the following structure:

```json
{
  "version": 1,
  "app_version": "0.4.0",
  "created_at_epoch_ms": 1732230000000,
  "device_info": "Generic Android 14",
  "modules": {
    "playlists": [
      {
        "id": "playlist_synthwave_favorites",
        "name": "Synthwave Favorites",
        "description": "Late night driving tracks",
        "tracks": [
          { "uri": "content://media/external/audio/media/1042", "title": "Nightcall", "artist": "Kavinsky" }
        ]
      }
    ],
    "favorites": [
      "content://media/external/audio/media/1042"
    ],
    "equalizer": {
      "preset": "Electronic",
      "bands": [4, 2, 0, 1, 3]
    }
  }
}
```

---

## 4. Restoration & Collision Resolution

During the restore workflow (`BackupManager`):
1. **Validation**: The manifest schema version is verified before reading data.
2. **Missing Media Matching**: If an audio file's MediaStore ID changed (e.g., following a device migration or SD card transfer), the restoration engine performs fuzzy matching based on file relative paths, titles, and artists to re-associate playlist tracks.
3. **Collision Strategy**:
   - **Playlists**: Existing playlists with identical names can either be overwritten or renamed with a timestamp suffix.
   - **Favorites**: Merged using set union operations without dropping existing favorites.
   - **Preferences**: Non-destructive merge of existing and restored configuration values.
