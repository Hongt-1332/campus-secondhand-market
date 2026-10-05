import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5176, // 固定端口，避免自动跳到5177
    proxy: {
      // 后端业务接口
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/images': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // AI代理，转发DeepSeek
      '/api-ai': {
        target: 'https://api.deepseek.com',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api-ai/, ''),
        timeout: 20000
      }
    }
  }
})