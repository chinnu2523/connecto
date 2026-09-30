import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  base: './',
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'https://connecto.fun',
        changeOrigin: true,
        secure: false,
        ws: true
      },
      '/uploads': {
        target: 'https://connecto.fun',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
