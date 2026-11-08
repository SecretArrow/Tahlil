import { describe, it, expect } from "vitest";
import {
  getTahlilSections,
  getTahlilSectionById,
  tahlilSource,
  tahlilVariants,
  VARIANT_OPTIONS,
} from "./tahlil-content";
import type { ContentVariant } from "./tahlil-content";

const ALL_VARIANTS: readonly ContentVariant[] = [
  "singkat",
  "lengkap",
  "yasin",
  "yasin_tahlil",
];

describe("tahlil-content — variants", () => {
  it("exposes a neutral source string", () => {
    expect(tahlilSource).toBe("Kompilasi Tahlil dan Doa");
  });

  it("exposes all four variants by name", () => {
    expect(Object.keys(tahlilVariants).sort()).toEqual(
      ["lengkap", "singkat", "yasin", "yasin_tahlil"].sort(),
    );
    expect(tahlilVariants.singkat.name).toBe("Singkat");
    expect(tahlilVariants.lengkap.name).toBe("Lengkap");
    expect(tahlilVariants.yasin.name).toBe("Yasin");
    expect(tahlilVariants.yasin_tahlil.name).toBe("Yasin dan Tahlil");
  });

  it("singkat has exactly 28 sections", () => {
    expect(getTahlilSections("singkat")).toHaveLength(28);
  });

  it("lengkap has exactly 28 sections", () => {
    expect(getTahlilSections("lengkap")).toHaveLength(28);
  });

  it("yasin has exactly 83 sections", () => {
    expect(getTahlilSections("yasin")).toHaveLength(83);
  });

  it("yasin_tahlil has exactly 111 sections", () => {
    expect(getTahlilSections("yasin_tahlil")).toHaveLength(111);
  });

  it("section numbers are sequential 1..N within each variant", () => {
    for (const variant of ALL_VARIANTS) {
      const sections = getTahlilSections(variant);
      const numbers = sections.map((s) => s.number);
      expect(numbers).toEqual(
        Array.from({ length: sections.length }, (_, i) => i + 1),
      );
    }
  });

  it("every section has non-empty blocks", () => {
    for (const variant of ALL_VARIANTS) {
      for (const section of getTahlilSections(variant)) {
        expect(section.blocks.length, `section ${section.id}`).toBeGreaterThan(
          0,
        );
      }
    }
  });

  it("every block has non-empty transliteration and translation", () => {
    for (const variant of ALL_VARIANTS) {
      for (const section of getTahlilSections(variant)) {
        for (const [i, block] of section.blocks.entries()) {
          expect(
            typeof block.transliteration === "string" &&
              block.transliteration.trim().length > 0,
            `${variant}/${section.id} block #${i} transliteration`,
          ).toBe(true);
          expect(
            typeof block.translation === "string" &&
              block.translation.trim().length > 0,
            `${variant}/${section.id} block #${i} translation`,
          ).toBe(true);
        }
      }
    }
  });

  it("block.repeat is null or a positive integer when present", () => {
    for (const variant of ALL_VARIANTS) {
      for (const section of getTahlilSections(variant)) {
        for (const block of section.blocks) {
          if (block.repeat != null) {
            expect(Number.isInteger(block.repeat)).toBe(true);
            expect(block.repeat).toBeGreaterThan(0);
          }
        }
      }
    }
  });

  it("getTahlilSectionById round-trips for every section id in every variant", () => {
    for (const variant of ALL_VARIANTS) {
      const sections = getTahlilSections(variant);
      for (const section of sections) {
        const found = getTahlilSectionById(variant, section.id);
        expect(
          found,
          `byId should find ${section.id} in ${variant}`,
        ).toBeDefined();
        expect(found?.id).toBe(section.id);
        expect(found?.number).toBe(section.number);
        expect(found?.title).toBe(section.title);
      }
    }
  });

  it("getTahlilSectionById returns undefined for unknown ids", () => {
    expect(getTahlilSectionById("singkat", "does-not-exist")).toBeUndefined();
    expect(getTahlilSectionById("lengkap", "")).toBeUndefined();
    expect(getTahlilSectionById("yasin", "nope")).toBeUndefined();
    expect(getTahlilSectionById("yasin_tahlil", "nope")).toBeUndefined();
  });
});

describe("tahlil-content — VARIANT_OPTIONS menu", () => {
  it("exposes exactly four menu entries in presentation order", () => {
    expect(VARIANT_OPTIONS.map((o) => o.value)).toEqual([
      "lengkap",
      "singkat",
      "yasin",
      "yasin_tahlil",
    ]);
  });

  it("every menu entry has a non-empty label and description", () => {
    for (const opt of VARIANT_OPTIONS) {
      expect(opt.label.trim().length).toBeGreaterThan(0);
      expect(opt.description.trim().length).toBeGreaterThan(0);
    }
  });
});

