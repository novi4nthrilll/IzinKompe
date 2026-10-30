# IzinKompe - Aplikasi Parental Control & Jam Belajar (Android)

Aplikasi Android native menggunakan Kotlin untuk membantu anak fokus belajar dengan mengunci aplikasi yang mengganggu selama sesi jam belajar aktif, dengan verifikasi OTP (TOTP) orang tua tanpa ketergantungan backend.

Implementasi ini dibangun secara presisi mengikuti panduan di `Panduan Membuat Aplikasi IzinKompe (Android).md`.

---

## 🚀 Fitur Utama & Kesesuaian dengan Panduan

| No | Fitur Panduan | Status | Implementasi |
|:---|:---|:---:|:---|
| 1 | **Pilih Aplikasi Terkunci** | ✅ Selesai | Menggunakan `PackageManager` (launcher apps), `QUERY_ALL_PACKAGES`, pencarian real-time, `RecyclerView`, dan Room DB. Pengecualian otomatis untuk Telepon, SMS, WhatsApp, dan aplikasi darurat (`BlocklistExclusions.kt`). |
| 2 | **Atur Jadwal Belajar** | ✅ Selesai | Pengaturan jam mulai/selesai via `MaterialTimePicker`, pemilihan hari aktif (Senin–Minggu), switch status, Room DB, serta fungsi deterministik `ScheduleChecker.isSessionActive(now)`. |
| 3 | **Deteksi Foreground App** | ✅ Selesai | `UsageStatsManager` polling (800ms) dari Foreground Service (`AppMonitorService.kt`), mendeteksi event `ACTIVITY_RESUMED` secara efisien dan hemat baterai. |
| 4 | **Blokir Aplikasi (Lock Screen)** | ✅ Selesai | `LockActivity` fullscreen overlay dengan permission `SYSTEM_ALERT_WINDOW`, menolak tombol back, dan notifikasi permanen Foreground Service. |
| 5 | **Buka Kunci Lewat OTP (TOTP)** | ✅ Selesai | TOTP lokal offline (RFC 6238) kompatibel dengan Google Authenticator. Tampilan QR code (`ZXing`) di `PairingActivity`. Buka kunci sementara 15 menit dengan auto-relock. |
| 6 | **Pengamanan Selama Sesi** | ✅ Selesai | `DeviceAdminReceiver` mencegah uninstalasi aplikasi; pemblokiran Settings sistem (`com.android.settings`); dan gate dialog OTP saat membuka pengaturan di aplikasi jika sesi belajar aktif. |
| 7 | **Keandalan Background** | ✅ Selesai | `BootReceiver` (`RECEIVE_BOOT_COMPLETED`, `QUICKBOOT_POWERON`) untuk otomatis menyalakan service setelah reboot, WakeLock terkelola, dan panduan abaikan optimasi baterai. |
| 8 | **5 Layar Minimal MVP** | ✅ Selesai | Onboarding (`OnboardingActivity`), Pilih App (`AppListActivity`), Jadwal (`ScheduleActivity`), Layar Kunci (`LockActivity`), Pairing OTP (`PairingActivity`), plus Dashboard Utama (`MainActivity`). |

---

## 📂 Struktur Proyek

```
izinkompe/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/izinkompe/
│   │   │   │   ├── IzinKompeApp.kt                 # Inisialisasi Database & Notification Channel
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/                      # LockedApp, Schedule, AppInfo
│   │   │   │   │   ├── db/                         # Room Database & DAOs
│   │   │   │   │   └── datastore/AppPreferences.kt # DataStore (TOTP Secret, Onboarding, Status)
│   │   │   │   ├── service/AppMonitorService.kt    # Foreground Service deteksi aplikasi
│   │   │   │   ├── receiver/                       # BootReceiver, AdminReceiver
│   │   │   │   ├── util/                           # TotpUtil, ScheduleChecker, BlocklistExclusions
│   │   │   │   └── ui/                             # MainActivity, Onboarding, AppList, Schedule, Lock, Pairing
│   │   │   ├── res/
│   │   │   │   ├── layout/                         # Layout XML untuk semua 5+1 activity
│   │   │   │   ├── drawable/                       # Backgrounds, shapes, gradients
│   │   │   │   ├── values/                         # Colors, strings, themes
│   │   │   │   └── xml/device_admin_policies.xml   # Kebijakan Device Admin
│   │   │   └── AndroidManifest.xml
│   │   └── test/java/com/izinkompe/                # Unit Tests (TotpUtilTest, ScheduleCheckerTest)
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── Panduan Membuat Aplikasi IzinKompe (Android).md
```

---

## 🛠️ Persyaratan & Cara Menjalankan

1. **Android Studio**: Android Studio Hedgehog (2023.1.1) atau versi lebih baru.
2. **JDK**: JDK 17.
3. **Buka Proyek**:
   - Di Android Studio, pilih **Open** lalu pilih folder `izinkompe`.
   - Tunggu sinkronisasi Gradle selesai.
4. **Jalankan Aplikasi**:
   - Hubungkan HP Android fisik (disarankan) atau emulator Android 9+ (API 28+).
   - Klik tombol **Run 'app'** (`Shift + F10`).
5. **Jalankan Unit Test**:
   - Buka `app/src/test/java/com/izinkompe/ScheduleCheckerTest.kt` atau `TotpUtilTest.kt`.
   - Klik kanan -> **Run Tests**.
