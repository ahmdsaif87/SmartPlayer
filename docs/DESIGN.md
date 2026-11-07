# SmartPlayer — UI/UX Design Specification (DESIGN.md)

**Inspirasi Desain:** YouTube × Netflix  
**Platform:** Android (Jetpack Compose, Material 3)  
**Tema:** Immersive Cinema Dark (`#0A0A0C` - `#121214`)  
**Status:** v1.0 — Architecture & Visual Spec

---

## 1. DESIGN PHILOSOPHY & VISUAL IDENTITY

SmartPlayer mengadopsi bahasa visual gabungan antara **Netflix** (eksplorasi konten imersif, kartu koleksi elegan, section *Continue Watching*, dan navigasi detail serial) serta **YouTube** (kemudahan gesture video player, filter pills/chips yang cepat, pencarian instan, dan kontrol playback yang responsif).

### 1.1 Palet Warna (Cinema Dark Theme)

Aplikasi berfokus pada konten video lokal, sehingga warna latar belakang dioptimalkan untuk layar AMOLED/OLED agar hemat daya dan nyaman di mata:

| Token | Nilai Hex | Penggunaan |
| --- | --- | --- |
| `Background` | `#0A0A0C` | Latar belakang kanvas utama (Deep Cinema Black) |
| `Surface` | `#141418` | Latar belakang kartu item, top app bar, bottom sheet |
| `SurfaceVariant` | `#202026` | Border halus, chip filter tidak aktif, divider |
| `PrimaryAccent` | `#E50914` | Netflix Red / Electric Crimson (CTA tombol Resume, progress bar aktif, indikator live) |
| `SecondaryAccent`| `#38BDF8` | Cyan / Sky Blue (Badge resolusi `1080p`, `4K`, codec chip) |
| `TextPrimary` | `#F8FAFC` | Judul, nama episode, teks utama (High Contrast 95%) |
| `TextSecondary` | `#94A3B8` | Subtitle, season info, durasi, ukuran file (Muted 60%) |
| `ProgressBarBg` | `#334155` | Background track progress bar |

### 1.2 Tipografi & Ikonografi
- **Font:** Inter / System Roboto (Clean, modern geometric sans-serif dengan legibilitas tinggi pada resolusi layar mobile).
- **Icon Set:** Material Symbols / Lucide Android icons (rounded, 24dp standard, konsisten 2dp stroke).

---

## 2. INFORMATION ARCHITECTURE & USER FLOW

```
[ Dashboard (Home) ] ──────────┬─────────────────────────────┐
        │                      │                             │
   (Tap Kartu)         (Tap Resume / Quick Play)       (Tap Search Bar)
        ▼                      │                             ▼
[ Series/Media Detail ]        │                      [ Search Overlay ]
        │                      │                             │
   (Tap Episode)               │                        (Select Video)
        └──────────────────────┼─────────────────────────────┘
                               ▼
                   [ Immersive Player Screen ]
                               │
            ┌──────────────────┴──────────────────┐
     (Quick Episode Sheet)                 (Audio & Subtitle Dialog)
```

---

## 3. SCREEN SPECIFICATIONS & WIREFRAMES

### 3.1 Screen 1: Dashboard (Netflix & YouTube Hybrid)

Dashboard adalah hub utama untuk langsung melanjutkan tontonan atau menjelajahi seluruh koleksi video lokal.

