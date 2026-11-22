# RollerSkater — Deep Architecture & System Specifications

This document provides in-depth technical specifications, sequence diagrams, subsystem designs, and data flow pipelines for **RollerSkater** (`com.rollerskater.app`).

---

## 1. Architectural Blueprint & Layers

RollerSkater is structured according to Unidirectional Data Flow (UDF) and Clean Architecture principles:

```mermaid
flowchart TD
    subgraph UI ["Presentation Layer (Jetpack Compose & Material 3)"]
        Activity["MainActivity / ExternalPlayerActivity"]
        Nav["Compose NavHost (Adaptive Layouts)"]
        Screens["Screens (Home, Library, Equalizer, Settings, etc.)"]
        PlayerSheet["UnifiedPlayerSheetV2 (Mini & Full Player)"]
        ViewModels["Hilt ViewModels (PlayerViewModel, SetupViewModel, etc.)"]
        StateHolders["State Holders (ConnectivityStateHolder, SharedStateHolder)"]
    end

    subgraph Service ["Service & Playback Layer (AndroidX Media3)"]
        MusicService["MusicService (MediaSessionService)"]
        DualEngine["DualPlayerEngine (Player A + Player B)"]
        Decoders["AudioDecoderPolicy (Platform + FFmpeg)"]
        AudioSink["EqualizerAudioSink & SurroundDownmix"]
        NotifProvider["MusicNotificationProvider"]
    end

    subgraph Data ["Domain & Data Layer"]
        MusicRepo["MusicRepositoryImpl"]
        PrefRepo["UserPreferencesRepository (DataStore)"]
        EqualizerRepo["EqualizerPreferencesRepository (DataStore)"]
        StatsRepo["PlaybackStatsRepository"]
        RoomDB["RollerSkaterDatabase (v7)"]
        DAOs["MusicDao, FavoritesDao, LyricsDao, etc."]
    end

    subgraph SystemStorage ["Storage & Native Layer"]
        MediaStore["Android MediaStore & Storage Access Framework"]
        TagLib["Native TagLib (libtaglib.so)"]
        JAudioTagger["JAudioTagger & Vorbis-Java"]
        GlanceWidgets["Glance App Widgets & Providers"]
    end

    Activity --> Nav
    Nav --> Screens
    Screens --> ViewModels
    Screens --> PlayerSheet
    ViewModels --> StateHolders
    ViewModels --> MusicRepo
    ViewModels --> PrefRepo
    ViewModels -.->|"MediaController IPC"| MusicService
    MusicService --> DualEngine
    DualEngine --> Decoders
    DualEngine --> AudioSink
    MusicService --> NotifProvider
    MusicRepo --> RoomDB
    RoomDB --> DAOs
    MusicRepo --> MediaStore
    MusicRepo --> TagLib
    MusicRepo --> JAudioTagger
    PrefRepo --> SystemStorage
    MusicService -.->|"Broadcast"| GlanceWidgets
```

---

## 2. Sequence Diagram 1: Cold App Startup

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant OS as Android OS
    participant App as RollerSkaterApplication
    participant Activity as MainActivity
    participant Hilt as Dagger Hilt
    participant Repo as MusicRepository
    participant DB as RollerSkaterDatabase
    participant UI as Jetpack Compose UI
    participant Service as MusicService

    User->>OS: Tap App Icon
    OS->>App: onCreate()
    App->>App: Init Timber & StrictMode policies
    App->>App: Prewarm Coil ImageLoader
    OS->>Activity: onCreate()
    Activity->>Hilt: Inject ViewModels & Repositories
    Hilt->>DB: Open/Migrate RollerSkaterDatabase (v7)
    Activity->>UI: setContent { RollerSkaterTheme { AppNavHost } }
    UI->>Repo: Observe songs & playlists Flow
    Repo->>DB: Query cached local tracks
    DB-->>UI: Emit cached LibraryState
    UI-->>User: Render Home & Library (<600ms)
    Activity->>Service: Bind MediaController (Async SessionToken)
    Service-->>Activity: onConnected(MediaController)
    Activity->>UI: Update active PlaybackState
```

---

## 3. Sequence Diagram 2: MediaStore Synchronization & Library Indexing

```mermaid
sequenceDiagram
    autonumber
    participant Worker as SyncWorker / Repo
    participant MediaStore as Android MediaStore
    participant Excluded as ExcludedDirectories
    participant TagLib as Kyant0 TagLib (.so)
    participant Room as Room SQLite DB
    participant FTS as songs_fts (FTS4)
    participant UI as Compose UI (Paging 3)

    Worker->>MediaStore: query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
    MediaStore-->>Worker: Cursor (id, path, date_modified, duration)
    Worker->>Excluded: Filter blacklisted folders / .nomedia
    Worker->>Room: Compare date_modified with existing SongEntities
    alt Track is New or File Modified
        Worker->>TagLib: Extract tags (ID3v2, Vorbis, MP4)
        TagLib-->>Worker: AudioMetadata (Title, Artist, Album, ReplayGain, MBIDs)
        Worker->>Worker: Parse multi-artist delimiters (;, /, &, feat.)
    end
    Worker->>Room: Bulk Insert/Update SongEntities (In Transaction)
    Room->>FTS: SQLite Triggers fire (trg_songs_fts_insert / update)
    FTS-->>Room: Update unicode61 virtual search table
    Room-->>UI: Room PagingSource invalidates & emits new PagingData
    UI-->>UI: Re-render list with stable keys (song.id)
