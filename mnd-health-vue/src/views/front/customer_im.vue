<template>
  <UIKitProvider language="zh-CN" theme="light">
    <div v-if="isLoggedIn" class="chat-page">
      <ChatLayout />
    </div>
    <div v-else class="loading">加载中...</div>
  </UIKitProvider>
</template>

<script setup lang="ts">
import { UIKitProvider } from "@tencentcloud/chat-uikit-vue3";
import { ref, onMounted } from "vue";
import { useLoginState } from "@tencentcloud/chat-uikit-vue3";
import ChatLayout from "@/components/ChatLayout.vue";
import axios from "axios";

const { login } = useLoginState();
const isLoggedIn = ref(false);

onMounted(async () => {
  try {
    // 调用后端接口获取 IM 登录凭证
    const response = await axios.get("/api/front/customer/im/account", {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    const { sdkAppId, account, userSig } = response.data.data.result;

    await login({
      sdkAppId: sdkAppId,
      userId: account,
      userSig: userSig,
    });

    /* await login({
      sdkAppId: 1600159810,
      userId: "customer_1",
      userSig:
        "eJyrVgrxCdYrSy1SslIy0jNQ0gHzM1NS80oy0zLBwsmlxSX5ualF8YZQ2eKU7MSCgswUJStDMwMDQ1NLC0MDiExqRUFmUSpQ3NTU1MjAACpakpkLEjO3sACqtzA2h5qSmQ40PCwgyiQkwN-PMMwvSDs-xSs0KjUyRj*rMNHCPCisyNM8sTAn38Inz8XdJNLRVqkWAI6qMyQ_",
    }); */
    isLoggedIn.value = true;
  } catch (error) {
    console.error("IM登录失败:", error);
  }
});
</script>

<style scoped>
.chat-page {
  width: 100%;
  height: 100vh;
}
.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  font-size: 16px;
  color: #999;
}
</style>
