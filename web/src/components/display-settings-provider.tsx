"use client";

import * as React from "react";
import { DisplayToggles, defaultDisplayToggles } from "@/data/display-settings";
import type { ContentVariant } from "@/data/tahlil-content";
import { COOKIE_KEYS, getCookie, setCookie } from "@/lib/cookies/client";

type Ctx = {
  toggles: DisplayToggles;
  setToggle: (key: keyof DisplayToggles, value: boolean) => void;
  setAll: (next: DisplayToggles) => void;
  reset: () => void;
  variant: ContentVariant;
  setVariant: (next: ContentVariant) => void;
};

const VALID_VARIANTS: readonly ContentVariant[] = ["singkat", "lengkap"];

const DisplaySettingsContext = React.createContext<Ctx | null>(null);

function parseVariant(raw: unknown): ContentVariant {
  return raw === "lengkap" || raw === "singkat" ? raw : "singkat";
}

function parseStored(raw: string | null): DisplayToggles {
  if (!raw) return defaultDisplayToggles;
  try {
    const parsed = JSON.parse(raw) as Partial<DisplayToggles>;
    return {
      variant: parseVariant(parsed.variant),
      arabic: typeof parsed.arabic === "boolean" ? parsed.arabic : true,
      transliteration:
        typeof parsed.transliteration === "boolean"
          ? parsed.transliteration
          : true,
      translation:
        typeof parsed.translation === "boolean" ? parsed.translation : true,
      punctuateArabic:
        typeof parsed.punctuateArabic === "boolean"
          ? parsed.punctuateArabic
          : false,
      colorizeArabic:
        typeof parsed.colorizeArabic === "boolean"
          ? parsed.colorizeArabic
          : false,
    };
  } catch {
    return defaultDisplayToggles;
  }
}

const SERVER_INITIAL: DisplayToggles | null =
  typeof window !== "undefined"
    ? parseStored(getCookie(COOKIE_KEYS.displayToggles))
    : null;

const SERVER_INITIAL_VARIANT: ContentVariant =
  typeof window !== "undefined"
    ? parseVariant(getCookie(COOKIE_KEYS.variant))
    : "singkat";

export function DisplaySettingsProvider({
  children,
  initialToggles,
  initialVariant,
}: {
  children: React.ReactNode;
  initialToggles?: DisplayToggles;
  initialVariant?: ContentVariant;
}) {
  const [toggles, setToggles] = React.useState<DisplayToggles>(
    () => initialToggles ?? SERVER_INITIAL ?? defaultDisplayToggles,
  );
  // The variant lives in its own cookie (tahlil-variant-v1) so it can be
  // read server-side without parsing the toggles blob. `initialVariant` is
  // passed from layout.tsx (server-read cookie) so SSR matches the first
  // client render; on the client during hydration we also have
  // SERVER_INITIAL_VARIANT as a backstop for any code path that mounts the
  // provider without the prop.
  const [variant, setVariantState] = React.useState<ContentVariant>(
    () => initialVariant ?? SERVER_INITIAL_VARIANT,
  );

  const persist = React.useCallback((next: DisplayToggles) => {
    const value = JSON.stringify(next);
    setCookie(COOKIE_KEYS.displayToggles, value);
    if (typeof fetch !== "undefined") {
      void fetch("/api/settings", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ key: "displayToggles", value }),
        keepalive: true,
      }).catch(() => {});
    }
  }, []);

  const persistVariant = React.useCallback((next: ContentVariant) => {
    setCookie(COOKIE_KEYS.variant, next);
    if (typeof fetch !== "undefined") {
      void fetch("/api/settings", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ key: "variant", value: next }),
        keepalive: true,
      }).catch(() => {});
    }
  }, []);

  const update = React.useCallback(
    (next: DisplayToggles) => {
      setToggles(next);
      persist(next);
    },
    [persist],
  );

  const setVariant = React.useCallback(
    (next: ContentVariant) => {
      if (!VALID_VARIANTS.includes(next)) return;
      setVariantState(next);
      persistVariant(next);
      // Keep the variant mirrored inside the toggles blob too, so older
      // code paths that read it from there still see the right value.
      setToggles((prev) =>
        prev.variant === next ? prev : { ...prev, variant: next },
      );
    },
    [persistVariant],
  );

  const value = React.useMemo<Ctx>(
    () => ({
      toggles,
      setToggle: (key, val) =>
        update({ ...toggles, [key]: val } as DisplayToggles),
      setAll: (next) => update(next),
      reset: () => {
        update(defaultDisplayToggles);
        setVariant(defaultDisplayToggles.variant);
      },
      variant,
      setVariant,
    }),
    [toggles, update, variant, setVariant],
  );

  return (
    <DisplaySettingsContext.Provider value={value}>
      {children}
    </DisplaySettingsContext.Provider>
  );
}

export function useDisplaySettings() {
  const ctx = React.useContext(DisplaySettingsContext);
  if (!ctx) {
    throw new Error(
      "useDisplaySettings must be used within DisplaySettingsProvider",
    );
  }
  return ctx;
}
