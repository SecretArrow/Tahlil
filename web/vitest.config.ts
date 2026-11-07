import { defineConfig } from "vitest/config";
import path from "node:path";

export default defineConfig({
  test: {
    environment: "node",
    include: ["src/**/*.test.ts", "src/**/*.test.tsx"],
    // Vitest inherits vite's built-in JSON plugin, so `import x from
    // "./file.json"` resolves correctly — no extra config needed. The earlier
    // CSS-stub plugin was removed once postcss.config.mjs was fixed to import
    // the @tailwindcss/postcss plugin as a function (vitest now loads the
    // postcss config without error).
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});