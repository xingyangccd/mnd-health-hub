<template>
  <div class="customer-container">
    <!-- <el-menu :default-active="active" class="menu" @select="selectHandle">
      <el-menu-item index="FrontMine">
        <el-icon>
          <User />
        </el-icon>
        <span>账户信息</span>
      </el-menu-item>
      <el-menu-item index="FrontOrderList">
        <el-icon>
          <Tickets />
        </el-icon>
        <span>我的订单</span>
      </el-menu-item>
      <el-menu-item index="FrontAppointmentList">
        <el-icon>
          <Files />
        </el-icon>
        <span>体检预约</span>
      </el-menu-item>
      <el-menu-item index="FrontCustomerIm">
        <el-icon>
          <Collection />
        </el-icon>
        <span>客服咨询</span>
      </el-menu-item>
    </el-menu> -->

    <!-- 这种写法需要启用路由模式：:router="true" -->
    <el-menu :default-active="$route.path" class="menu" :router="true">
      <el-menu-item index="/front/customer/mine">
        <el-icon>
          <User />
        </el-icon>
        <span>账户信息</span>
      </el-menu-item>
      <el-menu-item index="/front/customer/order_list">
        <el-icon>
          <Tickets />
        </el-icon>
        <span>我的订单</span>
      </el-menu-item>
      <el-menu-item index="/front/customer/appointment_list">
        <el-icon>
          <Files />
        </el-icon>
        <span>体检预约</span>
      </el-menu-item>
      <el-menu-item index="/front/customer/customer_im">
        <el-icon>
          <Collection />
        </el-icon>
        <span>客服咨询</span>
      </el-menu-item>
    </el-menu>
    <div class="content">
      <!-- 三级路由出口 -->
      <RouterView />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, type Ref, getCurrentInstance } from "vue";
import router from "../../router/index";

// 响应式对象决定哪个被菜单被激活。
//let active = ref("FrontMine");
// 使用当前路由的name作为激活的依据：路由的name必须和index一致
//let active = ref(router.currentRoute.value.name as string);

// 点击菜单当中的任何一项，都会执行这个回调函数。
/* const selectHandle = (index: string) => {
  // 在这里完成路由的跳转。
  router.push({
    name: index,
  });
}; */
</script>

<style lang="less" scoped>
@import url("customer.less");
</style>
