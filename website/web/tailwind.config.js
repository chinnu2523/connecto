/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        darkspace: '#0B0E14',
        darkglass: '#161B26',
        darkcard: '#1E2538',
        electric: '#6C5CE7',
        cyanglow: '#00CEC9',
        neonpink: '#FD79A8'
      }
    },
  },
  plugins: [],
}
