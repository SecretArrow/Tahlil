import { defineConfig } from "vitest/config";
import path from "node:path";

export default defineConfig({
  test: {
    environment: "node",
    include: ["src/**/*.test.ts", "src/**/*.test.tsx"],
    // Stub all CSS imports to an empty string. The unit tests are pure data
    // modules (no component rendering), but vitest still tries to load the
    // project's PostCSS config (postcss.config.mjs uses the string-form
    // plugin "@tailwindcss/postcss", which vitest's internal postcss loader
    // can't resolve as a plugin object -> "Invalid PostCSS Plugin found at
    // plugins[0]"). Returning '' for *.css prevents vitest from ever invoking
    // the postcss pipeline.
    server: {
      deps: {
        // Don't try to optimize/inline the postcss/tailwind modules.
        inline: [/vitest/, /@vitejs/],
      },
    },
  },
  plugins: [
    {
      name: "stub-css",
      enforce: "pre",
      resolveId(id) {
        // Intercept any CSS-related import so vitest never delegates to
        // its built-in CSS plugin (which loads postcss.config.mjs).
        if (/\.css(\?.*)?$/.test(id) || /\.scss$/.test(id)) {
          return `\0stubbed:${id}`;
        }
        return null;
      },
      load(id) {
        if (id.startsWith("\0stubbed:")) {
          return "export default ''";
        }
        return null;
      },
    },
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});