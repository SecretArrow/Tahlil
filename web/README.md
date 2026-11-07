# Tahlil & Doa — Web

Aplikasi web Majelis Tahlil & Doa (sumber: "Kompilasi Tahlil dan Doa") — rangkaian zikir, tahlil, Al-Fatihah, Ayat Kursi, Surah Yasin (varian Lengkap), dan doa untuk mendoakan para arwah kaum muslimin.

## Fitur

- Dua varian konten: **Singkat** (28 bagian) dan **Lengkap** (39 bagian, tambah 11 bagian Surah Yasin ayat 15-25) — bisa dipilih lewat tombol di hero atau panel Tampilan Bacaan
- 3 mode font Arab: Standar, Gaya Mushaf Utsmani, Naskhi Tebal
- Toggle show/hide Arab/Latin/Terjemah
- Toggle "Koma pada Teks Arab" (auto tambah koma Arab mengikuti terjemahan)
- Toggle "Warnai Kata Arab" (warna bergantian per kata)
- Slider tipografi: ukuran, jarak baris, jarak kata, jarak huruf
- Mode gelap/terang dengan auto-save via cookie
- Tanda waqf mushaf ۝X dengan CSS-drawn circle
- Teks Arab dipecah per kalimat dengan tanda di akhir baris

## Varian Singkat / Lengkap

Varian menentukan rangkaian bacaan yang ditampilkan:

- **Singkat** — 28 bagian, tanpa Surah Yasin. Cocok untuk majelis tahlil harian yang singkat.
- **Lengkap** — 39 bagian, menambahkan 11 bagian Surah Yasin (ayat 15-25) di antara Ayat Kursi dan Istighfar.

Pilihan tersimpan di cookie `tahlil-variant-v1` (1 tahun) dan dikirim juga ke endpoint `/api/settings`. Server membaca cookie ini saat SSR supaya render pertama langsung sesuai pilihan pengguna (tanpa hydration mismatch).

## Stack

- Next.js 16 App Router + TypeScript
- Tailwind CSS v4
- shadcn/ui
- Framer Motion
- next-themes (cookie-based)

## Pengembangan

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
```

## Testing

Unit test (vitest):

```bash
npm test
```

End-to-end test (Playwright — menjalankan `npm run build && npm run start`):

```bash
npx playwright install --with-deps chromium
npm run test:e2e
```

## Sumber

Konten: "Kompilasi Tahlil dan Doa".
