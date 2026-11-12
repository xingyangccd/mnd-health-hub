import { createApp } from "vue";
import App from "./App.vue";
// 导入路由器
import router from "./router/index.ts";
// 引入 Element Plus
import ElementPlus from "element-plus";
// 引入 Element Plus 的基础样式
import "element-plus/dist/index.css";
// 引入中文语言包（用于日历、分页等组件）
import zhCn from "element-plus/es/locale/lang/zh-cn";
// 新增内容：导入SVG依赖库，可以在页面上显示SVG图片
import "virtual:svg-icons-register";

// 创建app
const app = createApp(App);

// 使用路由器
app.use(router);

// 使用 Element Plus，并配置中文语言
app.use(ElementPlus, { locale: zhCn });

// 编写一个全局函数：添加页面中元素的守卫
// 参数permissions是：['ROOT', 'USER:DELETE']，只要用户有这个数组当中的任何一个权限，就有权限。
// app.config.globalProperties.isAuth = function(permissions: string[]): boolean{
//     if(!permissions || permissions.length === 0) {
//         // 没有要求任何权限，默认无权限
//         return false;
//     }

//     // permissions 不是 null，也不是 undefined，并且长度是 > 0
//     // 从localStorage中获取的是一个json字符串。
//     const permissionsJson = localStorage.getItem('permissions');
//     if(!permissionsJson) return false;

//     // permissionsJson不为null，不为undefined，并且字符串长度是 > 0的。
//     const localPermissions = JSON.parse(permissionsJson)
//     if(!Array.isArray(localPermissions)) return false;

//     // 只要数组 localPermissions 中有一个 permissions数组中的一个元素，则代表用户权限。
//     // 检查数组中是否至少有一个元素满足您指定的条件，满足返回 true，否则返回 false
//     return permissions.some(need => localPermissions.includes(need))
// }

// 提供一个全局的变量。
const minioUrl = "http://192.168.48.201:9000/mnd-health";
//const minioUrl = "http://localhost:9000/mnd-health";
//const minioUrl = "http://s2.gnip.vip:11693/mnd-health";
app.provide("minioUrl", minioUrl);

// 让vue项目使用websocket插件
// 配置WebSocket
import VueNativeSockModule from "vue-native-websocket-vue3";
const VueNativeSock = (VueNativeSockModule as any).default;

app.use(VueNativeSock, "ws://localhost:8080/api/websocket/push/message", {
  //收发消息自动按照json格式解析和序列化
  format: "json",
  // 如果WebSocket连接长时间不收发请求，会被服务端切断连接，下面的设置可以自动重连
  reconnection: true,
});

// 挂载
app.mount("#app");
