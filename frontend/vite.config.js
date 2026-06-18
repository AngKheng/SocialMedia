import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  define: {
    // sockjs-client được viết cho Node.js, tham chiếu biến `global`
    // mà Vite (browser-based) không có sẵn → cần polyfill thủ công
    global: 'window',
  },
})