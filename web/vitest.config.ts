import { defineConfig } from "vitest/config";
import path from "node:path";

export default defineConfig({
  test: {
    environment: "node",
    include: ["src/**/*.test.ts", "src/**/*.test.tsx"],
    // Disable CSS processing entirely. The unit tests are pure data modules
    // (no component rendering), and enabling CSS would make vitest load the
    // project's postcss.config.mjs — which uses the string-form plugin
    // "@tailwindcss/postcss" that vitest's internal postcss loader can't
    // resolve ("Invalid PostCSS Plugin found at plugins[0]"). The string
    // form is required by Next.js 16 Turbopack (it lazy-requires and can't
    // bundle native .node addons), so we keep it and just skip CSS in tests.
    css: false,
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});