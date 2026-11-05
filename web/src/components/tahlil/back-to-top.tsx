"use client";

import * as React from "react";
import { motion, useScroll, useMotionValueEvent } from "framer-motion";
import { ChevronUp } from "lucide-react";

export function BackToTop() {
  const { scrollY } = useScroll();
  const [show, setShow] = React.useState(false);

  useMotionValueEvent(scrollY, "change", (v) => {
    setShow(v > 600);
  });

  return (
    <motion.button
      type="button"
      onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}
      aria-label="Kembali ke atas"
      initial={false}
      animate={{
        opacity: show ? 1 : 0,
        scale: show ? 1 : 0.7,
        pointerEvents: show ? "auto" : "none",
      }}
      transition={{ duration: 0.25 }}
      className="fixed bottom-5 right-5 z-40 flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-emerald-600 to-teal-700 text-white shadow-lg shadow-emerald-900/30 transition-transform hover:scale-110 dark:from-emerald-500 dark:to-teal-600 sm:bottom-6 sm:right-6"
    >
      <ChevronUp className="h-5 w-5" strokeWidth={2.5} />
    </motion.button>
  );
}
