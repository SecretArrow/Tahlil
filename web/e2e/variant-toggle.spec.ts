import { test, expect } from "@playwright/test";

/**
 * Variant menu — Tahlil Lengkap / Tahlil Singkat / Yasin / Yasin dan Tahlil.
 *
 * The home page renders a single radiogroup labelled "Pilih varian tahlil"
 * inside the hero, with four radio cards (one per variant). Switching
 * selection updates the section list rendered below.
 *
 * Variant → section counts:
 *   - singkat      → 28 sections (no Yasin headings)
 *   - lengkap      → 39 sections (11 Yasin section headings)
 *   - yasin        → 83 sections, all "Surah Yasin Ayat N"
 *   - yasin_tahlil → 122 sections: 83 single-ayat Yasin headings + 11
 *                    multi-ayat Yasin headings (the Lengkap-style picks)
 *                    = 94 "Surah Yasin" headings in total.
 */
test.describe("Variant menu — four options", () => {
  test("switching to Lengkap reveals Surah Yasin sections, Singkat hides them", async ({
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

    // Switch to Lengkap via the hero radiogroup.
    await variantGroup.getByRole("radio", { name: /Tahlil Lengkap/ }).click();
    await expect(
      variantGroup.getByRole("radio", { name: /Tahlil Lengkap/ }),
    ).toHaveAttribute("aria-checked", "true");

    // Wait for the Yasin sections to render (Lengkap adds 11 of them).
    await expect(yasinHeading.first()).toBeVisible();
    await expect(yasinHeading).toHaveCount(11);

    // Switch back to Singkat — Yasin headings should disappear.
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

  test("tapping the Yasin dan Tahlil radio reveals 94 Surah Yasin headings", async ({
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

    // The combined variant includes:
    //   - 83 single-ayat Yasin sections ("Surah Yasin Ayat N")
    //   - 11 multi-ayat Yasin sections ("Surah Yasin Ayat N-M") inherited
    //     from the Lengkap list
    // for a total of 94 "Surah Yasin" headings.
    await expect(yasinHeading.first()).toBeVisible();
    await expect(yasinHeading).toHaveCount(94);
  });
});
