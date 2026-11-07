import contentJson from "./tahlil-content.json";

export type ContentBlock = {
  arabic?: string | null;
  transliteration: string;
  translation: string;
  note?: string | null;
  repeat?: number | null;
};

export type SectionType =
  "pembukaan" | "sholawat" | "surah" | "tahlil" | "ayat" | "doa" | "dzikir";

export type Section = {
  id: string;
  number: number;
  title: string;
  subtitle?: string | null;
  type: SectionType;
  blocks: ContentBlock[];
};

export type ContentVariant = "singkat" | "lengkap";

type ContentData = {
  source: string;
  variants: Record<
    ContentVariant,
    { name: string; description: string; sections: Section[] }
  >;
};

const content = contentJson as ContentData;

export const tahlilVariants = content.variants;
export const tahlilSource = content.source;

export function getTahlilSections(variant: ContentVariant): Section[] {
  return content.variants[variant].sections;
}

/**
 * Look up a single section by its id within a given variant.
 * Returns `undefined` when no section matches.
 */
export function getTahlilSectionById(
  variant: ContentVariant,
  id: string,
): Section | undefined {
  return content.variants[variant].sections.find((s) => s.id === id);
}

// Backward-compat: default export keeps the singkat list (used by any code that
// hasn't been migrated to the variant selector).
export const tahlilSections: Section[] = getTahlilSections("singkat");

export const heroInfo = {
  title: "Tahlil",
  subtitle: "Majelis Tahlil",
  description:
    "Rangkaian zikir, tahlil, dan doa untuk mendoakan para arwah kaum muslimin. Disusun dari Al-Qur'an, hadits, dan amaliah ulama.",
  arabicBismillah: "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ",
  source: tahlilSource,
};
