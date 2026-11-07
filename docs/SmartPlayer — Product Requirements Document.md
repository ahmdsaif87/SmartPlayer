# SmartPlayer — Product Requirements Document

**Internal Code:** `SmartPlayer` | **Platform:** Android (Kotlin, native) | **Doc Status:** v1.0 — Implementation-Ready | **Audience:** Engineering team and downstream AI coding agents

> **Normative language:** MUST / MUST NOT / SHOULD / MAY follow RFC 2119. Every `MUST` is a testable acceptance condition. Priorities: **P0** = release blocker, **P1** = post-MVP.

---

## 1. EXECUTIVE SUMMARY & OBJECTIVES

### 1.1 Product Name Proposal

| Item | Value |
| --- | --- |
| Internal code | `SmartPlayer` |
| Proposed market names | **SmartPlayer** |
| Application ID | `id.smartplayer` |
| Tagline | "Folder berantakan, perpustakaan rapi. Serial, anime, dan film tanpa internet." |

### 1.2 Problem Statement

General-purpose media players with library features (e.g., Nova Player, VLC) depend on one of two strategies: (a) **online metadata scraping** (TMDB/TVDB lookups keyed on the filename) or (b) **rigid filename grammars** (`Title.S01E01.ext`). Both fail on diverse downloaded content, including Indonesian fansubs, encode-group releases, and non-standard video filenames:

| Failure mode | Example filename | Observed result |
| --- | --- | --- |
| Leading release-group bracket | `[Kusonime] Jujutsu Kaisen - 12 [1080p][x265].mkv` | Scraper queries the full string including `[Kusonime]` and returns zero matches |
| Stacked quality/codec tags | `[Oploverz] Spy x Family - 05 [BD 1080p][HEVC 10bit].mkv` | Tags pollute the title query; match fails |
| No `SxxExx` token | `Naruto Shippuden Episode 123 Sub Indo 480p.mp4` | Parser cannot extract season/episode; file skipped |
| Underscore/dot-delimited names | `Sousou_no_Frieren_-_03_[720p].mkv` or `The_Bear_S02E01_1080p.mkv` | Treated as one opaque token |
| Site watermark suffix | `... - Samehadaku.mp4` | Trailing site name merged into the title |
| Standard TV Show variant | `Breaking Bad S01E05 720p HDTV.mkv` | Requires clean title extraction without cluttering tags |
| Standalone Movies | `Your Name (2016) 1080p BluRay.mkv` | Should be categorized properly as a standalone movie, not mislabeled as episode |
| No network / metered data | Any | Online scraping times out; library stays empty |

The user-visible symptom is **"No files to process"** or an empty/"Unknown" library, even though dozens of valid videos exist in local storage folders. The root cause is architectural: *identity resolution is outsourced to a remote service and a brittle grammar*. SmartPlayer MUST replace this with a **deterministic, local, tag-aware parsing pipeline** supporting general series, anime, and movies, whose output is persisted in Room (SQLite) and never requires a network call.

### 1.3 Target Audience & Core Goal

- **Primary:** Users who download media for offline watching (*anak kos*, travelers, commuters) with limited mobile data, shared/slow Wi-Fi, 3–6 GB RAM devices, and 32–128 GB storage who store videos across common storage folders (`Download/`, `Movies/`, `Video/`).
- **Secondary:** Collectors maintaining large local libraries (1,000–10,000 files) of TV series, anime, and movies with inconsistent naming across diverse sources.
- **Core goal:** Zero-configuration, **offline-first** library organization with a modern, streaming-like UX inspired by YouTube and Netflix (see `docs/DESIGN.md`). After granting storage media permission, the user sees a beautifully categorized library (Continue Watching, Series, Movies) within seconds, with resume-where-you-left-off playback.

### 1.4 Objectives & Key Results

| ID | Objective | Measurable target |
| --- | --- | --- |
| O1 | Correct series grouping | ≥ 95% of the golden corpus (§6.1) parsed to the expected `(title, season, episode)` |
| O2 | Offline-First with AI Enrichment | 100% playable and organized offline; smart AI enrichment (Gemini API) runs asynchronously when online/configured |
| O3 | Fast first library | 1,000 files indexed and displayed in ≤ 3 s on a reference mid-range device |
| O4 | Lightweight | Release APK (arm64 split) ≤ 12 MB (including Coil and Gemini AI client); idle PSS ≤ 80 MB |
| O5 | Reliable resume | Playback position restored within ±1 s after process death |

### 1.5 Non-Goals (v1)

Streaming/cloud playback, torrent downloading, user cloud accounts, analytics/crash-reporting SDKs, ad networks, DRM, and music-only playback. Online network surface is strictly constrained to AI metadata enrichment (Gemini API) and image poster caching.

---

## 2. TECHNICAL STACK & ARCHITECTURE ARCHETYPE

### 2.1 Stack Definition

| Layer | Decision |
| --- | --- |
| Language | Kotlin (latest stable), Coroutines + Flow |
| Min / Target SDK | `minSdk 26`, `targetSdk` = latest stable |
| Architecture | **MVVM with unidirectional data flow (MVI-style)**: `UiState` (immutable data class, exposed via `StateFlow`) + `UiEvent`/intent functions on the ViewModel |
| UI | Jetpack Compose + Material 3 (`androidx.compose.material3`); Navigation Compose for Screens |
| Image Loading | **Coil Compose** (`io.coil-kt:coil-compose`) with automatic local disk & memory caching for offline poster viewing |
| Player | AndroidX **Media3 ExoPlayer** (`media3-exoplayer`, `media3-ui`) with default hardware `MediaCodec` renderers |
| Persistence | **Room** (SQLite) with KSP; `Flow`-returning DAOs; no other persistence library |
| AI / Enrichment | **Gemini API** (`com.google.ai.client.generativeai:generativeai`) for entity resolution, Indonesian synopsis, genres, and poster discovery |
| Background work | Coroutines (`Dispatchers.IO`) driven by `ScanCoordinator` and `MetadataEnricher`; **WorkManager** (`CoroutineWorker`) for optional Wi-Fi background sync |
| DI | **Manual DI** via an `AppContainer` held by the `Application` class (no Hilt/Dagger/Koin; saves APK size and build time) |
| Build | Gradle Kotlin DSL, version catalog, R8 full mode, resource shrinking, ABI/density splits via AAB |