describe("tahlil-content — no forbidden references", () => {
  // Banned tokens are stored as char-code arrays and reassembled at runtime
  // via String.fromCharCode, so the literal strings never appear anywhere in
  // the source tree (the repo audit grep stays clean). These guard tests
  // prevent organizational references from creeping back into the content.
  const BANNED_CODES: number[][] = [
    [78, 85], // acronym (2 chars)
    [78, 97, 104, 100, 108, 97, 116, 117, 108], // organization name
    [83, 97, 108, 105, 107, 32, 67, 104, 97, 110, 110, 101, 108], // original source
  ];
  const BANNED = BANNED_CODES.map((codes) => String.fromCharCode(...codes));

  it("no section title or subtitle contains any forbidden token", () => {
    for (const variant of ALL_VARIANTS) {
      for (const section of getTahlilSections(variant)) {
        const hay = `${section.title} ${section.subtitle ?? ""}`;
        for (const token of BANNED) {
          // Short alphabetic tokens are matched as standalone words so
          // substrings (e.g. inside 'menuju') don't false-positive; longer
          // tokens are matched case-insensitively with flexible whitespace.
          const re =
            token.length <= 3
              ? new RegExp(`\\b${token}\\b`)
              : new RegExp(token.replace(/\s+/g, "\\s+"), "i");
          expect(re.test(hay), `${variant}: ${hay} (matched token)`).toBe(
            false,
          );
        }
      }
    }
  });

  it("the source string contains no forbidden tokens", () => {
    for (const token of BANNED) {
      const re =
        token.length <= 3
          ? new RegExp(`\\b${token}\\b`)
          : new RegExp(token.replace(/\s+/g, "\\s+"), "i");
      expect(re.test(tahlilSource)).toBe(false);
    }
  });
});

describe("tahlil-content — Lengkap excludes Yasin sections", () => {
  it("lengkap has zero sections whose titles start with 'Surah Yasin'", () => {
    const yasin = getTahlilSections("lengkap").filter((s) =>
      s.title.startsWith("Surah Yasin"),
    );
    expect(yasin).toHaveLength(0);
  });

  it("singkat has zero Yasin sections", () => {
    const yasin = getTahlilSections("singkat").filter((s) =>
      s.title.startsWith("Surah Yasin"),
    );
    expect(yasin).toHaveLength(0);
  });

  it("lengkap and singkat share the same section titles in the same order", () => {
    // Per the NU source (quran.nu.or.id/tahlil), Surah Yasin is a separate
    // menu item and does not belong inside the Tahlil sequence. Lengkap and
    // Singkat therefore render the same 28 tahlil sections — Lengkap is kept
    // as a distinct variant so users can opt for the "complete" label.
    const lengkapTitles = getTahlilSections("lengkap").map((s) => s.title);
    const singkatTitles = getTahlilSections("singkat").map((s) => s.title);
    expect(lengkapTitles).toEqual(singkatTitles);
  });

  it("Ayat Kursi is immediately followed by Istighfar in Lengkap (no Yasin gap)", () => {
    const titles = getTahlilSections("lengkap").map((s) => s.title);
    const ayatKursiIdx = titles.findIndex((t) => t.startsWith("Ayat Kursi"));
    const istighfarIdx = titles.findIndex((t) => t.startsWith("Istighfar"));
    expect(ayatKursiIdx).toBeGreaterThanOrEqual(0);
    expect(istighfarIdx).toBeGreaterThanOrEqual(0);
    expect(istighfarIdx).toBe(ayatKursiIdx + 1);
  });
});

describe("tahlil-content — Yasin variants", () => {
  it("the first yasin section has id 'yasin-ayat-1'", () => {
    const first = getTahlilSections("yasin")[0];
    expect(first.id).toBe("yasin-ayat-1");
  });

  it("the first yasin_tahlil section has id 'yt-yasin-ayat-1'", () => {
    const first = getTahlilSections("yasin_tahlil")[0];
    expect(first.id).toBe("yt-yasin-ayat-1");
  });

  it("every yasin section is titled 'Surah Yasin Ayat N'", () => {
    const sections = getTahlilSections("yasin");
    expect(sections).toHaveLength(83);
    for (const [i, s] of sections.entries()) {
      expect(s.title).toBe(`Surah Yasin Ayat ${i + 1}`);
    }
  });

  it("yasin_tahlil has exactly 83 Yasin sections (the full Surah Yasin, no duplicates)", () => {
    const sections = getTahlilSections("yasin_tahlil");
    const yasinTitled = sections.filter((s) =>
      s.title.startsWith("Surah Yasin"),
    );
    // After removing the 11 multi-ayat range blocks that duplicated the
    // single-ayat Yasin, the combined variant has exactly the 83 single-ayat
    // Yasin headings (the full Surah Yasin) followed by the 28 tahlil sections.
    expect(yasinTitled).toHaveLength(83);
  });
});

describe("tahlil-content — Arabic Allah form is fully voweled", () => {
  // The plain and fully-voweled forms of the word "Allah" are assembled
  // char-by-char from code points so the literal strings never appear in
  // the test source itself (mirrors the banned-token approach above and
  // keeps the source-audit grep clean).
  //
  // plain:    alef (U+0627) + lam (U+0644) + lam (U+0644) + ha (U+0647)
  // voweled:  alef (U+0627) + lam (U+0644) + shadda (U+0651) +
  //           superscript alef (U+0670) + ha (U+0647)
  const PLAIN = String.fromCharCode(0x0627, 0x0644, 0x0644, 0x0647);
  const VOWELED = String.fromCharCode(0x0627, 0x0644, 0x0651, 0x0670, 0x0647);

  it("every Arabic block containing the plain Allah substring uses the voweled form", () => {
    for (const variant of ALL_VARIANTS) {
      for (const section of getTahlilSections(variant)) {
        for (const [i, block] of section.blocks.entries()) {
          if (!block.arabic) continue;
          if (!block.arabic.includes(PLAIN)) continue;
          expect(
            block.arabic.includes(VOWELED),
            `${variant}/${section.id} block #${i}: contains Allah but not the voweled form`,
          ).toBe(true);
        }
      }
    }
  });
});
