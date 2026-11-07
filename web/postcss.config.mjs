import tailwindcss from "@tailwindcss/postcss";

// Import the Tailwind v4 PostCSS plugin as a FUNCTION (not the string
// form) so that PostCSS 8 — and vitest's internal postcss loader, which
// can't resolve string-form plugins — both recognise it. This is the
// canonical Tailwind v4 + Next.js 16 setup.
const config = {
  plugins: [tailwindcss],
};

export default config;