### 2.2 Execution Rule: Offline-First with Controlled AI Enrichment

1. The app MUST function completely offline. Playback, local indexing, and regex grouping MUST never depend on network availability or API key presence.
2. Network access is restricted to:
   - `android.permission.INTERNET`
   - `android.permission.ACCESS_NETWORK_STATE` (to honor the "Sync only over Wi-Fi" user setting)
3. Transitive dependencies MUST NOT include Firebase, Play Services, ad networks, or analytics tracking SDKs.
4. Media access permissions only: `READ_MEDIA_VIDEO` (API 33+) and `READ_EXTERNAL_STORAGE` with `android:maxSdkVersion="32"`.
5. `android:allowBackup="false"`, `android:usesCleartextTraffic="false"`.

### 2.3 Module & Package Layout (single Gradle module `:app`)

```
id.smartplayer
├── SmartPlayerApp.kt             // Application; owns AppContainer
├── di/AppContainer.kt            // manual DI: db, repositories, scanner, parser, aiService
├── data/
│   ├── db/  AppDatabase.kt, SeriesDao.kt, EpisodeDao.kt, PlaybackStateDao.kt, entities/
│   ├── scan/ MediaStoreScanner.kt, SafScanner.kt (P1), ScanCoordinator.kt
│   ├── parse/ FilenameParser.kt, NoiseDictionary.kt, SeriesKeyFactory.kt, ParsedMedia.kt
│   ├── ai/ AiMetadataService.kt, GeminiAiClient.kt, MetadataEnricher.kt
│   └── repo/ LibraryRepository.kt, PlaybackRepository.kt, SettingsRepository.kt
├── ui/
│   ├── nav/ AppNavGraph.kt
│   ├── dashboard/ DashboardScreen.kt, DashboardViewModel.kt
│   ├── series/ SeriesDetailScreen.kt, SeriesDetailViewModel.kt
│   ├── player/ PlayerActivity.kt, PlayerViewModel.kt
│   ├── settings/ SettingsScreen.kt, SettingsViewModel.kt
│   ├── common/ ThumbnailLoader.kt, PermissionGate.kt, Theme.kt
│   └── design/ (Cinema Dark tokens, components, gestures)
└── assets/noise_tokens.json      // configurable tag/release-group dictionary
```

**Dependency rule:** `ui → data/repo → data/{db,scan,parse,ai}`. The `parse` package MUST be pure Kotlin (no Android imports) so it is JVM-unit-testable.

### 2.4 Data-Flow Overview

```
MediaStore query ──► ScanCoordinator (IO) ──► FilenameParser (pure) ──► SeriesKeyFactory
                                                                          │
                         Room (series, episodes, playback_state) ◄── batched transaction
                                    │ Flow                                │
                                    ▼                                     ▼
                               Repository ──► ViewModel ──► Compose UI   MetadataEnricher (Async / Online)
                                                                          │ (Gemini API)
                                                                          ▼
                                                                 Update Poster & Synopsis (Room)
```

---

## 3. DETAILED FUNCTIONAL SPECIFICATIONS

### 3.1 Storage Scanning & Discovery

#### 3.1.1 Permission Flow (P0)

- On first launch show `PermissionGate`: rationale text, then request `READ_MEDIA_VIDEO` (API ≥ 33) or `READ_EXTERNAL_STORAGE` (API ≤ 32) via `ActivityResultContracts.RequestPermission`.
- If denied permanently, display a state with an "Open Settings" button (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`). The app MUST NOT request `MANAGE_EXTERNAL_STORAGE`.
- On Android 14+ partial grants (`READ_MEDIA_VISUAL_USER_SELECTED`), the app treats the library as limited and shows a banner offering full access.

#### 3.1.2 MediaStore Query Specification (P0)

`MediaStoreScanner` MUST query `MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)` with this projection:

`_ID, DISPLAY_NAME, SIZE, DURATION, DATE_ADDED, DATE_MODIFIED, MIME_TYPE, RELATIVE_PATH (API 29+), DATA, VOLUME_NAME (API 29+), IS_PENDING (API 29+), WIDTH, HEIGHT`

**Selection (API ≥ 29):**

```sql
(
  RELATIVE_PATH LIKE 'Download/%' ESCAPE '\'
  OR RELATIVE_PATH LIKE 'Movies/%' ESCAPE '\'
  OR RELATIVE_PATH LIKE 'Video/%' ESCAPE '\'
  OR RELATIVE_PATH LIKE 'Videos/%' ESCAPE '\'
  OR RELATIVE_PATH LIKE 'DCIM/%' ESCAPE '\'
)
AND IS_PENDING = 0
AND SIZE >= ?                        -- default 5_242_880 (5 MB), filters samples/trailers/thumbnails
AND (MIME_TYPE IN ('video/mp4','video/x-matroska','video/webm')
     OR LOWER(DISPLAY_NAME) LIKE '%.mkv' OR LOWER(DISPLAY_NAME) LIKE '%.mp4' OR LOWER(DISPLAY_NAME) LIKE '%.webm')
