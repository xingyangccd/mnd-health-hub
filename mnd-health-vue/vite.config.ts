import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";

// 新增内容：配置SVG依赖库
import path from "path";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    // 配置按需自动加载 EP 组件和 API
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    // 自动导入 EP 组件库
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    // 新增内容：引入SVG图标素材文件
    createSvgIconsPlugin({
      iconDirs: [path.resolve(process.cwd(), "src/icons/svg")],
      symbolId: "[name]",
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    // 配置前端项目所在服务器的IP地址
    host: "0.0.0.0", // localhost表示只能在本机访问，现在不限IP了。修改为0.0.0.0
    // 配置前端项目所在服务器的端口号
    port: 5173,
    allowedHosts: ["s2.gnip.vip"],

    // 配置代理
    // 所有以 /api 开头的请求路径都走vite开发服务器的代理机制。
    // 你实际的请求路径是：http://localhost:5173/api/test
    // vite开发服务器代理的结果是：https://localhost:8080/api/test
    // 这样后端就不会认为是跨域访问了。
    proxy: {
      "/api": {
        //target: "https://localhost:8080",
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false, // vite开发服务器不验证后端服务器的证书（我们HTTPS协议使用的是自签名证书，因此这里不能让Vite开发服务器验证证书，如果验证会失败）
      },
    },
  },
  // 新增配置
  css: {
    preprocessorOptions: {
      scss: {
        // 静默所有弃用警告
        quietDeps: true,
        silenceDeprecations: ["import", "global-builtin"],
      },
    },
  },
});
