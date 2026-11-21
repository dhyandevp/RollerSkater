# Database Schema Specification — RollerSkaterDatabase

This document outlines the SQLite schema, entities, indexes, and migration strategies managed by Room in **RollerSkater** (`com.rollerskater.app.data.database.RollerSkaterDatabase`).

---

## 1. Overview

- **Database Class**: `RollerSkaterDatabase`
- **Current Version**: `7`
- **Schema Export Directory**: `app/schemas/com.rollerskater.app.data.database.RollerSkaterDatabase/`
- **Target SQLite Engine**: Android SQLite / Room

---

## 2. Core Entities & Tables

### 1. `songs` Table (`SongEntity`)
Stores indexed metadata for all discovered local audio tracks.

| Column | Type | Nullable | Description |
|---|---|---|---|
| `id` | `TEXT` | `NO` (PK) | Unique track identifier (e.g., MediaStore URI or hashed path) |
| `title` | `TEXT` | `NO` | Track title extracted from metadata tags |
| `artist` | `TEXT` | `NO` | Artist name |
| `album` | `TEXT` | `NO` | Album title |
| `album_id` | `INTEGER` | `NO` | Album identifier for grouped browsing |
| `duration` | `INTEGER` | `NO` | Track duration in milliseconds |
| `uri` | `TEXT` | `NO` | Content URI string for playback |
| `path` | `TEXT` | `NO` | Absolute filesystem path |
| `album_art_uri` | `TEXT` | `YES` | Custom or cached artwork URI |
| `track_number` | `INTEGER` | `NO` | Track order within album |
| `year` | `INTEGER` | `NO` | Release year |
| `date_added` | `INTEGER` | `NO` | Epoch timestamp of file discovery |
| `date_modified` | `INTEGER` | `NO` | Epoch timestamp of last file modification |
| `genre` | `TEXT` | `YES` | Musical genre |
| `mime_type` | `TEXT` | `YES` | Audio MIME type (e.g., `audio/flac`, `audio/mpeg`) |
| `bitrate` | `INTEGER` | `YES` | Encoded bitrate in bits per second |
| `sample_rate` | `INTEGER` | `YES` | Audio sampling frequency in Hz |

### 2. `playlists` Table (`PlaylistEntity`)
Stores user-created and imported playlist definitions.

| Column | Type | Nullable | Description |
|---|---|---|---|
| `id` | `TEXT` | `NO` (PK) | UUID or prefix-coded playlist ID |
| `name` | `TEXT` | `NO` | User-visible playlist title |
| `description` | `TEXT` | `YES` | Optional user description |
| `created_at` | `INTEGER` | `NO` | Epoch creation timestamp |
| `modified_at` | `INTEGER` | `NO` | Epoch last updated timestamp |
| `custom_cover_uri`| `TEXT` | `YES` | User-assigned custom playlist artwork |

### 3. `playlist_songs` Table (`PlaylistSongEntity`)
Join table linking playlists to tracks with preserved ordering.

| Column | Type | Nullable | Description |
|---|---|---|---|
| `playlist_id` | `TEXT` | `NO` (Composite PK) | Foreign key targeting `playlists(id)` |
| `song_id` | `TEXT` | `NO` (Composite PK) | Foreign key targeting `songs(id)` |
| `sort_order` | `INTEGER` | `NO` | Explicit position index within the playlist |

### 4. `listening_stats` Table (`ListeningStatsEntity`)
Maintains private on-device metrics for playback recap and listening trends.

| Column | Type | Nullable | Description |
|---|---|---|---|
| `song_id` | `TEXT` | `NO` (PK) | Song ID |
| `play_count` | `INTEGER` | `NO` | Total completed playback count |
| `skip_count` | `INTEGER` | `NO` | Total skip count (< 30s playback) |
| `last_played` | `INTEGER` | `NO` | Epoch timestamp of most recent listen |
| `total_duration_ms`| `INTEGER` | `NO` | Cumulative playback duration in milliseconds |

### 5. `album_art_themes` Table (`AlbumArtThemeEntity`)
Caches extracted Material You color palettes for instantaneous UI theme styling without blocking Compose rendering.

| Column | Type | Nullable | Description |
|---|---|---|---|
| `artwork_uri` | `TEXT` | `NO` (PK) | Normalised artwork URI |
| `primary_color` | `INTEGER` | `NO` | Dominant ARGB color value |
| `secondary_color`| `INTEGER` | `NO` | Vibrant ARGB color value |
| `surface_color` | `INTEGER` | `NO` | Background ARGB color value |

---

## 3. Database Migration History

RollerSkater strictly preserves database migration integrity:
- **v1 to v3**: Initial schema establishment with indexed track searching and playlist support.
- **v4 to v5**: Offline caching tables and artist associations.
- **v6 to v7**: Removal of legacy cloud streaming tables to establish a clean, strictly offline-first database layout.

All migration steps are defined in `app/src/main/java/com/rollerskater/app/data/database/Migrations.kt`.
