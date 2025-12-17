/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "#1A2B3C", // Navy
          foreground: "#FFFFFF",
        },
        secondary: {
          DEFAULT: "#4ECDC4", // Teal
          foreground: "#1A2B3C",
        },
        accent: {
          DEFAULT: "#FF6B00", // Orange
          foreground: "#FFFFFF",
        },
        destructive: {
          DEFAULT: "#F44336", // Red
          foreground: "#FFFFFF",
        },
        muted: {
          DEFAULT: "#B0B0B0", // Gray
          foreground: "#FFFFFF",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
        success: "#009688",
        warning: "#FF9800",
        info: "#2196F3",
      },
      fontFamily: {
        sans: ["Noto Sans KR", "sans-serif"],
        display: ["Montserrat", "sans-serif"],
        mono: ["Roboto Mono", "monospace"],
      },
      borderRadius: {
        lg: "8px", // Large
        md: "6px", // Medium
        sm: "4px", // Subtle
      },
    },
  },
  plugins: [],
}