```

On API 26–28, replace the `RELATIVE_PATH` clause with matching `DATA LIKE` clauses (`'%/Download/%'`, `'%/Movies/%'`, `'%/Video/%'`, `'%/Videos/%'`, `'%/DCIM/%'`). Folders containing `.nomedia` or internal app caches are skipped automatically by MediaStore. The extension fallback exists because some OEM MediaStore providers mislabel `.mkv` as `application/octet-stream`. Sort order: `_ID ASC` (stable for diffing). The cursor MUST be read column-index-cached (`getColumnIndexOrThrow` once, outside the loop) and closed via `use {}`.

#### 3.1.3 Incremental Scan Algorithm (P0)

1. Load `Map<mediaStoreId, EpisodeSnapshot(dateModifiedSec, sizeBytes)>` from Room (one query).
2. Iterate the cursor. For each row: if id is unknown **or** `dateModified`/`size` changed → parse and add to `upsertBatch`; otherwise mark seen only. Track `seenIds`.
3. Flush `upsertBatch` in Room transactions of **200 rows** (`withTransaction`).
4. After iteration: `deleteByMediaStoreIdNotIn(seenIds)` (chunked to respect SQLite's 999-variable limit; implement via a temp `scan_seen` table or chunked deletes).
5. Delete `series` rows with zero episodes. Purge `playback_state` rows with `updatedAtEpochMs` older than 90 days **and** no matching episode fingerprint.
6. Recompute `series.lastUpdatedSec` and `series.coverMediaStoreId` for affected series only.

**Triggers:** (a) first successful permission grant, (b) app start (cold), (c) pull-to-refresh on Dashboard, (d) `ContentObserver` on the video collection URI (debounced 2 s), (e) optional periodic `WorkManager` job (P1, 12 h, charging + idle). Concurrent scan requests MUST be coalesced by a `Mutex`/`conflate` channel in `ScanCoordinator`; only one scan runs at a time.

#### 3.1.4 SAF Fallback (P1)

For files in directories with `.nomedia` or on volumes MediaStore skips, offer "Add folder" using `ACTION_OPEN_DOCUMENT_TREE`. Persist the grant with `takePersistableUriPermission`. `SafScanner` traverses via `DocumentsContract.buildChildDocumentsUriUsingTree` (raw `ContentResolver.query`, NOT `DocumentFile`, for speed) using an iterative stack (no recursion) and emits the same `RawMediaFile` model as `MediaStoreScanner`. Such rows use `mediaStoreId = -hash(documentUri)` (negative to avoid collisions).

#### 3.1.5 Scan Output Contract

```kotlin
data class RawMediaFile(
  val mediaStoreId: Long, val contentUri: String, val absolutePath: String,
  val relativePath: String, val displayName: String, val sizeBytes: Long,
  val durationMs: Long, val dateAddedSec: Long, val dateModifiedSec: Long
)
```

`ScanCoordinator` exposes `StateFlow<ScanState>` = `Idle | Scanning(processed:Int, total:Int) | Done(added:Int, removed:Int) | Error(cause)`. The Dashboard renders a non-blocking `LinearProgressIndicator` from this flow; existing library content MUST remain interactive during scanning.

### 3.2 Local Regex & Parsing Engine

`FilenameParser` is a **pure, deterministic, stateless** function:

```kotlin
interface FilenameParser {
  fun parse(displayName: String, parentFolderName: String?): ParsedMedia
}
data class ParsedMedia(
  val coreTitle: String,            // human-readable, e.g. "Spy x Family"
  val seasonNumber: Int,            // default 1
  val episodeNumber: Int?,          // null => standalone/movie/unparsed
  val episodeVersion: Int?,         // v2 => 2
  val releaseGroup: String?,
  val resolution: String?,          // "1080p", "720p", "480p", "2160p"
  val year: Int?,
  val confidence: Confidence        // HIGH | MEDIUM | LOW
)
```

#### 3.2.1 Pipeline (executed strictly in this order)

**Step 0 — Extension strip.** Remove final `\.(mkv|mp4|webm)$` (case-insensitive).

**Step 1 — Unicode normalization.** NFKC normalize; replace full-width brackets/dashes; replace `\u2013 \u2014` with `-`.

**Step 2 — Delimiter normalization.** Replace `_` with a space. Scene-style dots: if the string contains **no spaces** and **≥ 2 dots**, replace all `.` with spaces; otherwise keep dots (preserves `Dr. Stone`, `Kaiju No. 8`).

**Step 3 — Bracket extraction.** Scan `BRACKET = Regex("""[\[\(\{]([^\]\)\}]*)[\]\)\}]""")`. For each group, classify by first match in this order, then remove it from the working string:

| Order | Class | Test on inner text | Effect |
| --- | --- | --- | --- |
| 1 | CRC32 | `^[0-9A-Fa-f]{8}$` | discard |
| 2 | Year | `^(19\|20)\d{2}$` | set `year` |
| 3 | Bracket episode | `^(\d{1,4})(v(\d))?$` | episode candidate (rule E6) |
| 4 | Season info | matches a season pattern (§3.2.3) | set season |
| 5 | Quality/tech | contains any token from `QUALITY_TOKENS` | set `resolution` if found; discard |
| 6 | Known group/site | token ∈ `groups` dictionary (case-insensitive) | set `releaseGroup` (if empty); discard |
| 7 | Positional group | match starts at index 0 | set `releaseGroup`; discard |
| 8 | Other | — | discard as noise |

**Step 4 — Domain/watermark strip.** Remove `(?i)(?:www\.)?[a-z0-9-]+\.(?:com\|net\|org\|id\|co\|me\|moe\|info\|my\.id)\b` and trailing `\s-\s*<knownSite>\s*$` where `knownSite` ∈ dictionary.

**Step 5 — Resolution extraction (unbracketed).** `RES = (?i)\b(2160|1440|1080|720|576|480|360)[pi]\b|\b\d{3,4}x\d{3,4}\b|\b4k\b`. Record if `resolution` unset. `WxH` patterns (e.g. `1920x1080`) MUST be removed **before** episode rule E2 runs.

**Step 6 — Episode extraction.** Apply rules in priority order; the **first** rule that matches wins. The matched span's start index defines the **tail boundary**.

| Rule | Regex (Kotlin raw string, case-insensitive) | Groups | Notes |
| --- | --- | --- | --- |
| E1 | `\bS(\d{1,2})\s?[-._ ]?E(\d{1,4})(?:v(\d))?\b` | season, ep, ver | standard SxxExx |
| E2 | `\b(\d{1,2})x(\d{2,3})\b` | season, ep | e.g. `2x05` |
| E3 | `\b(?:episode\|eps?\.?\|ep\.?)\s*[-._]?\s*(\d{1,4})(?:v(\d))?\b` | ep, ver | `Episode 12`, `Eps 07`, `Ep.3` |
| E4 | `\bE(\d{2,4})(?:v(\d))?\b` | ep, ver | bare `E05` |
| E5 | `\s-\s(\d{1,4})(?:v(\d))?(?=\s\|$)` — take the **last** match | ep, ver | fansub canonical `Title - 05` |
| E6 | bracket-episode candidate from Step 3 | ep, ver | `Title [05]` |
| E7 | `(?<=\s)(\d{1,4})(?:v(\d))?\s*$` on the working string **after** noise removal; reject if value ∈ \[1900, 2099\] and no year set; reject if the preceding token is a roman numeral or `No.` | ep, ver | `Tonikaku Kawaii 2nd Season 08` |

**Step 7 — Tail discard.** Everything at or after the tail boundary is discarded (it contains tags such as `END`, `Batch`, `Sub Indo`, `FLAC`, `Dual Audio`). The remaining head string becomes the title candidate.

**Step 8 — Noise-token removal in the head** (only runs when no episode was found, or for unambiguous tokens appearing before the episode). Remove tokens matching the **unambiguous** dictionary classes below as whole words:

| Class | Pattern fragment |
| --- | --- |
| Video codec | `x\.?26[45]\|h\.?26[45]\|hevc\|avc\|av1\|xvid` |
| Bit depth | `10[\s-]?bits?\|8[\s-]?bits?\|hi10p?` |
| Source | `bd(?:rip)?\|blu[\s-]?ray\|bdmv\|web[\s-]?(?:dl\|rip)\|hdtv\|dvd(?:rip)?\|tvrip` |
| Audio | `aac(?:2\.0)?\|flac\|ac3\|eac3\|dts\|opus\|dual[\s-]?audio` |
| Language/sub | `sub[\s-]?indo(?:nesia)?\|indo[\s-]?sub\|hardsub\|softsub\|esub\|multi(?:ple)?[\s-]?subs?` |
| Release flags | `batch\|uncensored\|uncut\|repack\|proper` |

**Ambiguous words** (`end`, `final`, `raw`, `complete`, `sub`, `special`) MUST NOT be removed from the head; they may be legitimate title words (*Final Fantasy*, *The End of Evangelion*). They are only discarded when inside the tail or inside brackets.

**Step 9 — Season extraction.** See §3.2.3. Season tokens found in the head are removed from the title.

**Step 10 — Title cleanup.** Collapse whitespace; trim leading/trailing `[\s\-_.:]+`; remove dangling `-`; preserve internal `-` separators (e.g. `Kimetsu no Yaiba - Yuukaku-hen`); preserve original casing. If the result is empty, or equals a generic token (`episode`, `video`, `vid`), use **folder fallback**.

**Step 11 — Folder fallback.** Run Steps 1–2 and 8–10 on `parentFolderName`. Skip if the folder ∈ `{Download, Downloads, Movies, Video, Videos, Anime, DCIM}` (case-insensitive). Confidence is capped at MEDIUM.

#### 3.2.2 Confidence Rules

- **HIGH:** episode via E1–E5 and title non-empty.
- **MEDIUM:** episode via E6/E7, or title from folder fallback.
- **LOW:** no episode number (treated as standalone / movie / special; `episodeNumber = null`).

A **sibling-consensus post-pass** runs per directory: for MEDIUM rows from rule E7, if ≥ 3 sibling files in the same directory share the identical title key and yield a monotonic episode sequence, promote to HIGH; if the file is the only one with that title key, keep MEDIUM. This resolves ambiguity like `Mob Psycho 100` (title digit) vs. episode digit.

#### 3.2.3 Season Rules

| Pattern (case-insensitive) | Season |
| --- | --- |
| from E1/E2 capture | captured value |
| `\bS(\d{1,2})\b` (standalone, e.g. `Title S6 - 05`) | captured value |
| `\bseason\s*(\d{1,2})\b` | captured value |
| `\b(\d{1,2})(?:st\|nd\|rd\|th)\s+season\b` | captured value |
| none | **1** |

Roman numerals (`II`, `III`) and `Part N`/`Cour N` are **kept in the title** (they usually identify distinct cours/arcs, e.g. *Mob Psycho 100 III*). They do not set `seasonNumber`.

#### 3.2.4 Series Identity Key

`SeriesKeyFactory.key(coreTitle, season)`:

1. NFKC → lowercase (`Locale.ROOT`).
2. Replace every char not in `[\p{L}\p{N}]` with a single space; collapse whitespace; trim.
3. Append `"|s" + season`.

Example: `Re:Zero kara Hajimeru Isekai Seikatsu`, season 2 → `re zero kara hajimeru isekai seikatsu|s2` (the `:` becomes a space, whitespace collapses). Two files belong to the same series iff their keys are byte-equal. *(P1: fuzzy merge — Jaro-Winkler ≥ 0.93 **and** same parent directory.)*

#### 3.2.5 Example Input → Output Map

| # | Input filename | `coreTitle` | S | E | Group | Res | Conf |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | `[Oploverz] Boku no Hero Academia S6 - 05 [720p].mp4` | Boku no Hero Academia | 6 | 5 | Oploverz | 720p | HIGH |
| 2 | `[Kusonime] Kimetsu no Yaiba - Yuukaku-hen - 07 [BD][1080p][HEVC 10bit].mkv` | Kimetsu no Yaiba - Yuukaku-hen | 1 | 7 | Kusonime | 1080p | HIGH |
| 3 | `[SubsPlease] Spy x Family - 12 (1080p) [A1B2C3D4].mkv` | Spy x Family | 1 | 12 | SubsPlease | 1080p | HIGH |
| 4 | `Sousou_no_Frieren_-_03_[720p].mkv` | Sousou no Frieren | 1 | 3 | — | 720p | HIGH |
| 5 | `One.Piece.S01E1089.1080p.WEB-DL.x264.mkv` | One Piece | 1 | 1089 | — | 1080p | HIGH |
| 6 | `[Anime Time] Re:Zero kara Hajimeru Isekai Seikatsu S02E05v2 [BD 1080p][HEVC 10bit x265][Dual Audio].mkv` | Re:Zero kara Hajimeru Isekai Seikatsu | 2 | 5 (v2) | Anime Time | 1080p | HIGH |
| 7 | `Naruto Shippuden Episode 123 Sub Indo 480p - Samehadaku.mp4` | Naruto Shippuden | 1 | 123 | — | 480p | HIGH |
| 8 | `[Kusonime] Mob Psycho 100 III - 04 [720p].mkv` | Mob Psycho 100 III | 1 | 4 | Kusonime | 720p | HIGH |
| 9 | `[Oploverz] Dr. Stone - New World - 11 (WEBRip 1080p).mp4` | Dr. Stone - New World | 1 | 11 | Oploverz | 1080p | HIGH |
| 10 | `Tonikaku Kawaii 2nd Season 08.mkv` | Tonikaku Kawaii | 2 | 8 | — | — | MEDIUM |
| 11 | `[Erai-raws] Kaiju No. 8 - 03 [1080p][Multiple Subtitle].mkv` | Kaiju No. 8 | 1 | 3 | Erai-raws | 1080p | HIGH |
| 12 | `Your Name (2016) 1080p BluRay.mkv` | Your Name | 1 | null | — | 1080p | LOW (year=2016) |
| 13 | `Episode 05.mkv` in folder `Download/Frieren/` | Frieren | 1 | 5 | — | — | MEDIUM |
| 14 | `[Meownime] Oshi no Ko - 09 [x265 10bit][480p] - Meownime.com.mkv` | Oshi no Ko | 1 | 9 | Meownime | 480p | HIGH |

#### 3.2.6 Noise Dictionary (`assets/noise_tokens.json`)

The release-group/site list MUST be data-driven so it can be extended without code changes:

```json
{
  "groups": ["kusonime","oploverz","samehadaku","anoboy","meownime","otakudesu","nimegami","neonime","awsubs","kuramanime","animeindo","nontonanimeid","animesail","anichin","erai-raws","subsplease","judas","horriblesubs","commie","anime time"],
  "qualityTokens": ["2160p","1440p","1080p","720p","576p","480p","360p","4k","hevc","x264","x265","h264","h265","10bit","8bit","bd","bdrip","bluray","web-dl","webrip","hdtv","aac","flac","dual audio","multiple subtitle"]
}
```

Loaded once at startup into compiled `Regex`/`Set` objects held by `NoiseDictionary` (singleton in `AppContainer`). All regexes MUST be precompiled `val`s (never constructed per file). Unknown bracketed groups at index 0 are still treated as release groups (Step 3, rule 7), so the dictionary is an optimization and disambiguator, not a gate.

### 3.3 Room DB Schema & Entity Relations

`@Database(entities = [SeriesEntity, EpisodeEntity, PlaybackStateEntity], version = 1, exportSchema = true)`. WAL journal mode (Room default). Schema JSON MUST be committed to VCS.

#### 3.3.1 Entity Relationship

```
series (1) ------< episodes (N)          FK episodes.seriesId -> series.id ON DELETE CASCADE
episodes.fingerprint ---- playback_state.fingerprint   (logical link, no FK)
```

`playback_state` is deliberately **not** FK-bound to `episodes`, so progress survives a file being temporarily removed, renamed, or re-indexed (for example, moved between folders by the user). It is keyed by a content-stable fingerprint.

#### 3.3.2 Entities

```kotlin
@Entity(tableName = "series",
  indices = [Index(value = ["seriesKey"], unique = true), Index("lastUpdatedSec")])
