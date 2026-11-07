import { defineConfig } from "vitest/config";
import path from "node:path";

export default defineConfig({
  test: {
    environment: "node",
    include: ["src/**/*.test.ts", "src/**/*.test.tsx"],
    // Disable CSS processing entirely. The unit tests exercise pure data
    // modules (no component rendering), and vitest otherwise tries to load
    // the project's PostCSS config (Tailwind v4 plugin), which fails with
    // "Invalid PostCSS Plugin" because the Tailwind v4 plugin object isn't
    // a PostCSS 8 plugin in the legacy sense vitest expects.
    css: false,
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
