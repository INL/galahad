// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import yaml from '@rollup/plugin-yaml'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), yaml()],
  server: {
    watch: {
      usePolling: true
    }
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
})