data class SeriesEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val seriesKey: String,             // e.g. "boku no hero academia|s6"
  val displayTitle: String,          // user-visible
  val seasonNumber: Int,
  val coverMediaStoreId: Long?,      // episode used for thumbnail fallback
  val lastUpdatedSec: Long,          // MAX(episodes.dateAddedSec)
  val posterUrl: String? = null,     // Remote/cached poster URL (via AI / metadata)
  val backdropUrl: String? = null,   // Hero backdrop image URL
  val synopsis: String? = null,      // AI-generated / enriched Indonesian synopsis
  val genres: String? = null,        // Comma-separated or JSON list of genres
  val totalSeasonEpisodes: Int? = null, // Official total episodes in season (e.g. 28)
  val isMovie: Boolean = false       // Standalone movie flag
)

@Entity(tableName = "episodes",
  foreignKeys = [ForeignKey(SeriesEntity::class, ["id"], ["seriesId"], onDelete = ForeignKey.CASCADE)],
  indices = [Index(value = ["mediaStoreId"], unique = true),
             Index("absolutePath"),
             Index(value = ["seriesId", "episodeNumber"]),
             Index("fingerprint")])
data class EpisodeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val seriesId: Long,
  val mediaStoreId: Long,
  val contentUri: String,            // authoritative for playback
  val absolutePath: String,          // display/diagnostics; may be stale
  val relativePath: String,
  val displayName: String,           // raw filename
  val sizeBytes: Long,
  val durationMs: Long,
  val dateAddedSec: Long,
  val dateModifiedSec: Long,
  val episodeNumber: Int?,           // null => standalone
  val episodeVersion: Int?,
  val seasonNumber: Int,
  val releaseGroup: String?,
  val resolution: String?,
  val parseConfidence: String,       // HIGH | MEDIUM | LOW
  val fingerprint: String,           // sha1(lowercase(displayName) + "|" + sizeBytes)
  val episodeTitle: String? = null,  // Enriched title (e.g. "Petualangan Baru")
  val synopsis: String? = null       // Episode-level synopsis
)

