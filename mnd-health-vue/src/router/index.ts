import { createRouter, createWebHistory } from "vue-router";

// 创建路由器
const router = createRouter({
  // 指定路由模式：基于history模式，不是基于hash模式（#）
  history: createWebHistory(),
  // 配置多个路由
  // 思考一个问题？什么时候需要配置二级路由？
  // 标准：当这个页面是一个独立的页面，没有任何嵌套，那么一级路由就行了。
  // 当这个页面中嵌套一个页面的话，需要启用二级/三级/多级路由。
  routes: [
    // 中括号是数组，数组中的每个 {} 是一个路由对象，这个路由对象属于一级路由。

    // 一级路由
    // 业务端
    {
      path: "/front",
      name: "Front",
      component: () => import("@/views/front/main.vue"),
      redirect: "/front/index",
      children: [
        // 二级路由
        {
          path: "index",
          name: "FrontIndex",
          component: () => import("@/views/front/index.vue"),
        },
        {
          path: "goods/:id",
          name: "FrontGoods",
          component: () => import("@/views/front/goods.vue"),
        },
        {
          path: "goods_list",
          name: "FrontGoodsList",
          component: () => import("@/views/front/goods_list.vue"),
        },
        {
          path: "customer",
          name: "FrontCustomer",
          component: () => import("@/views/front/customer.vue"),
          children: [
            {
              path: "mine",
              name: "FrontMine",
              component: () => import("@/views/front/mine.vue"),
            },
            {
              path: "order_list",
              name: "FrontOrderList",
              component: () => import("@/views/front/order_list.vue"),
            },
            {
              path: "customer_im",
              name: "FrontCustomerIM",
              component: () => import("@/views/front/customer_im.vue"),
            },
            {
              path: "appointment_list",
              name: "FrontAppointmentList",
              component: () => import("@/views/front/appointment_list.vue"),
            },
          ],
        },
        {
          path: "goods_snapshot/:id/:mode",
          name: "FrontGoodsSnapshot",
          component: () => import("@/views/front/goods_snapshot.vue"),
        },
      ],
    },

    // 一级路由
    // mis端
    {
      path: "/mis",
      name: "Mis",
      component: () => import("@/views/mis/main.vue"),
      redirect: "/mis/home",
      // 二级路由
      children: [
        {
          path: "home",
          name: "MisHome",
          component: () => import("@/views/mis/home.vue"),
          // 在路由器上使用meta来定义路由的元数据信息。
          // 这个信息将来可以通过 route 路由对象来获取。
          meta: {
            title: "首页",
            isTab: false, // 整个系统当中只有home页不以tab页展示。
          },
        },
        {
          path: "dept",
          name: "MisDept",
          component: () => import("@/views/mis/dept.vue"),
          meta: {
            title: "部门管理",
            isTab: true, // true表示，该页面以tab页的方式展示。
          },
        },
        {
          path: "role",
          name: "MisRole",
          component: () => import("@/views/mis/role.vue"),
          meta: {
            title: "角色管理",
            isTab: true,
          },
        },
        {
          path: "user",
          name: "MisUser",
          component: () => import("@/views/mis/user.vue"),
          meta: {
            title: "用户管理",
            isTab: true,
          },
        },
        {
          path: "goods",
          name: "MisGoods",
          component: () => import("@/views/mis/goods.vue"),
          meta: {
            title: "体检套餐",
            isTab: true,
          },
        },
        {
          path: "order",
          name: "MisOrder",
          component: () => import("@/views/mis/order.vue"),
          meta: {
            title: "订单管理",
            isTab: true,
          },
        },
        {
          path: "rule",
          name: "MisRule",
          component: () => import("@/views/mis/rule.vue"),
          meta: {
            title: "促销规则",
            isTab: true,
          },
        },
        {
          path: "customer_im",
          name: "MisCustomerIm",
          component: () => import("@/views/mis/customer_im.vue"),
          meta: {
            title: "客服IM",
            isTab: true,
          },
        },
        {
          path: "appointment",
          name: "MisAppointment",
          component: () => import("@/views/mis/appointment.vue"),
          meta: {
            title: "体检预约",
            isTab: true,
          },
        },
        {
          path: "customer_checkin",
          name: "MisCustomerCheckin",
          component: () => import("@/views/mis/customer_checkin.vue"),
          meta: {
            title: "体检签到",
            isTab: true,
          },
        },
        {
          path: "doctor_checkup",
          name: "MisCheckup",
          component: () => import("@/views/mis/doctor_checkup.vue"),
          meta: {
            title: "医生检查",
            isTab: true,
          },
        },
        {
          path: "flow_regulation",
          name: "MisFlowRegulation",
          component: () => import("@/views/mis/flow_regulation.vue"),
          meta: {
            title: "人员限流",
            isTab: true,
          },
        },
        {
          path: "checkup_report",
          name: "MisCheckupReport",
          component: () => import("@/views/mis/checkup_report.vue"),
          meta: {
            title: "体检报告",
            isTab: true,
          },
        },
      ],
    },

    // mis端登录页面：是一个独立的页面，所以这个配置为一级路由即可。
    {
      path: "/mis/login",
      name: "MisLogin",
      component: () => import("@/views/mis/login.vue"),
      // 在这个数组当中配置的路由对象是二级路由
      children: [],
    },
    // 404
    {
      path: "/404",
      name: "404",
      component: () => import("@/views/404.vue"),
    },
    {
      // 捕获所有未定义的路由路径
      // /:pathMatch 定义一个路径参数，名为 pathMatch，名字随意
      // (.*) 正则表达式，匹配任意字符（除换行符外）
      // * 星号表示匹配多段路径，支持嵌套路由
      // 这个配置需要放到所有路由的最下面，兜底。
      path: "/:pathMatch(.*)*",
      // 重定向到 /404
      redirect: "/404",
    },
  ],
});

