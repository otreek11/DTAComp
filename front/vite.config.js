import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
// O front chama a API em "/api/*" (ver src/services/api.js); o proxy remove o prefixo.
// Assim as rotas da SPA (/admin, /revisao, ...) nunca colidem com as do backend.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
