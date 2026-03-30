import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  // 【新增】强制指定一个新端口
  server: {
    port: 8888,
    strictPort: true // 如果 8888 被占用，直接报错，不自动顺延
  }
})