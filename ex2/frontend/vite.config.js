import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/auth': 'http://localhost:8080',
      '/users': 'http://localhost:8080',
      '/exams': 'http://localhost:8080',
      '/questions': 'http://localhost:8080',
      '/submissions': 'http://localhost:8080',
    }
  }
})
