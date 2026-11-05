# Tahlil & Doa

> Web + Native Android (Kotlin) application for reading Tahlil & Doa — sourced from Kitab Tahlil Salik Channel.

[![CI](https://github.com/SecretArrow/Tahlil-Doa/actions/workflows/ci.yml/badge.svg)](https://github.com/SecretArrow/Tahlil-Doa/actions/workflows/ci.yml)

## 📱 Struktur Repository

```
Tahlil-Doa/
├── web/                 # Next.js 16 web app (TypeScript, Tailwind v4)
├── android/             # Native Android app (Kotlin, Jetpack Compose)
├── .github/workflows/   # CI/CD pipeline (GitHub Actions)
├── docs/                # Dokumentasi tambahan
├── CHANGELOG.md
└── README.md
```

## 🌐 Web App

**Stack:** Next.js 16 App Router, TypeScript, Tailwind CSS v4, shadcn/ui, Framer Motion, next-themes

**Fitur:**
- 11 section lengkap (Pembukaan → Al-Fatihah Penutup)
- 3 mode font Arab: Standar, Gaya Mushaf Utsmani, Naskhi Tebal
- Toggle show/hide Arab/Latin/Terjemah
- Toggle "Koma pada Teks Arab"
- Toggle "Warnai Kata Arab" (alternating colors per word)
- Slider tipografi (ukuran, jarak baris, jarak kata, jarak huruf)
- Mode gelap/terang dengan auto-save via cookie

**Cara menjalankan:**
```bash
cd web
npm install
npm run dev
```

**Build:**
```bash
npm run build
```

## 📱 Android App

**Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, DataStore, Coroutines, Gradle Kotlin DSL

**Fitur (sama dengan web):**
- 11 section dengan Arabic text + transliterasi + terjemahan
- Toggle show/hide Arab/Latin/Terjemah
- Toggle "Koma pada Teks Arab"
- Toggle "Warnai Kata Arab"
- 3 mode font Arab (Standard, Quran, Naskhi Tebal)
- Slider tipografi
- Dark/Light theme (auto-save via DataStore)
- Navigasi drawer + scroll active tracking
- Tanda waqf mushaf ۝X dengan CSS-drawn circle
- Repeat badge untuk blok dengan `repeat` field

**Cara build (lokal):**
```bash
cd android
./gradlew assembleDebug
# atau untuk release signed (memerlukan keystore):
./gradlew assembleRelease
```

**APK output:**
- `app-arm64-v8a-release.apk` (modern 64-bit ARM)
- `app-armeabi-v7a-release.apk` (legacy 32-bit ARM)
- `app-x86_64-release.apk` (emulators / x86_64 devices)
- `app-universal-release.apk` (all ABIs)

## 🚀 CI/CD Pipeline

Pipeline otomatis via GitHub Actions (`.github/workflows/ci.yml`):

1. **web-test**: Next.js lint + build
2. **android-test**: Gradle lint + unit tests + assembleRelease (signed)
3. **android-split-apks**: Verifikasi signature setiap APK dengan `apksigner`
4. **release**: Buat GitHub Release dengan semua artifacts (push to main only)

**Required secrets:**
- `KEYSTORE_BASE64` — base64-encoded keystore file
- `KEYSTORE_PASSWORD` — keystore password
- `KEY_ALIAS` — key alias
- `KEY_PASSWORD` — key password

## 📦 Releases

Release otomatis dibuat setiap push ke `main` branch. Setiap release berisi:
- 4 APK (per-ABI + universal) yang sudah signed
- Web build (zip)

Lihat: https://github.com/SecretArrow/Tahlil-Doa/releases

## 📚 Sumber Konten

Kitab Tahlil: Salik Channel

## 📄 License

MIT