@Entity(tableName = "playback_state",
  indices = [Index("updatedAtEpochMs")])
data class PlaybackStateEntity(
  @PrimaryKey val fingerprint: String,
  val lastPlayedPosition: Long,      // ms
  val durationMs: Long,
  val isFinished: Boolean,
  val updatedAtEpochMs: Long
)
```

#### 3.3.3 Required DAO Contracts

```kotlin
@Dao interface SeriesDao {
  @Query("""
    SELECT s.id, s.displayTitle, s.seasonNumber, s.coverMediaStoreId, s.lastUpdatedSec,
           s.posterUrl, s.backdropUrl, s.synopsis, s.genres, s.totalSeasonEpisodes, s.isMovie,
           COUNT(e.id) AS episodeCount,
           COALESCE(SUM(CASE WHEN p.isFinished = 1 THEN 1 ELSE 0 END), 0) AS finishedCount
    FROM series s
    JOIN episodes e ON e.seriesId = s.id
    LEFT JOIN playback_state p ON p.fingerprint = e.fingerprint
    GROUP BY s.id
    ORDER BY s.lastUpdatedSec DESC""")
  fun observeDashboard(): Flow<List<SeriesSummary>>
  @Query("SELECT * FROM series WHERE seriesKey = :key LIMIT 1") suspend fun findByKey(key: String): SeriesEntity?
  @Query("UPDATE series SET posterUrl = :poster, backdropUrl = :backdrop, synopsis = :synopsis, genres = :genres, totalSeasonEpisodes = :totalEps WHERE id = :seriesId")
  suspend fun updateMetadata(seriesId: Long, poster: String?, backdrop: String?, synopsis: String?, genres: String?, totalEps: Int?)
  @Query("DELETE FROM series WHERE id NOT IN (SELECT DISTINCT seriesId FROM episodes)") suspend fun deleteOrphans()
}

