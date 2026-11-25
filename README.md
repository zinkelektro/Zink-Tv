# TV Playlist — Android / Android TV


## 0. Branding yang sudah ditambahkan

Saya telah menambahkan dua gambar yang Anda kirim ke dalam source code:

- `app/src/main/res/drawable-nodpi/app_logo.png` → dipakai sebagai ikon/logo aplikasi.
- `app/src/main/res/drawable-nodpi/tv_banner.png` → dipakai sebagai banner launcher Android TV.
- `app/src/main/res/drawable-nodpi/welcome_banner.png` dan `welcome_logo.png` → dipakai di halaman masuk / splash / welcome screen.

Aplikasi sekarang dibuka lewat `SplashActivity`, lalu pengguna menekan tombol **Masuk ke Aplikasi** untuk masuk ke daftar channel.

Aplikasi Java native, Media3 ExoPlayer, daftar channel lokal, pencarian, filter kategori, pemilih playlist, dan navigasi tombol remote/D-pad. APK debug universal (tanpa ABI split), Android 6.0/API 23 ke atas. Internet diperlukan untuk menonton.

## 1. Build APK lewat GitHub (tanpa Android Studio)

1. Ekstrak ZIP. Buka https://github.com/new, buat repository `TVPlaylist` dengan **Private**, tanpa README, .gitignore atau license tambahan. Playlist dapat memuat URL privat/token; periksa hak distribusi dan URL sebelum mengunggah. Jangan jadikan repo publik tanpa pemeriksaan.
2. Buka terminal di folder `TVPlaylist` yang berisi `settings.gradle` dan `.github`. Pastikan Git terpasang. Jalankan perintah berikut; ganti USERNAME dengan akun GitHub Anda.

```bash
git init
git add .
git commit -m "Initial TV Playlist Android app"
git branch -M main
git remote add origin https://github.com/USERNAME/TVPlaylist.git
git push -u origin main
```

Jika Git meminta identitas, jalankan `git config user.name "Nama Anda"` dan `git config user.email "email Anda"`, lalu ulangi commit. Login GitHub melalui Git Credential Manager atau token akses yang sesuai (bukan password akun). Jangan simpan token di repo.

Alternatif tanpa terminal: di repo kosong pilih **uploading an existing file**, unggah isi folder proyek termasuk `.github/workflows/build-apk.yml`. Jika browser tidak mengunggah folder tersembunyi, buat file workflow lewat Add file > Create new file dengan nama `.github/workflows/build-apk.yml`, salin isinya dari paket, lalu commit.

3. Buka tab **Actions** > **Build Android APK**. Workflow berjalan setelah push ke `main`. Jika Actions belum aktif, aktifkan dahulu di Settings > Actions > General sesuai izin akun. Build manual: pilih workflow > Run workflow > main > Run workflow.
4. Buka run terbaru. Jika sukses (centang hijau), pada bagian **Artifacts** unduh `TV-Playlist-APK` (ZIP). Ekstrak ZIP tersebut untuk mendapatkan `app-debug.apk`.
5. Salin APK ke HP atau Android TV, izinkan pemasangan aplikasi dari sumber tersebut, lalu instal. Alternatif via ADB setelah USB/network debugging diaktifkan: `adb install -r app-debug.apk`.

APK debug ditandatangani otomatis dengan debug key; cocok untuk pengujian, bukan publikasi Play Store. Build di runner berbeda bisa memakai kunci berbeda sehingga update mungkin memerlukan uninstall versi sebelumnya. Uninstall menghapus data aplikasi. Untuk distribusi resmi gunakan release signing yang konsisten dan simpan keystore secara aman.

## 2. Build lokal (opsional)

Gunakan JDK 17, Gradle 8.9, Android SDK Platform 35 dan Build Tools 35.0.0. Atur ANDROID_HOME atau buat `local.properties` berisi `sdk.dir` sesuai komputer. Dari root proyek:

```bash
gradle :app:assembleDebug
```

APK berada di `app/build/outputs/apk/debug/app-debug.apk`. Paket tidak menyertakan Gradle wrapper binary atau SDK; workflow memasang Gradle secara otomatis. Android Studio dapat membuka root project dan menggunakan instalasi Gradle 8.9.

## 3. Playlist dan keterbatasan

Sumber: tiga file utama dari Playlist Tv.rar. Salinan BACKUP dan .bak tidak digunakan. Data dikonversi menjadi JSON pada `app/src/main/assets/`, dengan field `name`, `group`, `url`.

| File asal | Entri EXTINF | Entri dimasukkan |
| --- | ---: | ---: |
| playlist1.m3u | 726 | 393 |
| playlistdp.m3u | 221 | 4 |
| test_playlist.m3u | 1930 | 692 |

Total 1089 entri lintas tiga playlist (channel yang sama dapat muncul di beberapa playlist). Filter konservatif: melewati entri yang mengandung konfigurasi lisensi/DRM, header khusus EXTHTTP/stream_headers/cookie/extra-headers, URL dengan pipe, URL kosong/tidak valid secara dasar, serta duplikat identik dalam satu playlist. Header VLC biasa tidak diterapkan. Ini tidak menjamin stream yang tersisa bebas DRM atau dapat diputar. HLS, DASH, dan format progresif yang didukung Media3 dapat diputar jika server mengizinkan. Tidak ada mekanisme bypass DRM atau autentikasi. URL HTTP diizinkan untuk kompatibilitas dengan sumber tetapi tidak terenkripsi. Gunakan hanya siaran yang Anda berhak akses. Jangan membagikan APK berisi URL rahasia karena asset dapat diekstrak.

Untuk mengubah playlist, edit JSON terkait (UTF-8) menggunakan format berikut, isi URL dengan stream resmi yang Anda miliki izin untuk akses:

```json
[{"name":"Nama TV","group":"Nasional","url":"https://example.org/live.m3u8"}]
```

Commit dan push perubahan untuk build ulang. Nama aplikasi berada di AndroidManifest.xml, package/version di app/build.gradle. Kode utama di MainActivity.java.

## 4. Troubleshooting

- Build merah: buka Actions > run > langkah yang gagal dan baca log. Periksa akses jaringan, dependency, SDK, serta kuota Actions.
- Video gagal: coba channel lain; sumber mungkin offline, token kedaluwarsa, geo-block, perlu header/DRM resmi atau format tidak kompatibel.
- APK tidak dapat dipasang: periksa Android minimal 6, izin sideload, ruang penyimpanan, serta konflik tanda tangan aplikasi.

## Status pengujian

Paket source dan workflow disiapkan, data playlist dikonversi, struktur proyek diperiksa, serta logo + banner Anda sudah dipasang ke proyek. Build Android dan pemutaran streaming belum diuji di perangkat/runner karena lingkungan pembuat paket tidak memiliki Android SDK/Gradle. Tidak ada APK hasil kompilasi di dalam ZIP ini; GitHub Actions bertugas menghasilkan APK. Tidak ada jaminan semua URL aktif atau berizin.