```
┌────────────────────────────────────────────────────────┐
│  SmartPlayer                         [ 🔍 ]  [ ⚙️ / 🔄 ] │  <- Top Bar
├────────────────────────────────────────────────────────┤
│  ( Semua )  ( Sedang Ditonton )  ( Serial )  ( Film )  │  <- YouTube Filter Pills
├────────────────────────────────────────────────────────┤
│  LANJUTKAN MENONTON                                    │  <- Netflix Continue Watching
│  ┌──────────────────┐  ┌──────────────────┐            │     (Horizontal Carousel)
│  │ [Thumbnail/Hero] │  │ [Thumbnail/Hero] │            │
│  │ Jujutsu Kaisen   │  │ Frieren          │            │
│  │ S1:E12 • 14:20   │  │ S1:E03 • 08:45   │            │
│  │ [████████░░░░░░] │  │ [████░░░░░░░░░░] │            │
│  └──────────────────┘  └──────────────────┘            │
├────────────────────────────────────────────────────────┤
│  SEMUA KOLEKSI (42)                      [ Urutkan ▾ ] │
│  ┌───────────────────┐    ┌───────────────────┐        │  <- Adaptive 2-Column Grid
│  │ [ Poster / Thumb] │    │ [ Poster / Thumb] │        │     (AspectRatio 16:10 / 16:9)
│  │ 1080p  12 Eps     │    │ 720p   Movie      │        │
│  │                   │    │                   │        │
│  │ Spy x Family      │    │ Your Name (2016)  │        │
│  │ S1 • 12/12 eps    │    │ Film • 1j 46m     │        │
│  │ [██████████████]✓ │    │ [████████░░░░░░░] │        │
│  └───────────────────┘    └───────────────────┘        │
└────────────────────────────────────────────────────────┘
```

#### Komponen Utama Dashboard:
1. **Top App Bar:**
   - Logo teks `SmartPlayer` di sebelah kiri dengan aksen warna primer.
   - Tombol Search (`🔍`) dan Refresh/Settings (`🔄`).
   - Indikator scan tipis (*LinearProgressIndicator*) di bawah app bar saat indexing background berlangsung.
2. **Interactive Search Bar:**
   - Dapat diakses via ikon search atau expandable search field dengan instant-filtering (hasil muncul instan saat mengetik tanpa reload).
3. **Category Filter Pills (Ala YouTube):**
   - Pill chips horizontal scrollable: `Semua`, `Sedang Ditonton`, `Belum Selesai`, `Serial`, `Film`, `Selesai`.
   - Mengubah tampilan grid di bawahnya secara reaktif dan halus.
4. **Section "Lanjutkan Menonton" (Ala Netflix):**
   - Horizontal snap list menampilkan serial/film yang sedang dalam proses tonton (progress > 5s dan < 95%).
   - Kartu landscape lebar (16:9), ada judul episode, sisa durasi, dan progress bar merah menyala.
   - Tombol *Play* bulat overlay semi-transparan untuk satu kali tap langsung masuk ke video.
5. **Main Grid Koleksi:**
   - 2 kolom pada smartphone, 3-4 kolom pada tablet / foldable screen.
   - Menampilkan poster thumbnail dari frame video lokal berkualitas tinggi.
   - Badge overlay: Kualitas (`1080p`, `4K`, `720p`), jumlah episode (`12 Eps` atau `Movie`).
   - Label judul 2 baris (ellipsis), season label, dan progress bar ringkasan status menonton.

---

### 3.2 Screen 2: Series & Movie Detail Screen (Netflix Style)

Halaman untuk melihat detail episode dalam satu serial atau detail lengkap file film.

```
┌────────────────────────────────────────────────────────┐
│  [ ← Back ]                                [ Info / ℹ️ ]│
├────────────────────────────────────────────────────────┤
│                                                        │
│             [ BACKDROP THUMBNAIL 16:9 ]                │
│                                                        │
├────────────────────────────────────────────────────────┤
│  Sousou no Frieren                                     │
│  Season 1 • 28 Episode • Selesai Ditonton: 14/28       │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  ▶  Lanjutkan Nonton (Eps 15)                    │  │  <- Big Netflix Primary CTA
│  └──────────────────────────────────────────────────┘  │
│  [ + Tandai Semua Selesai ]  [ 📁 Buka Folder File ]   │  <- Secondary Action Chips
├────────────────────────────────────────────────────────┤
│  EPISODE                                [ Season 1 ▾ ] │
│                                                        │
│  ┌──────────────┐  Episode 01 - Petualangan Baru       │  <- Flat Episode Row
│  │ [Thumbnail]  │  24:12 • 450 MB • [1080p]            │
│  │ [██████████]✓│  Sudah selesai ditonton              │
│  └──────────────┘                                      │
│  ┌──────────────┐  Episode 02 - Sihir Pembersih        │
│  │ [Thumbnail]  │  23:50 • 430 MB • [1080p]            │
│  │ [██████░░░░] │  Tersisa 10 menit                    │
│  └──────────────┘                                      │
└────────────────────────────────────────────────────────┘
```