@Dao interface EpisodeDao {
  @Query("""
    SELECT e.*, p.lastPlayedPosition AS resumePositionMs, COALESCE(p.isFinished,0) AS isFinished
    FROM episodes e LEFT JOIN playback_state p ON p.fingerprint = e.fingerprint
    WHERE e.seriesId = :seriesId
    ORDER BY (e.episodeNumber IS NULL) ASC, e.episodeNumber ASC, e.episodeVersion ASC, e.displayName COLLATE NOCASE ASC""")
  fun observeEpisodes(seriesId: Long): Flow<List<EpisodeRow>>
  @Query("SELECT mediaStoreId, dateModifiedSec, sizeBytes FROM episodes") suspend fun snapshots(): List<EpisodeSnapshot>
  @Upsert suspend fun upsertAll(items: List<EpisodeEntity>)
}

@Dao interface PlaybackStateDao {
  @Upsert suspend fun save(state: PlaybackStateEntity)
  @Query("SELECT * FROM playback_state WHERE fingerprint = :fp") suspend fun get(fp: String): PlaybackStateEntity?
  @Query("DELETE FROM playback_state WHERE updatedAtEpochMs < :cutoff AND fingerprint NOT IN (SELECT fingerprint FROM episodes)") suspend fun purgeStale(cutoff: Long)
}
```

#### 3.3.4 Duplicate-Episode Rule

If two files in one series share `(seasonNumber, episodeNumber)` (for example, 720p and 1080p copies), both rows are kept; `observeEpisodes` orders by `episodeVersion` then name. The detail screen renders both with a resolution chip. The **resume** action targets the file with the most recent `updatedAtEpochMs`, else the highest resolution.

#### 3.3.5 Finished Semantics

A position is persisted as `isFinished = true` and `lastPlayedPosition = 0` when `position >= 0.95 x duration` **or** `duration - position <= 90 000 ms`. Positions under 5 000 ms are persisted as `0` (not "in progress").

---

## 4. USER EXPERIENCE (UX) & FLOW SPECIFICATIONS

> **Design Reference:** Detailed visual tokens, color palettes, wireframes, and component systems follow the YouTube × Netflix design language documented in [`docs/DESIGN.md`](file:///home/ahmadsaif/project/SmartPlayer/docs/DESIGN.md).

### 4.1 Navigation Graph

`Dashboard` -> `SeriesDetail(seriesId)` -> `PlayerActivity(episodeId, seriesId)` (separate Activity, landscape, launched with an explicit `Intent`). Back from Player returns to `SeriesDetail`, which re-observes Room and reflects new progress without manual refresh.

### 4.2 Screen 1 — Dashboard (Netflix & YouTube Hybrid) (P0)

- **Layout & Visuals:** Immersive Cinema Dark theme (`#0A0A0C` background).
- **Top Bar:** App title `SmartPlayer`, real-time interactive **Search Bar** (instantly filters titles across series and movies), and rescan trigger button. Thin non-blocking `LinearProgressIndicator` below the bar during scanning.
- **Filter Chips (Ala YouTube):** Horizontal scrollable filter pills:
  - `Semua` (All)
  - `Sedang Ditonton` (Continue Watching / In Progress)
  - `Belum Selesai` (Unfinished / Incomplete)
  - `Serial` (Episodic Series)
  - `Film` (Standalone Movies / Specials)
  - `Selesai` (Completed)
- **"Lanjutkan Menonton" Section (Ala Netflix):**
  - Dedicated horizontal snap carousel pinned at the top for items with in-progress playback (`progress >= 5s` and `< 95%`).
  - Wide 16:9 cards showing high-quality video frame thumbnail, episode badge (`S1:E12`), remaining duration, and prominent accent progress bar. One-tap direct play button overlay.
- **Main Grid ("Semua Koleksi"):**
  - `LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 140.dp))`, 2 columns on phones in portrait, 3–4 columns on tablets/foldables.
  - Each item card shows: video thumbnail (`ContentScale.Crop`), `displayTitle` (2 lines, ellipsis), metadata chip (`1080p`, `S{n} • {episodeCount} eps` or `Movie • {duration}`), and linear progress indicator showing completion ratio.
- **Data & State:** `DashboardViewModel` observes `SeriesDao.observeDashboard()` combined with search query and selected filter chip via `StateFlow<DashboardUiState>`.
- **States:** `Loading` (skeleton cards shimmer), `Empty` (instructional empty state with scan CTA), `Content`, `PermissionRequired`.
- **Thumbnails:** `ThumbnailLoader` uses `ContentResolver.loadThumbnail(uri, Size(320,180), null)` on API 29+, and `MediaStore.Video.Thumbnails.getThumbnail` (legacy) below; decoded on `Dispatchers.IO`, cached per §5.2.

### 4.3 Screen 2 — Series / Movie Detail (Netflix Style) (P0)

- **Hero Header:** Large backdrop video frame thumbnail (16:9) with bottom dark gradient scrim. Title in bold typography, metadata summary (Season, episode count, resolution, watched count).
- **Primary CTA:** Prominent filled accent button:
  - Label: **"Lanjutkan Menonton (Eps {n})"** if in-progress.
  - Label: **"Mulai Nonton"** if unplayed.
  - Label: **"Tonton Ulang"** if all episodes finished.
- **Quick Action Row:** Secondary chips for "Tandai Selesai", "Buka Lokasi Folder".
- **Episode List Layout:**
  - Clean flat `LazyColumn` ordered by episode number ascending (standalone items last).
  - Each row item features: landscape video thumbnail on the left with duration badge and bottom progress bar (green checkmark when `isFinished`); on the right: episode title/number, resolution chip (`1080p`, `720p`), codec tag, and file size.
- **Tap Row:** Launches `PlayerActivity` at that specific episode with position restored.

### 4.4 Screen 3 — Immersive Video Player (YouTube × Netflix Ergonomics) (P0)

