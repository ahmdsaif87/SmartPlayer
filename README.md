# SmartPlayer 🎬

SmartPlayer adalah aplikasi pemutar video lokal modern untuk Android dengan antarmuka imersif bergaya **Cinema Dark (Netflix × YouTube)**. Dibangun menggunakan **Jetpack Compose**, **Material 3**, dan **Media3 ExoPlayer**, berfokus pada privasi, performa tinggi, dan pengalaman menonton offline yang mulus.

---

## ✨ Fitur Utama

- **🎨 Cinema Dark Theme**: Desain elegan bernuansa bioskop (`#0D0D11`, `#15151E`, aksen Netflix Red `#E50914`), nyaman di mata dan hemat daya untuk layar OLED/AMOLED.
- **📂 Organisasi Otomatis**: Mendeteksi dan mengelompokkan video lokal secara cerdas menjadi **Serial**, **Anime**, dan **Film** berdasarkan penamaan file.
- **🚫 Isolasi Rekaman Kamera & WA**: Memisahkan rekaman pribadi (`DCIM/Camera`, `WhatsApp`) agar koleksi film/serial tetap rapi (dapat diaktifkan/dinonaktifkan di Pengaturan).
- **🎛️ Kontrol Gesture Player**:
  - Geser vertikal di sisi **kiri** layar untuk mengatur **Brightness** (kecerahan).
  - Geser vertikal di sisi **kanan** layar untuk mengatur **Volume** suara.
  - Double-tap untuk *fast forward* / *rewind* 10 detik.
  - Scrubber/tracker timeline yang halus (*smooth seek*).
- **💬 Dual-Audio & Subtitle Picker**: Pemilihan trek audio ganda dan teks subtitle internal/eksternal dengan tampilan teks subtitle bergaris tepi (*clean outline*) yang mudah dibaca.
- **🖼️ Picture-in-Picture (PiP)**: Tetap menonton video sambil membuka aplikasi lain.
- **⏭️ Next Episode Countdown**: Otomatis berpindah ke episode berikutnya dengan hitung mundur overlay.
- **🕒 Halaman Riwayat Tontonan**:
  - Melanjutkan tontonan tepat di posisi terakhir.
  - Hapus riwayat satuan (ikon ✕) atau hapus seluruh riwayat sekaligus.
- **🗑️ Manajemen File**: Mendukung penghapusan video satuan maupun *bulk delete* langsung dari aplikasi.
- **⚡ 100% Offline & Ringan**: Tidak memerlukan koneksi internet atau login pihak ketiga, seluruh metadata tersimpan aman di database lokal Room (SQLite).

---

## 🛠️ Tech Stack & Arsitektur

- **Bahasa**: Kotlin (100%)
- **UI Framework**: Jetpack Compose & Material 3
- **Video Engine**: AndroidX Media3 ExoPlayer
- **Database**: Room Database (SQLite)
- **Dependency Injection**: Hilt / Dagger
- **Asynchronous**: Kotlin Coroutines & Flow
- **Arsitektur**: Clean Architecture + MVVM

---

## 🚀 Memulai (Build & Run)

### Prasyarat
- Android Studio Ladybug / Meerkat atau yang lebih baru.
- JDK 17 atau 21.
- Android SDK dengan Minimum SDK 26 (Android 8.0) dan Target SDK 34 (Android 14).

### Kompilasi dari Terminal
```bash
# Clone repositori
git clone https://github.com/ahmdsaif87/SmartPlayer.git
cd SmartPlayer

# Build APK Debug
./gradlew assembleDebug

# Install ke perangkat yang terhubung (via ADB)
./gradlew installDebug
```

---

## 📄 Lisensi

Proyek ini dibuat untuk keperluan personal dan open-source di bawah lisensi MIT.