#### Fitur Utama Detail Screen:
1. **Hero Header:**
   - Backdrop gambar besar yang diambil dari thumbnail episode terbaru/pertama dengan efek gradient gelap ke bawah (*scrim gradient*).
   - Judul serial besar dan tegas, informasi metadata (Season, jumlah episode, status resolusi tertinggi).
2. **Primary Action ("Lanjutkan Menonton" / "Mulai Nonton"):**
   - Tombol besar mencolok dengan icon play: otomatis mendeteksi episode yang belum selesai dengan posisi terakhir; jika semua selesai, tombol berubah menjadi "Tonton Ulang".
3. **Episode List Layout:**
   - List datar (flat list) dengan thumbnail landscape di sisi kiri.
   - Thumbnail memiliki indikator progress bar di bagian bawah dan badge centang hijau jika sudah selesai (`isFinished = true`).
   - Sisi kanan menampilkan: Nomor & Nama Episode, Durasi, Ukuran file, Chip resolusi (`1080p`, `HEVC`), dan estimasi sisa waktu.

---

### 3.3 Screen 3: Video Player Immersive (YouTube & Netflix Hybrid)

Pemutar video landscape penuh (ExoPlayer + Media3) dengan kontrol dan gesture intuitif.

```
┌────────────────────────────────────────────────────────────────────────┐
│ [ ← ]  Sousou no Frieren — Episode 15                 [ 💬 ]  [ ⚙️ ]  │ <- Top Bar
│                                                                        │
│                                                                        │
│      << -10s                  [  ▶ / ⏸  ]                 +10s >>      │ <- Center Controls
│   (Double Tap L)                                       (Double Tap R)  │
│                                                                        │
│                                                                        │
│ 14:20 ──●─────────────────────────────────────────────────── -09:50    │ <- Scrub Bar
│ [ 🔒 Lock ]  [ ⏭ Next ]  [ ⚡ Speed (1.0x) ]  [ 📑 Episodes ] [ ⛶ Fit ]│ <- Bottom Bar
└────────────────────────────────────────────────────────────────────────┘
```

#### Gesture Player (Inspirasi YouTube):
- **Double Tap Kiri:** Mundur 10 detik (`-10s`) disertai animasi ripple lingkaran dan teks bubble "+10s".
- **Double Tap Kanan:** Maju 10 detik (`+10s`).
- **Swipe Vertikal di Sisi Kiri Layar:** Mengatur *Brightness* layar (dengan HUD slider vertikal transparan).
- **Swipe Vertikal di Sisi Kanan Layar:** Mengatur *Volume* audio (dengan HUD slider vertikal).
- **Pinch to Zoom / Double Tap Full:** Toggle aspect ratio antara `Fit to Screen`, `Crop / Zoom to Fill` (menghilangkan black bar), dan `100% Original`.
- **Horizontal Slide Scrubbing:** Menggeser timeline video secara presisi dengan preview waktu melayang.

#### Fitur & Kontrol Player (Inspirasi Netflix):
- **Quick Episode Drawer / Bottom Sheet (`[ 📑 Episodes ]`):**
  - Pengguna dapat membuka drawer daftar episode **tanpa keluar dari player**.
  - Menampilkan thumbnail dan judul episode berikutnya, tap langsung berpindah episode seketika.
- **Audio & Subtitle Picker Dialog (`[ 💬 ]`):**
  - Dialog pop-up cepat untuk memilih track audio (misal: *Japanese Dual Audio*, *English*) dan subtitle (*Bahasa Indonesia*, *English*, *Off*).
  - Mendukung subtitle bawaan MKV maupun file sidecar `.srt`.
  - Rendering subtitle bersih dengan background box transparan hitam untuk keterbacaan maksimal.