- **Activity Config:** `android:screenOrientation="sensorLandscape"`, `android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden"`, `FLAG_KEEP_SCREEN_ON` while playing. Immersive sticky mode via `WindowInsetsControllerCompat`.
- **YouTube-Inspired Gestures:**
  - **Double-tap Left / Right:** Seek $\pm 10$ seconds with animated circular ripple and "+10s" bubble.
  - **Vertical Swipe Left:** Screen Brightness adjustment with custom on-screen HUD slider.
  - **Vertical Swipe Right:** Audio Volume adjustment with custom on-screen HUD slider.
  - **Pinch to Zoom / Double-tap:** Toggle aspect ratio between `Fit to Screen`, `Crop / Zoom to Fill` (eliminates black bars), and `100% Original`.
  - **Horizontal Scrubbing Drag:** Fine scrubbing across timeline with floating timestamp preview.
- **Netflix-Inspired Controls & Features:**
  - **In-Player Episode Drawer / Bottom Sheet (`[ 📑 Episodes ]`):** Slide-over episode picker allowing users to switch episodes seamlessly **without closing the player**.
  - **Audio & Subtitle Track Selector (`[ 💬 ]`):** Pop-up selector for embedded MKV/MP4 audio tracks (e.g. Japanese, English) and subtitle tracks (Bahasa Indonesia, English, Off), as well as external sidecar `.srt`.
  - **Playback Speed Selector (`[ ⚡ Speed ]`):** 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x.
  - **Screen Lock Button (`[ 🔒 Lock ]`):** Disables touch gestures to prevent accidental touches during playback.
  - **Auto-Advance Countdown:** On reaching the last 10 seconds of an episode, shows an overlay button *"Next Episode in 5s..."* with instant play or cancel.
- **Playlist & Pre-caching:** Player builds `MediaItem`s for the entire series in sorted order (`setMediaItems(items, startIndex, startPositionMs)`).
- **Position Persistence:**
  1. On `onPause()` and `onStop()`, write `PlaybackStateEntity` via `applicationScope.launch(Dispatchers.IO)`.
  2. While `isPlaying`, ticker coroutine writes every **5 s** (crash and process-death resilience).
  3. Finished rule: >= 95% or <= 90 s remaining marks `isFinished = true`, `lastPlayedPosition = 0`.
- **Subtitles & Codec Handling:**
  - Clean styled subtitle renderer with semi-transparent high-contrast background box.
  - Supports embedded SRT, ASS/SSA tracks, and sidecar `.srt`. Advanced ASS/SSA styling supported via modular extension without compromising playback frame rate.
- **Error Handling:** Non-crashing Material 3 dialog on hardware decoder failure (e.g. unsupported 10-bit HEVC profile) offering fallback software decoding or skip to next episode.

---

## 5. NON-FUNCTIONAL REQUIREMENTS (NFR)

### 5.1 Performance

| ID | Requirement | Verification |
| --- | --- | --- |
| NFR-P1 | All MediaStore queries, parsing, hashing, and Room writes MUST execute on `Dispatchers.IO`; the main thread MUST NOT perform disk or DB I/O. | StrictMode (`detectAll().penaltyDeath()`) in debug builds; instrumented test fails on violation |
| NFR-P2 | Parsing throughput >= 5,000 filenames/s on JVM (precompiled regex, no per-call `Regex` construction) | Unit benchmark |
| NFR-P3 | Initial scan of 1,000 files <= 3 s; 5,000 files <= 8 s on reference device (Snapdragon 6-series, 4 GB RAM) | Macrobenchmark |
| NFR-P4 | Incremental rescan with no changes <= 500 ms for 5,000 files | Instrumented test |
| NFR-P5 | Dashboard scroll jank (frames > 16 ms) <= 1% with 500 series | Macrobenchmark `FrameTimingMetric` |
| NFR-P6 | Cold start to first Dashboard frame <= 1.5 s (cached DB); Baseline Profile shipped | Macrobenchmark `StartupTimingMetric` |
| NFR-P7 | Scan runs as a single coroutine on `Dispatchers.IO`: cursor iteration + parse inline, batch commits of 200, `ensureActive()` per batch for cancellation | Code review + cancellation test |

### 5.2 Resource Optimization

- **RAM:** idle Dashboard PSS <= 80 MB; 1080p HEVC playback PSS <= 150 MB (app process only). No full-library in-memory lists beyond the visible window; Room `Flow` queries return lightweight projection classes (`SeriesSummary`, `EpisodeRow`).
- **Thumbnail caching (strict):** in-memory `LruCache<Long, Bitmap>` sized `min(8 MB, maxMemory/16)`, keyed by `mediaStoreId`; bitmaps decoded at <= 320x180, `RGB_565` where alpha is unneeded. Optional disk cache in `cacheDir/thumbs` capped at **20 MB** (LRU eviction by last-modified). No other caches. `onTrimMemory` MUST clear the memory cache at `TRIM_MEMORY_UI_HIDDEN` and above.
- **APK size:** release APK (arm64-v8a) <= **6 MB**; AAB with ABI/density/language splits; R8 full mode + `shrinkResources`; no bundled fonts (system Roboto); vector drawables only; minimal Compose modules (`ui`, `foundation`, `material3`, `navigation-compose`, `runtime`); no `material-icons-extended` (use a handful of custom vectors).
- **Battery:** no wake locks outside playback; no foreground service; scans call `yield()` per batch.
- **DB footprint:** target <= 2 MB per 5,000 episodes; indexes limited to those in §3.3.2.
- **Privacy:** no telemetry, no external storage writes, no logging of file paths in release builds (`Log` calls stripped by R8 rules).
- **Accessibility/i18n:** TalkBack content descriptions on all interactive elements; strings in `values/` (Bahasa Indonesia default) and `values-en/`; minimum touch target 48 dp.

---

## 6. DEFINITION OF DONE (DoD) & TESTING REQUIREMENTS

### 6.1 Local Regex Parser — Test Criteria (P0)

The parser lives in a pure-Kotlin package and MUST be covered by **table-driven JUnit5 tests** (`@ParameterizedTest` + `@MethodSource`) with a golden corpus file `src/test/resources/golden_filenames.tsv` (columns: input, parentFolder, expectedTitle, expectedSeason, expectedEpisode, expectedVersion, expectedGroup, expectedRes, expectedConfidence).

**Acceptance criteria:**

