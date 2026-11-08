import { test, expect } from "@playwright/test";

/**
 * Variant menu — Tahlil Lengkap / Tahlil Singkat / Yasin / Yasin dan Tahlil.
 *
 * The home page renders a single radiogroup labelled "Pilih varian tahlil"
 * inside the hero, with four radio cards (one per variant). Switching
 * selection updates the section list rendered below.
 *
 * Variant → section counts (after the Yasin-removal fix; per the NU source
 * at quran.nu.or.id/tahlil, Surah Yasin is a SEPARATE menu item and does
 * not belong inside the Tahlil sequence):
 *   - singkat      → 28 sections (no Yasin headings)
 *   - lengkap      → 28 sections (no Yasin headings — identical to Singkat;
 *                    kept as a distinct variant so users can opt for the
 *                    "complete" label and so the variant menu stays stable)
 *   - yasin        → 83 sections, all "Surah Yasin Ayat N"
 *   - yasin_tahlil → 111 sections: the full 83-ayat Yasin first, then the
 *                    28-section tahlil sequence = 83 "Surah Yasin" headings.
 */
test.describe("Variant menu — four options", () => {
  test("Lengkap and Singkat both show no Surah Yasin sections", async ({
    page,
  }) => {
    await page.goto("/");

    // Sanity check — the variant radiogroup is present in the hero.
    const variantGroup = page.getByRole("radiogroup", {
      name: "Pilih varian tahlil",
    });
    await expect(variantGroup).toBeVisible();

    // The radiogroup exposes four radio cards.
    await expect(variantGroup.getByRole("radio")).toHaveCount(4);

    // Default variant is Singkat → no Yasin headings on the page.
    const yasinHeading = page.getByRole("heading", {
      level: 2,
      name: /^Surah Yasin/,
    });
    await expect(yasinHeading).toHaveCount(0);

    // Switch to Lengkap via the hero radiogroup. Per the NU source
    // (quran.nu.or.id/tahlil), Tahlil Lengkap does NOT include Surah
    // Yasin — Yasin is read separately, before the tahlil sequence.
    // So Lengkap must also show zero Yasin headings.
    await variantGroup.getByRole("radio", { name: /Tahlil Lengkap/ }).click();
    await expect(
      variantGroup.getByRole("radio", { name: /Tahlil Lengkap/ }),
    ).toHaveAttribute("aria-checked", "true");
    await expect(yasinHeading).toHaveCount(0);

    // Lengkap should still render the tahlil sequence — assert a known
    // non-Yasin heading is present (Ayat Kursi is section 14 in both
    // Singkat and Lengkap).
    const ayatKursi = page.getByRole("heading", {
      level: 2,
      name: /Ayat Kursi/,
    });
    await expect(ayatKursi.first()).toBeVisible();

    // Switch back to Singkat — still no Yasin headings.
    await variantGroup.getByRole("radio", { name: /Tahlil Singkat/ }).click();
    await expect(
      variantGroup.getByRole("radio", { name: /Tahlil Singkat/ }),
    ).toHaveAttribute("aria-checked", "true");
    await expect(yasinHeading).toHaveCount(0);
  });

  test("tapping the Yasin radio reveals all 83 Surah Yasin sections", async ({
    page,
  }) => {
    await page.goto("/");

    const variantGroup = page.getByRole("radiogroup", {
      name: "Pilih varian tahlil",
    });
    await expect(variantGroup).toBeVisible();

    const yasinHeading = page.getByRole("heading", {
      level: 2,
      name: /^Surah Yasin Ayat/,
    });

    // Before switching, default (Singkat) shows no Yasin headings.
    await expect(yasinHeading).toHaveCount(0);

    // Switch to Yasin.
    await variantGroup.getByRole("radio", { name: /^Yasin$/ }).click();
    await expect(
      variantGroup.getByRole("radio", { name: /^Yasin$/ }),
    ).toHaveAttribute("aria-checked", "true");

    // The Yasin variant has all 83 ayat as standalone sections titled
    // "Surah Yasin Ayat N".
    await expect(yasinHeading.first()).toBeVisible();
    await expect(yasinHeading).toHaveCount(83);
  });

  test("tapping the Yasin dan Tahlil radio reveals 83 Surah Yasin headings", async ({
    page,
  }) => {
    await page.goto("/");

    const variantGroup = page.getByRole("radiogroup", {
      name: "Pilih varian tahlil",
    });
    await expect(variantGroup).toBeVisible();

    const yasinHeading = page.getByRole("heading", {
      level: 2,
      name: /^Surah Yasin/,
    });

    // Before switching, default (Singkat) shows no Yasin headings.
    await expect(yasinHeading).toHaveCount(0);

    // Switch to Yasin dan Tahlil.
    await variantGroup.getByRole("radio", { name: /Yasin dan Tahlil/ }).click();
    await expect(
      variantGroup.getByRole("radio", { name: /Yasin dan Tahlil/ }),
    ).toHaveAttribute("aria-checked", "true");

    // The combined variant opens with the full 83-ayat Yasin (83 headings
    // titled "Surah Yasin Ayat N"), then appends the 28-section tahlil
    // sequence — which, after the Yasin-removal fix, contains NO further
    // Yasin blocks. So the total "Surah Yasin" heading count is exactly 83.
    await expect(yasinHeading.first()).toBeVisible();
    await expect(yasinHeading).toHaveCount(83);
  });
});
