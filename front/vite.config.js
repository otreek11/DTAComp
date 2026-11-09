import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/conteudos': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/tags': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/usuarios': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/arquivos': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
