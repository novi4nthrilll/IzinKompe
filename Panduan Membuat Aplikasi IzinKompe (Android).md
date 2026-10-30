# Panduan Membuat Aplikasi IzinKompe (Android)

Oct 30, 2026 · @viando

## Stack yang disarankan

Bangun IzinKompe sebagai aplikasi Android asli dengan Kotlin, karena hanya cara ini yang bisa benar-benar mengunci app lain, dan jangan pakai backend dulu di MVP.

- Android Studio, Kotlin, minSdk 26 atau 28.
- Penyimpanan lokal: DataStore untuk pengaturan, Room untuk daftar app dan jadwal.
- Tanpa backend dulu: kode OTP dibuat lokal dengan TOTP (lihat langkah 5).

## Langkah 1 sampai 4: fitur inti

Empat langkah ini membentuk inti aplikasi: pilih app, atur jadwal, deteksi app yang dibuka, lalu blokir.

**1. Pilih app yang mau dikunci**

- Ambil daftar app terinstal lewat `PackageManager`, biasanya dengan intent `ACTION_MAIN` + `CATEGORY_LAUNCHER`, atau `getInstalledApplications`.
- Di Android 11 ke atas, deklarasikan `<queries>` atau permission `QUERY_ALL_PACKAGES` di manifest supaya daftarnya lengkap.
- Tampilkan di `RecyclerView` (atau Compose `LazyColumn`) dengan checkbox, lalu simpan package name yang dipilih.
- Buat daftar app yang tidak boleh dikunci: telepon, SMS, dan app komunikasi seperti WhatsApp.

**2. Atur jadwal belajar**

- Simpan jam mulai, jam selesai, dan hari aktif.
- Buat fungsi `isSessionActive(now)` yang mengecek apakah sekarang masuk jam belajar. Pisahkan jadi fungsi sendiri supaya mudah dites.

**3. Deteksi app yang sedang dibuka**

Ada dua pilihan:

- **UsageStatsManager**: izin `PACKAGE_USAGE_STATS`, diberikan manual user lewat Settings. Polling tiap 500 ms sampai 1 detik dari Foreground Service, lalu baca event `ACTIVITY_RESUMED` terakhir. Lebih ringan dan lebih mudah disetujui Play Store.
- **AccessibilityService**: dengarkan `TYPE_WINDOW_STATE_CHANGED`. Lebih akurat dan realtime, tapi Play Store ketat soal ini dan user harus mengaktifkannya manual.

Untuk tugas ospek, mulai dari UsageStats + Foreground Service.

**4. Blokir app**

- Kalau app yang dibuka ada di daftar kunci dan sesi sedang aktif, tampilkan `LockActivity` layar penuh ("Ini jam belajar, minta OTP ke orang tua").
- Sejak Android 10, app di background dibatasi membuka Activity. Solusinya izin `SYSTEM_ALERT_WINDOW` (overlay), atau, kalau pakai AccessibilityService, panggil `performGlobalAction(GLOBAL_ACTION_HOME)` untuk melempar ke home.
- Foreground Service wajib punya notifikasi permanen.

## Langkah 5 sampai 8: OTP, pengamanan, dan keandalan

Empat langkah ini membuat kunci tidak mudah dilewati: OTP orang tua, pengaturan terkunci, pemantauan, dan service yang tetap hidup.

**5. Buka kunci lewat OTP**

- `LockActivity` punya input kode. Kalau benar, buka kunci selama durasi tertentu (misalnya 15 menit), lalu kunci lagi otomatis.
- **Tanpa backend (disarankan untuk MVP)**: pakai TOTP (RFC 6238, seperti Google Authenticator). Saat pairing, anak dan orang tua berbagi satu secret lewat QR. Orang tua melihat kode 6 digit di app-nya, anak memverifikasi secara lokal tanpa internet.
- **Dengan backend**: Firebase (Auth, Firestore, FCM). Anak mengirim permintaan, orang tua dapat notifikasi, lalu mengirim OTP. Lebih sesuai visi produk, tapi kerjanya lebih banyak.

**6. Pengaturan terkunci selama sesi**

- Mengubah daftar app atau jadwal harus lewat gate OTP juga selama sesi aktif.
- Supaya app tidak mudah dimatikan atau di-uninstall, pakai `DeviceAdminReceiver`. Selama device admin aktif, app tidak bisa di-uninstall dengan cara biasa.
- Layar Settings sistem sebaiknya ikut diblokir selama sesi, dengan mekanisme deteksi yang sama seperti langkah 3 dan 4.

**7. Notifikasi ke orang tua saat app dinonaktifkan**

- App yang sudah di-uninstall tidak bisa mengirim apa pun, jadi jangan menjanjikan "notifikasi saat di-uninstall".
- Cara yang realistis: app mengirim heartbeat berkala ke server, dan server memberi tahu orang tua kalau heartbeat berhenti ("dinonaktifkan atau berhenti terhubung"). Ini butuh backend, jadi masuk tahap lanjutan.
- Untuk MVP lokal, cukup tampilkan di sisi anak, atau catat sebagai pengembangan lanjutan di slide.

**8. Supaya tetap berjalan**

- Pasang `BOOT_COMPLETED` receiver untuk menyalakan ulang service setelah HP restart.
- Minta user mematikan battery optimization untuk app ini.
- Beberapa merek (Xiaomi, Oppo, Vivo, Samsung) agresif mematikan service background, jadi tes di HP asli.

## Layar minimal

MVP cukup lima layar:

1. Onboarding dan permission (usage access, overlay, device admin).
2. Pilih app.
3. Atur jadwal.
4. Layar kunci (`LockActivity`).
5. Pairing dengan orang tua dan input OTP.

## Urutan pengerjaan dan catatan

Kerjakan dari fitur inti ke pengamanan, dan sisi orang tua serta backend belakangan:

1. Daftar app
2. Jadwal
3. Deteksi foreground app
4. `LockActivity`
5. OTP (TOTP)
6. Foreground Service + boot receiver
7. Device admin
8. Sisi orang tua dan backend

Catatan penting:

- Tes di HP Android asli, karena emulator sering tidak representatif untuk service dan overlay.
- Kebijakan Play Store ketat soal izin Accessibility dan `QUERY_ALL_PACKAGES`. Untuk demo ospek ini tidak jadi masalah, tapi perlu diperhatikan kalau nanti dipublikasikan.
