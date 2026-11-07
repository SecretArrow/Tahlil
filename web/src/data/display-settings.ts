import type { ContentVariant } from "./tahlil-content";

export type DisplayToggles = {
  /**
   * Which content variant to render: "singkat" (28 sections, no Yasin)
   * or "lengkap" (39 sections, adds Yasin ayat 15-25). Stored alongside
   * the boolean toggles so a single cookie round-trip covers everything.
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
  colorizeArabic: false,
};