```

---

## 4. Sequence Diagram 3: Track Playback & Equalizer Pipeline

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UI as Library / PlayerSheet
    participant VM as PlayerViewModel
    participant Controller as MediaController
    participant Service as MusicService
    participant DualEngine as DualPlayerEngine
    participant Decoders as AudioDecoderPolicy
    participant AudioSink as EqualizerAudioSink
    participant HAL as Audio HAL / DSP

    User->>UI: Tap Track in Library
    UI->>VM: playTrack(song)
    VM->>Controller: setMediaItem(MediaItem) & play()
    Controller->>Service: onCustomCommand / onPlay()
    Service->>DualEngine: loadAndPlay(MediaItem)
    DualEngine->>Decoders: evaluateDecoder(mimeType)
    alt Requires Extended Codec (APE, ALAC, WMA, Float FLAC)
        Decoders-->>DualEngine: Route through FFmpeg Extension (libffmpegJNI.so)
    else Standard Platform Codec
        Decoders-->>DualEngine: Route through Android MediaCodec
    end
    DualEngine->>AudioSink: Feed Decoded Audio Buffers
    AudioSink->>AudioSink: Apply ReplayGain dB & Equalizer Bands
    AudioSink->>AudioSink: Surround Downmix (if 5.1 channel)
    AudioSink->>HAL: Write PCM stream to Hardware Sink
    Service->>Service: Post Media Notification with PlaybackState
    Service->>UI: Broadcast playback progress & state
```

---

## 5. Sequence Diagram 4: Dual-Player Gapless & Equal-Power Crossfade

```mermaid
sequenceDiagram
    autonumber
    participant PlayerA as Player A (Master)
    participant DualEngine as DualPlayerEngine
    participant PlayerB as Player B (Auxiliary)
    participant Session as MediaSession
    participant Sink as AudioSink

    Note over PlayerA,PlayerB: Normal Playback: Player A volume = 1.0f, Player B idle
    PlayerA->>DualEngine: Position reaches TrackDuration - CrossfadeDuration
    DualEngine->>PlayerB: prepare(NextMediaItem) & play()
    Note over DualEngine: Crossfade begins: duration = T
    loop Every 20ms during transition (normalized time t = elapsed / T)
        DualEngine->>PlayerA: setVolume(cos(π/2 * t))
        DualEngine->>PlayerB: setVolume(sin(π/2 * t))
        DualEngine->>Sink: Constant Power: sin²(t) + cos²(t) = 1.0f
    end
    Note over DualEngine: Transition completes (t = 1.0f)
    DualEngine->>PlayerA: adoptMediaItemAndPosition(PlayerB.mediaItem, PlayerB.position)
    DualEngine->>PlayerA: setVolume(1.0f)
    DualEngine->>PlayerB: pause() & resetToIdle()
    DualEngine->>Session: Retain seamless playback state without session re-creation
```

---

## 6. Sequence Diagram 5: On-Device Backup & Restore Pipeline

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Setup as SetupScreen / Settings
    participant Coord as BackupCoordinator
    participant SAF as Storage Access Framework
    participant Modules as BackupModuleHandlers
    participant Room as RollerSkaterDatabase

    alt Export Flow
        User->>Setup: Tap "Create Backup"
        Setup->>SAF: ACTION_CREATE_DOCUMENT (.json / .rsk)
        SAF-->>Setup: Output Document Uri
        Setup->>Coord: createBackup(uri)
        Coord->>Modules: Collect Playlists, Favorites, EQ, Stats, Bookmarks, Prefs
        Modules->>Room: Query entities
        Room-->>Modules: Entities
        Modules-->>Coord: JSON DTOs
        Coord->>SAF: Serialize formatted JSON archive
        Coord-->>User: Backup Export Completed
    else Restore Flow
        User->>Setup: Select Backup File (SAF Document Uri)
        Setup->>Coord: inspectBackup(uri)
        Coord-->>Setup: BackupManifest (version, date, available modules)
        Setup->>User: Display module selection checklist
        User->>Setup: Confirm selection
        Setup->>Coord: restoreSelectedModules(plan)
        Coord->>Modules: Deserialize and validate payload
        Modules->>Room: Fuzzy match MediaStore IDs by relative path & title
        Modules->>Room: Non-destructive merge / insert into Room tables
        Room-->>Setup: Restore Complete
        Setup-->>User: Success Notification
    end
```