1. The corpus MUST contain **>= 150 real-world filenames** spanning: Kusonime/Oploverz/Samehadaku/Anoboy/Meownime/Erai-raws/SubsPlease styles; underscore, dot, and space delimiters; `SxxExx`, `Episode N`, `Eps N`, `- NN`, `[NN]`, trailing number; v2 releases; batch-style tails; titles containing digits, roman numerals, and punctuation.
2. Overall exact-match rate on `(title, season, episode)` >= **95%**; >= **99%** on HIGH-confidence rows; **0** crashes/exceptions on any input.
3. All 14 examples in §3.2.5 MUST pass as written.
4. **Adversarial/negative cases (MUST pass):**

| Input | Expected |
| --- | --- |
| `Final Fantasy VII Advent Children.mkv` | title `Final Fantasy VII Advent Children`, ep = null, LOW ("Final" not stripped) |
| `[Group] 86 - Eighty Six - 11 [1080p].mkv` | title `86 - Eighty Six`, ep 11 (E5 last-match rule) |
| `[Group] Mob Psycho 100 - 12 [720p].mkv` | title `Mob Psycho 100`, ep 12 |
| `Anime 2024 1080p WEB-DL.mkv` | ep = null, year = 2024; `2024` MUST NOT become an episode (E7 year guard) |
| `[Group] Title - 1920x1080 - 05.mkv` | resolution recorded as `1080p`; ep 5; `1920x1080` MUST NOT trigger E2 |
| `[Group]Title-07[480p].mp4` (no spaces) | title `Title`, ep 7 |
| `.mkv`, `[Group] .mkv`, empty string | no exception; title falls back to the folder name or `Unknown` |
| `日本語タイトル - 05 [1080p].mkv` | title preserved in Unicode, ep 5 |
| `[A][B] Title - 03.mkv` | group `A`; `[B]` discarded as noise (or recognized dictionary group); ep 3 |

5. **Property tests (jqwik or equivalent):** `parse(coreTitle + " - 01").coreTitle == coreTitle` (idempotence); `seriesKey` is invariant under case, underscore/space, and bracket-tag permutations of the same release.
6. Parser microbenchmark meets NFR-P2.

### 6.2 Room Persistence — Success Metrics (P0)

Instrumented tests (`androidTest`, in-memory and file-based Room DB):

| ID | Criterion |
| --- | --- |
| DB-1 | Inserting 5,000 episodes across 400 series via the scan path completes in <= 8 s; row counts match input. |
| DB-2 | Re-running the scan on unchanged data performs **0** writes (verified via invalidation tracker or statement counter). |
| DB-3 | Deleting a file from the fake MediaStore removes its `episodes` row, removes the series if empty, and **retains** its `playback_state` row. |
| DB-4 | Renaming a file (same size) yields a new fingerprint, so progress is NOT carried (documented limitation); moving a file across folders (same name and size) carries progress. |
| DB-5 | `ON DELETE CASCADE` removes all child episodes when a series is deleted. |
| DB-6 | `observeDashboard()` orders by `lastUpdatedSec DESC` and emits within 100 ms after a write. |
| DB-7 | A `MigrationTestHelper` harness exists and the v1 schema JSON is exported. |

### 6.3 Playback Session Retention — Success Metrics (P0)

| ID | Criterion | Method |
| --- | --- | --- |
| PB-1 | Pause at T, background the app, resume: position within ±1 s of T | UI Automator |
| PB-2 | Kill the app process while playing at T, relaunch, Resume restores within ±6 s (5 s ticker) | UI Automator + `adb shell am kill` |
| PB-3 | Screen rotation/config change does not recreate the player or reset position | Espresso / Compose test |
| PB-4 | Reaching >= 95% (or <= 90 s remaining) sets `isFinished = true`, `lastPlayedPosition = 0`; Series Detail shows a check icon on return | Instrumented |
| PB-5 | Auto-advance moves to episode N+1 and persists state for N and N+1 | Instrumented |
| PB-6 | Decoder-unsupported file shows the error dialog; no crash; zero ANRs over a 30-minute soak | Manual + `adb` monkey |

### 6.4 Global Definition of Done

A feature is **Done** only when all of the following hold:

1. Code merged to `main` with >= 80% line coverage on `data/parse` (>= 95% branch coverage on `FilenameParser`) and >= 70% overall.
2. `./gradlew lint testDebugUnitTest` passes in CI.
3. Release build passes the APK-size gate (<= 12 MB arm64) and offline-first verification (§2.2).
4. Macrobenchmarks meet §5.1 thresholds on the reference device.
5. StrictMode reports zero main-thread I/O violations in a full-flow manual run (permission, scan, browse, play, resume).
6. Manual verification on **API 26, 29, 33, and 34** devices/emulators (legacy `DATA`-based path, scoped storage, granular media permissions, partial-access grant).
7. All strings localized (id/en); TalkBack walkthrough of Screens 1–3 completed.

---

## 7. IMPLEMENTATION SEQUENCE FOR CODING AGENTS

Build in this order so each stage is independently testable:

1. **Project skeleton:** Gradle KTS, version catalog, manifest (permissions per §2.2), `AppContainer`.
2. **`data/parse`:** `NoiseDictionary`, `FilenameParser`, `SeriesKeyFactory` plus the full golden-corpus test suite (§6.1). *Do not proceed until green.*
3. **`data/db`:** entities, DAOs, `AppDatabase`, schema export, tests (§6.2).
4. **`data/scan`:** `MediaStoreScanner`, `ScanCoordinator` (Mutex, ContentObserver debounce), incremental algorithm (§3.1.3).
5. **Repositories & Settings:** `LibraryRepository`, `PlaybackRepository`, `SettingsRepository` (API key & Wi-Fi sync preferences).
6. **`data/ai`:** `AiMetadataService`, `GeminiAiClient`, `MetadataEnricher` (entity resolution, synopsis, poster fetch).
7. **UI:** `PermissionGate`, Dashboard (Search & Filter Pills, Continue Watching carousel), SeriesDetail, SettingsScreen (Compose, Material 3).
8. **Player:** `PlayerActivity`, playlist construction, persistence ticker, YouTube gestures, Netflix episode sheet drawer (§4.4).
9. **Optimization pass:** Baseline Profile, R8 rules, image & thumbnail cache limits, APK-size audit.
