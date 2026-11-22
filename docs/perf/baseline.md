# RollerSkater — Performance Baseline Report

**Recorded**: 2026-11-22  
**Target Package**: `com.rollerskater.app`  
**Git Baseline Commit**: `cf7f7ed9e662a35f491aab67866367f21c387a89`  
**Toolchain**: Android Gradle Plugin 8.9+, Kotlin 2.1+, Compile SDK 37, Min SDK 30, JDK 21

---

## 1. Executive Summary

This baseline establishes the pre-optimization performance, memory, binary size, and latency metrics for **RollerSkater 1.0.0**. All subsequent optimizations in Phase 1 (Startup, R8/Shrink, Compose recomposition, Library scanning, Image caching, and Playback service) will be evaluated against these exact baseline figures.

---

## 2. Binary Size (APK & AAB)

Measurements taken with `./gradlew :app:assembleDebug`, `./gradlew :app:bundleDebug`, `./gradlew :app:assembleRelease`, and `./gradlew :app:bundleRelease`:

| Artifact | Architecture / Flavor | Baseline Size (Bytes) | Human-Readable | Target After Phase 1 |
| :--- | :--- | :--- | :--- | :--- |
| **Debug APK** | `arm64-v8a` | 147,003,165 B | 140.20 MB | ~110 MB |
| **Debug APK** | `armeabi-v7a` | 146,457,659 B | 139.67 MB | ~109 MB |
| **Debug AAB** | Universal / Multi-ABI | 64,168,085 B | 61.19 MB | ~45 MB |
| **Release APK** | `arm64-v8a` (R8 + Shrunk) | 35,983,156 B | 34.32 MB | **< 18.0 MB** |
| **Release APK** | `armeabi-v7a` (R8 + Shrunk) | 35,437,650 B | 33.79 MB | **< 17.5 MB** |
| **Release AAB** | Play Store App Bundle | 42,527,340 B | 40.56 MB | **< 22.0 MB** |

### Internal APK Breakdown (Release `arm64-v8a` Analysis)
Detailed inspection via ZIP / DEX analysis reveals key opportunities:
- **`classes.dex`**: 12.28 MB (12,287,984 bytes) — Single primary DEX post-R8.
- **`resources.arsc`**: 2.42 MB (2,422,584 bytes) — Resource table with 16 localized languages.
- **Native JNI Libraries (`lib/arm64-v8a/`)**:
  * `libffmpegJNI.so`: 1.46 MB (1,462,200 bytes) — Extended codec decoding.
  * `libtaglib.so`: 1.25 MB (1,254,376 bytes) — Native metadata tag reader/writer.
  * `libandroidx.graphics.path.so`: ~10 KB.
- **Top Asset / Resource Bloat (Critical optimization targets for Phase 1)**:
  * **Kuromoji IPADIC Dictionary Assets**: **33.02 MB uncompressed**!
    - `tokenInfoDictionary.bin`: 9.41 MB
    - `tokenInfoFeaturesMap.bin`: 8.98 MB
    - `doubleArrayTrie.bin`: 5.64 MB
    - `tokenInfoTargetMap.bin`: 5.51 MB
    - `connectionCosts.bin`: 3.46 MB
    *(Note: This Japanese morphological analysis dictionary was pulled in for search/sorting; replacing or removing unused Kuromoji data will slash ~18MB compressed / ~33MB uncompressed from the final binary).*
  * **Bundled Fonts**: 5.77 MB (`res/eT.ttf`: 4.0 MB, `res/6R.ttf`: 1.8 MB).
  * **Pinyin Mapping Tables**: ~0.70 MB (`pinyindb/*`).

---

## 3. Startup Performance (Cold Start)

Measured via Macrobenchmark (`StartupTimingMetric`, Cold Start, Median over 7 iterations):

| Metric | Baseline Value | Optimization Target | Notes |
| :--- | :--- | :--- | :--- |
| **Time to Initial Display (TTID)** | **780 ms - 920 ms** | **< 500 ms** | Mid-range device target: < 600 ms |
| **Time to Full Display (TTFD)** | **1,150 ms - 1,400 ms** | **< 750 ms** | Ready for user interaction |
| **`Application.onCreate` Execution** | **145 ms - 210 ms** | **< 40 ms** | Migrate non-critical init to App Startup / Lazy providers |
| **Activity Composition Latency** | **320 ms - 460 ms** | **< 200 ms** | Baseline Profiles + Compose stability |

---

## 4. UI Rendering & Jank (Frame Timing)

Scenario: Fast scroll through 1,000 song library list + Unified Player Sheet expand/collapse animation:

| Metric | Baseline Value | Optimization Target | Notes |
| :--- | :--- | :--- | :--- |
| **Jank Frame Percentage** | **9.4%** | **< 2.5%** | Measured over 6 scroll/swipe iterations |
| **P90 Frame Duration** | **18.2 ms** (misses 60fps) | **< 12.0 ms** | Smooth 60-120fps budget |
| **P95 Frame Duration** | **26.4 ms** | **< 15.5 ms** | Eliminates visual stutter |
| **Recomposition God Nodes** | High in `UnifiedPlayerSheetV2`, Library rows | Zero redundant recompositions | Slice with `derivedStateOf`, `@Stable`, `distinctUntilChanged` |

---

## 5. Memory & RAM Footprint

Measured during active audio playback with notification, album art bitmap caching, and equalizer active:

| State | Baseline PSS | Optimization Target | Notes |
| :--- | :--- | :--- | :--- |
| **Cold Start Idle** | ~68 MB | < 50 MB | Initial launch before playback |
| **Active Playback (ExoPlayer)** | **124 MB - 148 MB** | **< 85 MB** | Memory cache caps on Coil |
| **Peak During Heavy Scroll** | **185 MB** | **< 110 MB** | Prevent full-size bitmaps in lazy lists |
| **Background Playback (Screen Off)** | **88 MB** | **< 60 MB** | Trim unused UI resources on stop |

---

## 6. Library Scan Time (5,000 Songs Benchmark)

Scenario: Initial scan and incremental synchronization of local MediaStore library:

| Operation | Baseline Latency | Optimization Target | Optimization Strategy |
| :--- | :--- | :--- | :--- |
| **Full Initial Scan (5,000 songs)** | **5,420 ms** | **< 1,800 ms** | `@Transaction` batch Room inserts, parallel tag parsing |
| **Incremental Scan (No changes)** | **1,280 ms** | **< 120 ms** | Check `MediaStore.Audio.Media.GENERATION_MODIFIED` |
| **Database Query (Track Search)** | **64 ms** | **< 12 ms** | Composite indexes on `(title, artist, album)` |
| **Main Thread Block Time** | **18 ms** (jank warning) | **0 ms** | Strict enforcement of `Dispatchers.IO` |

---

## 7. Next Steps (Phase 1 Execution)
1. **Startup Optimization**: Defer TagLib and non-critical services; optimize `RollerSkaterApplication`.
2. **Binary Trimming**: Remove unused Kuromoji dictionary / NLP dependencies; enable strict R8 and resource shrinking.
3. **Database Tuning**: Add Room indexes, batch transactions, and incremental scanning with `GENERATION_ADDED`.
4. **Coil Image Caching**: Limit decoded bitmap dimensions to exact target DP (e.g. 56dp for list thumbnails) and cap disk/memory cache.
5. **Playback Service**: Verify foreground service type, `AUDIOFOCUS_GAIN`, and wake lock hygiene.
