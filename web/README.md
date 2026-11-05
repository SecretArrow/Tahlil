# Tahlil & Doa — Web

Aplikasi web Tahlil Singkat (dari kitab Salik Channel) — rangkaian zikir, tahlil, Al-Fatihah, Ayat Kursi, dan doa untuk mendoakan para arwah kaum muslimin.

## Fitur

- 11 section lengkap (Pembukaan → Al-Fatihah Penutup)
- 3 mode font Arab: Standar, Gaya Mushaf Utsmani, Naskhi Tebal
- Toggle show/hide Arab/Latin/Terjemah
- Toggle "Koma pada Teks Arab" (auto tambah koma Arab mengikuti terjemahan)
- Toggle "Warnai Kata Arab" (warna bergantian per kata)
- Slider tipografi: ukuran, jarak baris, jarak kata, jarak huruf
- Mode gelap/terang dengan auto-save via cookie
- Tanda waqf mushaf ۝X dengan CSS-drawn circle
- Teks Arab dipecah per kalimat dengan tanda di akhir baris

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

## Sumber

Kitab Tahlil: Salik Channel
