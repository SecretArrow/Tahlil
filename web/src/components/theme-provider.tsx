"use client";

import * as React from "react";
import { ThemeProvider as NextThemesProvider } from "next-themes";

export function ThemeProvider({
  children,
  ...props
}: React.ComponentProps<typeof NextThemesProvider>) {
  return (
    <NextThemesProvider
      // Persist theme to a cookie named `tahlil-theme` so the server can
      // read it on next request (avoids flash of wrong theme on reload).
      cookieName="tahlil-theme"
      // Also keep localStorage as a secondary fallback (next-themes default).
      storage="cookie"
      {...props}
    >
      {children}
    </NextThemesProvider>
  );
}
