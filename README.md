# Tahlil

> Web + Native Android (Kotlin) application for reading Tahlil & Doa — with a Singkat (ringkas) and Lengkap (full) variant toggle.

[![CI](https://github.com/SecretArrow/Tahlil/actions/workflows/ci.yml/badge.svg)](https://github.com/SecretArrow/Tahlil/actions/workflows/ci.yml)

Forked and continued from `SecretArrow/Tahlil-Doa`. This is now the canonical repository.

## 📱 Struktur Repository

```
Tahlil/
├── web/                 # Next.js 16 web app (TypeScript, Tailwind v4)
├── android/             # Native Android app (Kotlin, Jetpack Compose)
├── .github/workflows/   # CI/CD pipeline (GitHub Actions)
├── docs/                # Dokumentasi tambahan
├── CHANGELOG.md
└── README.md
```

## ✨ Fitur utama

- **Dua varian konten: Singkat & Lengkap.** Varian Singkat (28 section) berisi inti tahlil; varian Lengkap (39 section) menambahkan ayat-ayat pilihan Surah Yasin. Toggle tersedia di home screen dan Settings.
- 3 mode font Arab: Standar, Gaya Mushaf Utsmani, Naskhi Tebal
- Toggle show/hide Arab/Latin/Terjemah
- Toggle "Koma pada Teks Arab"
- Toggle "Warnai Kata Arab" (alternating colors per word)
- Slider tipografi (ukuran, jarak baris, jarak kata, jarak huruf)
- Mode gelap/terang dengan auto-save (DataStore di Android, cookie di web)
- Pengingat harian (Android, via WorkManager — background process yang bertahan setelah app ditutup)
- Navigasi drawer + scroll active tracking
- Tanda waqf mushaf ۝X dengan CSS-drawn circle
- Repeat badge untuk blok dengan `repeat` field

## 🌐 Web App

**Stack:** Next.js 16 App Router, TypeScript, Tailwind CSS v4, shadcn/ui, Framer Motion, next-themes

**Cara menjalankan:**
```bash
cd web
npm install
npm run dev
```

**Build & test:**
```bash
npm run build      # production build
npm test           # vitest unit tests
npm run test:e2e   # Playwright e2e (variant toggle, navigation)
```

## 📱 Android App

**Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, DataStore, Coroutines, WorkManager, Gradle Kotlin DSL

**Cara build (lokal):**
```bash
cd android
./gradlew assembleDebug
# atau untuk release signed (memerlukan keystore):
./gradlew assembleRelease
```

**APK output (split per ABI):**
- `app-arm64-v8a-release.apk` (modern 64-bit ARM)
- `app-armeabi-v7a-release.apk` (legacy 32-bit ARM)
- `app-x86_64-release.apk` (emulators / x86_64 devices)
- `app-universal-release.apk` (all ABIs)

## 🚀 CI/CD Pipeline

Pipeline otomatis via GitHub Actions (`.github/workflows/ci.yml`):

1. **auto-fix** — `ktlint --format` (Kotlin) + `prettier` (web); commit & push otomatis jika ada perubahan
2. **web-test** — Next.js lint + build + vitest unit tests
3. **web-e2e** — Playwright e2e (variant toggle)
4. **android-test** — Gradle lint + unit tests + assembleRelease (signed, split per ABI)
5. **android-split-apks** — Verifikasi signature setiap APK dengan `apksigner`
6. **android-instrumented-test** — e2e di emulator API 29 + verifikasi release APK launch
7. **release** — Buat GitHub Release dengan semua artifacts (push to main only)

Teknik hemat waktu build:
- Gradle cache + parallel + configuration-cache
- npm cache
- Cancel in-progress run pada ref yang sama
- Job paralel (web & android berjalan bersamaan)
- Auto-fix mencegah round-trip format-only

**Required secrets:**
- `KEYSTORE_ZIP_BASE64` — base64 dari `key-sign-apk.zip` (zip ter-password)
- `KEYSTORE_ZIP_PASSWORD` — password untuk unzip `key-sign-apk.zip`
- `KEYSTORE_PASSWORD` — password keystore (JKS)
- `KEY_ALIAS` — key alias di dalam keystore
- `KEY_PASSWORD` — password untuk key alias

## 📦 Releases

Release otomatis dibuat setiap push ke `main` branch. Setiap release berisi:
- 4 APK (per-ABI + universal) yang sudah signed
- Web build (zip)

Lihat: https://github.com/SecretArrow/Tahlil/releases

## 📚 Sumber Konten

Konten tahlil dikompilasi dari sumber-sumber publik (Al-Qur'an, hadits, dan amaliah ulama) — lihat `android/app/src/main/assets/tahlil-content.json` dan `web/src/data/tahlil-content.json`.

## 📄 License

MIT