- **Kecepatan Putar (`[ ⚡ Speed ]`):** Pilihan 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x.
- **Lock Screen (`[ 🔒 Lock ]`):** Mengunci semua interaksi layar agar tidak sengaja terpencet saat menonton.
- **Auto-Advance Countdown:** Saat episode mendekati akhir (10 detik terakhir), muncul tombol overlay ala Netflix: *"Next Episode in 5s..."* dengan opsi putar sekarang atau batal.

---

### 3.4 Screen 4: Settings & AI Configuration Screen

Halaman pengaturan untuk konfigurasi AI dan perilaku pemindaian:

```
┌────────────────────────────────────────────────────────┐
│  [ ← Back ]  Pengaturan & AI                           │
├────────────────────────────────────────────────────────┤
│  KECERDASAN BUATAN (AI ENRICHMENT)                     │
│                                                        │
│  Gemini API Key                                        │
│  [ •••••••••••••••••••••••• ] [ Simpan ]              │
│  Dapatkan API key gratis di ai.google.dev             │
│                                                        │
│  [ x ] Hanya sinkronisasi AI saat terhubung Wi-Fi      │
│  [ x ] Otomatis cari poster & sinopsis saat scan       │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ⚡ Sinkronkan Ulang Semua Metadata AI             │  │
│  └──────────────────────────────────────────────────┘  │
├────────────────────────────────────────────────────────┤
│  PENYIMPANAN & PEMINDAIAN                              │
│                                                        │
│  Folder yang dipantau:                                 │
│  • Download/                                           │
│  • Movies/                                             │
│  • Video/                                              │
│                                                        │
│  [ 🔄 Mulai Pemindaian Penuh ]                         │
└────────────────────────────────────────────────────────┘
```

---

## 4. DESIGN TOKENS & JETPACK COMPOSE COMPONENT SYSTEM

### 4.1 Elemen Kartu & Container
- **Card Corner Radius:** `16.dp` untuk thumbnail kartu grid, `12.dp` untuk kartu episode list, `24.dp` untuk tombol CTA utama.
- **Elevation / Glow:** Menggunakan border halus `1.dp` solid `Color(0x1AFFFFFF)` daripada drop shadow pekat (modern flat cinema design).
- **Progress Bar:** Ketinggian `4.dp` pada kartu list, `3.dp` di bawah thumbnail dashboard, `6.dp` pada player scrub bar dengan handle thumb yang responsif terhadap sentuhan jari.

### 4.2 State Handling Visuals
- **Skeleton Shimmer Loading:** Saat pemindaian database awal, kartu menampilkan animasi shimmer abu-abu gelap (`#1A1A22` ke `#282834`).
- **Empty State:** Ilustrasi modern minimalis + teks informatif:  
  *"Belum ada video ditemukan di folder perangkat. Simpan file video (.mp4, .mkv, .webm) di folder Download, Movies, atau Video."* + Tombol CTA *"Pindai Ulang"*.
- **Error Dialog:** Bottom sheet elegan saat format codec tidak didukung hardware (misal: HEVC 10-bit pada chipset lawas), menyarankan skip ke episode berikutnya atau beralih software renderer.

---

## 5. ACCESSIBILITY & ERGONOMICS (ONE-HANDED FRIENDLY)

1. **Thumb-Zone Optimization:** Elemen interaktif sering dipakai (Play button, Season dropdown, Filter chips) diposisikan di 2/3 area bawah layar saat mode portrait.
2. **Contrast Standards:** Rasio kontras teks putih terhadap background $\ge 12:1$ (melebihi standar WCAG AAA).
3. **Haptic Feedback:** Getaran lembut haptic (Android `HapticFeedbackConstants.CLOCK_TICK`) saat melakukan seek gesture dan saat menekan tombol lock player.