// 编写全局路由守卫（鉴权）
// to: 要跳转的目标路由对象。
// from: 当前路由对象
// next: 是一个函数，通过调用这个函数来放行（最终是否跳转，全靠这个函数）
router.beforeEach((to, from, next) => {
  // 这个回调函数类似于一个拦截器。
  // 在该回调函数中验证用户是否有这个权限，如果有权限，则放行。如果没有权限则跳转到指定页。
  // 获取token
  const token = localStorage.getItem("token") || "";
  // 获取permissions权限列表
  // localStorage 当中取出的是字符串
  // 权限列表什么样子：['ROOT', 'USER:DELETE', 'USER:UPDATE', 'ORDER:UPDATE', 'ORDER:DELETE']
  // 通过 || '' 给定一个默认值
  const permissions = localStorage.getItem("permissions") || "";

  // 判断该用户是否已经登录了
  // !! 运算符代替的是：Boolean() 函数
  //const isLoggedIn = Boolean(token) && Boolean(permissions)
  const isLoggedIn = !!token && !!permissions;
  //const isLoggedIn = token && permissions

  // 获取要跳转的路径
  const path = to.path;

  // 白名单：直接放行（/mis/login、/front/index）
  if (path === "/mis/login" || path === "/front/index") {
    return next();
  }

  // 程序能够走到这里就需要鉴权了。
  // mis端（除了登录页，其他页都需要登录）
  // 当请求路径以 /mis 开头，则认为请求是mis端发过来的。
  if (path.startsWith("/mis")) {
    // 以下大括号中的 path 是一个对象的属性名，不是我们上面path变量的变量名。
    // next() 放行
    // next({path: 'url'}) 指定跳转到某个位置
    return isLoggedIn ? next() : next({ path: "/mis/login" });
  }

  // 业务端：客户自己查看自己的详细信息/查看商品快照，是需要本人登录，其他情况都不需要登录。
  // /front/customer 查看用户详细信息的路径
  // /front/goods_snapshot 查看用户自己的商品快照的路径
  if (
    path.startsWith("/front/customer") ||
    path.startsWith("/front/goods_snapshot")
  ) {
    const isLoginFront = !!token;
    // 必须登录
    return isLoginFront ? next() : next({ path: "/front/index" });
  }

  // 其他页面，全部放行
  return next();
});

// 默认导出
export default router;
