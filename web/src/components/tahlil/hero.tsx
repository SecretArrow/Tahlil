"use client";

import * as React from "react";
import { motion } from "framer-motion";
import { useDisplaySettings } from "@/components/display-settings-provider";
import type { ContentVariant } from "@/data/tahlil-content";
import { getTahlilSections } from "@/data/tahlil-content";

type CtaButton = {
  href: string;
  label: string;
  variant: "primary" | "secondary";
};

const ctaButtons: CtaButton[] = [
  { href: "#pembukaan", label: "Mulai", variant: "primary" },
  { href: "#tahlil-ahli-kubur", label: "Tahlil", variant: "secondary" },
  { href: "#dzikir", label: "Dzikir", variant: "secondary" },
  { href: "#doa-pembuka", label: "Doa", variant: "secondary" },
];

const VARIANT_OPTIONS: ReadonlyArray<{
  value: ContentVariant;
  label: string;
  short: string;
}> = [
  { value: "singkat", label: "Singkat", short: "S" },
  { value: "lengkap", label: "Lengkap", short: "L" },
];

function VariantToggle() {
  const { variant, setVariant } = useDisplaySettings();
  return (
    <div
      role="radiogroup"
      aria-label="Pilih varian tahlil"
      className="inline-flex items-center rounded-full border border-border bg-card/60 p-1 backdrop-blur"
    >
      {VARIANT_OPTIONS.map((opt) => {
        const active = variant === opt.value;
        const count = getTahlilSections(opt.value).length;
        return (
          <button
            key={opt.value}
            type="button"
            role="radio"
            aria-checked={active}
            onClick={() => setVariant(opt.value)}
            className={`relative inline-flex items-center gap-1.5 rounded-full px-4 py-2 text-sm font-semibold transition-colors ${
              active
                ? "bg-primary text-primary-foreground shadow"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            {opt.label}
            <span
              className={`rounded-full px-1.5 py-0.5 text-[10px] font-medium ${
                active
                  ? "bg-primary-foreground/20 text-primary-foreground"
                  : "bg-muted text-muted-foreground"
              }`}
            >
              {count}
            </span>
          </button>
        );
      })}
    </div>
  );
}

export function Hero() {
  return (
    <section
      id="top"
      className="relative overflow-hidden pt-28 pb-14 sm:pt-32 lg:pt-40 lg:pb-20"
    >
      {/* Decorative background orbs */}
      <div className="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1.2 }}
          className="absolute -top-32 -right-24 h-72 w-72 rounded-full bg-emerald-500/15 blur-3xl dark:bg-emerald-500/20"
        />
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1.2, delay: 0.2 }}
          className="absolute top-20 -left-20 h-72 w-72 rounded-full bg-amber-400/15 blur-3xl dark:bg-amber-400/15"
        />
      </div>

      <div className="mx-auto max-w-4xl px-4 text-center sm:px-6 lg:px-8">
        {/* Variant toggle — Singkat / Lengkap */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="flex justify-center"
        >
          <VariantToggle />
        </motion.div>

        {/* CTA buttons */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="mt-9 flex flex-wrap items-center justify-center gap-3"
        >
          {ctaButtons.map((btn) => (
            <a
              key={btn.href}
              href={btn.href}
              className={
                btn.variant === "primary"
                  ? "inline-flex items-center justify-center rounded-full bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground shadow-md transition-all hover:shadow-lg hover:brightness-110 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  : "inline-flex items-center justify-center rounded-full border border-border bg-card/60 px-6 py-3 text-sm font-semibold text-foreground backdrop-blur transition-all hover:bg-accent"
              }
            >
              {btn.label}
            </a>
          ))}
        </motion.div>
      </div>
    </section>
  );
}
