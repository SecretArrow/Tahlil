import { test, expect } from "@playwright/test";

/**
 * Variant toggle — Singkat / Lengkap.
 *
 * The home page renders a radiogroup labelled "Pilih varian tahlil"
 * (in the hero) plus a second one labelled "Varian Tahlil" inside the
 * display-settings panel. Either one switches the content between the
 * 28-section Singkat list and the 39-section Lengkap list (which adds
 * 11 Surah Yasin sections).
 */
test.describe("Singkat / Lengkap variant toggle", () => {
  test("switching to Lengkap reveals Surah Yasin sections, Singkat hides them", async ({
    page,
  }) => {
    await page.goto("/");

    // Sanity check — the variant radiogroup is present in the hero.
    const variantGroup = page.getByRole("radiogroup", {
      name: "Pilih varian tahlil",
    });
    await expect(variantGroup).toBeVisible();

    // Default variant is Singkat → no Yasin headings on the page.
    const yasinHeading = page.getByRole("heading", {
      level: 2,
      name: /^Surah Yasin/,
    });
    await expect(yasinHeading).toHaveCount(0);

    // Switch to Lengkap via the hero radiogroup.
    await variantGroup.getByRole("radio", { name: /Lengkap/ }).click();
    await expect(
      variantGroup.getByRole("radio", { name: /Lengkap/ }),
    ).toHaveAttribute("aria-checked", "true");

    // Wait for the Yasin sections to render (Lengkap adds 11 of them).
    await expect(yasinHeading.first()).toBeVisible();
    await expect(yasinHeading).toHaveCount(11);

    // Switch back to Singkat — Yasin headings should disappear.
    await variantGroup.getByRole("radio", { name: /Singkat/ }).click();
    await expect(
      variantGroup.getByRole("radio", { name: /Singkat/ }),
    ).toHaveAttribute("aria-checked", "true");
    await expect(yasinHeading).toHaveCount(0);
  });

  test("the settings panel exposes the same variant control", async ({
    page,
  }) => {
    await page.goto("/");

    // Open the display-settings panel via the header button.
    await page
      .getByRole("button", { name: "Pengaturan tampilan bacaan" })
      .click();

    const panel = page.getByRole("dialog", {
      name: "Pengaturan tampilan bacaan",
    });
    await expect(panel).toBeVisible();

    const panelGroup = panel.getByRole("radiogroup", { name: "Varian Tahlil" });
    await expect(panelGroup).toBeVisible();

    await panelGroup.getByRole("radio", { name: /Lengkap/ }).click();
    await expect(
      panelGroup.getByRole("radio", { name: /Lengkap/ }),
    ).toHaveAttribute("aria-checked", "true");

    // The hero radiogroup should reflect the same active variant.
    const heroGroup = page.getByRole("radiogroup", {
      name: "Pilih varian tahlil",
    });
    await expect(
      heroGroup.getByRole("radio", { name: /Lengkap/ }),
    ).toHaveAttribute("aria-checked", "true");
  });
});
