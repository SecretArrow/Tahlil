import type { ContentVariant } from "./tahlil-content";

export type DisplayToggles = {
  /**
   * Which content variant to render. One of "singkat" (28 sections),
   * "lengkap" (39 sections, adds Surah Yasin 15-25), "yasin" (Surah Yasin
   * lengkap, 83 ayat), or "yasin_tahlil" (Yasin + Tahlil Lengkap combined,
   * 122 sections). Stored alongside the boolean toggles so a single cookie
   * round-trip covers everything. Default is "singkat".
   */
  variant: ContentVariant;
  arabic: boolean;
  transliteration: boolean;
  translation: boolean;
  /**
   * When true, the Arabic text is post-processed to insert Arabic commas
   * (،) at clause/list boundaries — mirroring the punctuation that already
   * exists in the Indonesian translation. Off by default so the Arabic
   * renders as authored. Quran verses (with ۝ or ۩ marks) are never
   * modified.
   */
  punctuateArabic: boolean;
  /**
   * When true (default), each Arabic comma (،) and Latin period (.) in
   * Arabic text starts a new line — the long-standing hardcoded behaviour
   * in `splitBySentence`. When false, the Arabic text flows as a single
   * block (no line break per comma); it still word-wraps naturally via
   * CSS. Ayah marks (۝X) are real verse boundaries and are always split
   * regardless of this toggle.
   */
  breakOnComma: boolean;
  /**
   * When true, each Arabic word is wrapped in a colored span, alternating
   * between two shades so consecutive words are visually distinguishable.
   * The two shades are picked based on the active theme:
   *   - Light theme (dark text)   → #000000 and #011b45
   *   - Dark theme  (light text)  → #ffffff and #d9dadb
   * Off by default.
   */
  colorizeArabic: boolean;
};

export const defaultDisplayToggles: DisplayToggles = {
  variant: "singkat",
  arabic: true,
  transliteration: true,
  translation: true,
  punctuateArabic: false,
  breakOnComma: true,
  colorizeArabic: false,
};
