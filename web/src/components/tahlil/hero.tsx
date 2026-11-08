"use client";

import * as React from "react";
import { motion } from "framer-motion";
import { useDisplaySettings } from "@/components/display-settings-provider";
import { getTahlilSections, VARIANT_OPTIONS } from "@/data/tahlil-content";

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

function VariantMenu() {
  const { variant, setVariant } = useDisplaySettings();
  return (
    <div
      role="radiogroup"
      aria-label="Pilih varian tahlil"
      className="grid grid-cols-1 gap-3 sm:grid-cols-2 max-w-2xl mx-auto"
    >
      {VARIANT_OPTIONS.map((opt, i) => {
        const active = variant === opt.value;
        const count = getTahlilSections(opt.value).length;
        return (
          <motion.button
            key={opt.value}
            type="button"
            role="radio"
            aria-checked={active}
            aria-label={opt.label}
            onClick={() => setVariant(opt.value)}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, delay: 0.1 + i * 0.05 }}
            className={`group flex flex-col gap-1 rounded-2xl border p-4 text-left transition-all ${
              active
                ? "border-primary bg-primary/5"
                : "border-border bg-card hover:bg-accent"
            }`}
          >
            <div className="flex items-center justify-between gap-2">
              <span className="font-semibold text-foreground">{opt.label}</span>
              <span
                className={`shrink-0 rounded-full px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide ${
                  active
                    ? "bg-primary/15 text-primary"
                    : "bg-muted text-muted-foreground"
                }`}
              >
                {count} bagian
              </span>
            </div>
            <p className="text-xs text-muted-foreground">{opt.description}</p>
          </motion.button>
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
        {/* Variant selector — 4-item menu */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
        >
          <VariantMenu />
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
