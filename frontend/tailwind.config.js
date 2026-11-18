/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        tamil: {
          crimson: '#8B0000',
          gold: '#D4AF37',
          terracotta: '#C85A32',
          deepBlue: '#0F172A',
          tan: '#FDFBF7',
        }
      }
    },
  },
  plugins: [],
};
