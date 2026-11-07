# SmartPlayer

SmartPlayer adalah aplikasi pemutar video lokal untuk Android dengan antarmuka Cinema Dark (Netflix x YouTube). Dibangun menggunakan Jetpack Compose, Material 3, dan Media3 ExoPlayer, berfokus pada performa tinggi, privasi, dan pengalaman menonton offline yang mulus.

---

## Fitur Utama

- **Cinema Dark Theme**: Tampilan bernuansa bioskop (#0D0D11, #15151E, aksen merah #E50914) yang nyaman di mata dan hemat daya untuk layar OLED/AMOLED.
- **Organisasi Otomatis**: Mendeteksi dan mengelompokkan video lokal menjadi Serial, Anime, dan Film berdasarkan nama file.
- **Filter Cepat**: Filter kategori instan (Semua, Serial, Film).
- **Isolasi Rekaman Kamera & WhatsApp**: Menyaring folder rekaman kamera pribadi dan WhatsApp agar koleksi media tetap rapi (dapat diaktifkan/dinonaktifkan di Pengaturan).
- **Kontrol Gesture Player**:
  - Geser vertikal di sisi kiri untuk mengatur kecerahan (Brightness).
  - Geser vertikal di sisi kanan untuk mengatur volume suara.
  - Ketuk ganda (double-tap) untuk melompat 10 detik maju/mundur.
  - Penarikan progress bar (scrubber) yang halus dan responsif.
- **Dukungan Audio & Subtitle**: Pemilihan trek audio ganda dan subtitle internal/eksternal dengan font bergaris tepi tipis yang jelas.
- **Picture-in-Picture (PiP)**: Melanjutkan pemutaran video saat beralih ke aplikasi lain.
- **Next Episode Countdown**: Transisi otomatis ke episode berikutnya dengan hitung mundur.
- **Riwayat Tontonan**: Halaman khusus untuk melanjutkan tontonan terakhir, dilengkapi tombol hapus satuan dan hapus semua.
- **Manajemen File**: Penghapusan file video satuan dan massal (bulk delete) langsung dari aplikasi.
- **100% Offline**: Beroperasi penuh secara lokal tanpa memerlukan koneksi internet maupun akun pihak ketiga.

---

## Tech Stack

- **Bahasa**: Kotlin
- **UI Toolkit**: Jetpack Compose & Material 3
- **Media Engine**: AndroidX Media3 (ExoPlayer)
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Concurrency**: Coroutines & Flow
- **Arsitektur**: MVVM & Clean Architecture

---

## Cara Kompilasi

### Prasyarat
- Android Studio Ladybug atau yang lebih baru
- JDK 17 atau 21
- Android SDK (Min SDK 26, Target SDK 34)

### Build APK
```bash
git clone https://github.com/ahmdsaif87/SmartPlayer.git
cd SmartPlayer
./gradlew assembleDebug
```

---
