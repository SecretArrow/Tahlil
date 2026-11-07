import { describe, it, expect } from "vitest";
import {
  getTahlilSections,
  getTahlilSectionById,
  tahlilSource,
  tahlilVariants,
} from "./tahlil-content";

describe("tahlil-content — variants", () => {
  it("exposes a neutral source string", () => {
    expect(tahlilSource).toBe("Kompilasi Tahlil dan Doa");
  });

  it("exposes both variants by name", () => {
    expect(Object.keys(tahlilVariants).sort()).toEqual(["lengkap", "singkat"]);
    expect(tahlilVariants.singkat.name).toBe("Singkat");
    expect(tahlilVariants.lengkap.name).toBe("Lengkap");
  });

  it("singkat has exactly 28 sections", () => {
    expect(getTahlilSections("singkat")).toHaveLength(28);
  });

  it("lengkap has exactly 39 sections", () => {
    expect(getTahlilSections("lengkap")).toHaveLength(39);
  });

  it("section numbers are sequential 1..N within each variant", () => {
    for (const variant of ["singkat", "lengkap"] as const) {
      const sections = getTahlilSections(variant);
      const numbers = sections.map((s) => s.number);
      expect(numbers).toEqual(
        Array.from({ length: sections.length }, (_, i) => i + 1),
      );
    }
  });

  it("every section has non-empty blocks", () => {
    for (const variant of ["singkat", "lengkap"] as const) {
      for (const section of getTahlilSections(variant)) {
        expect(section.blocks.length, `section ${section.id}`).toBeGreaterThan(
          0,
        );
      }
    }
  });

  it("every block has non-empty transliteration and translation", () => {
    for (const variant of ["singkat", "lengkap"] as const) {
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
    for (const variant of ["singkat", "lengkap"] as const) {
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

  it("getTahlilSectionById round-trips for every section id in both variants", () => {
    for (const variant of ["singkat", "lengkap"] as const) {
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
  });
});

describe("tahlil-content — no forbidden references", () => {
  // Banned tokens are kept base64-encoded so the literal strings never
  // appear anywhere in the source tree. Decoded at runtime for the guard
  // tests below — these prevent organizational references from creeping
  // back into the content.
  const BANNED_B64 = "WyJOVSIsICJOYWhkbGF0dWwiLCAiU2FsaWsgQ2hhbm5lbCJd";
  const BANNED = JSON.parse(
    typeof Buffer !== "undefined"
      ? Buffer.from(BANNED_B64, "base64").toString("utf-8")
      : atob(BANNED_B64),
  ) as string[];

  it("no section title or subtitle contains any forbidden token", () => {
    for (const variant of ["singkat", "lengkap"] as const) {
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

describe("tahlil-content — Lengkap includes Yasin sections", () => {
  it("lengkap has 11 sections whose titles start with 'Surah Yasin'", () => {
    const yasin = getTahlilSections("lengkap").filter((s) =>
      s.title.startsWith("Surah Yasin"),
    );
    expect(yasin).toHaveLength(11);
  });

  it("singkat has zero Yasin sections", () => {
    const yasin = getTahlilSections("singkat").filter((s) =>
      s.title.startsWith("Surah Yasin"),
    );
    expect(yasin).toHaveLength(0);
  });

  it("the Yasin sections sit between Ayat Kursi and Istighfar in Lengkap", () => {
    const titles = getTahlilSections("lengkap").map((s) => s.title);
    const ayatKursiIdx = titles.findIndex((t) => t.startsWith("Ayat Kursi"));
    const firstYasinIdx = titles.findIndex((t) => t.startsWith("Surah Yasin"));
    const istighfarIdx = titles.findIndex((t) => t.startsWith("Istighfar"));
    expect(ayatKursiIdx).toBeGreaterThanOrEqual(0);
    expect(firstYasinIdx).toBeGreaterThan(ayatKursiIdx);
    expect(istighfarIdx).toBeGreaterThan(firstYasinIdx);
  });
});
