<template>
  <!-- 整个页面，设置背景图片 -->
  <div class="page">
    <!-- 登录面板 -->
    <div class="panel">
      <!-- 左边一个小div -->
      <div class="left">
        <!-- 上面一个图片 -->
        <img src="../../assets/login/logo.png" class="logo" />
        <!-- 下面一个图片 -->
        <img src="../../assets/login/big.png" class="big" />
      </div>
      <!-- 右边一个小div -->
      <div class="right">
        <!-- 显示标题字和版本号的div -->
        <div class="title-container">
          <h2>美年大健康体检系统</h2>
          <span>V1.0</span>
        </div>
        <!-- 登录表单控件div -->
        <div>
          <div class="row">
            <el-input
              v-model="loginInfo.username"
              placeholder="用户名"
              :prefix-icon="User"
              size="large"
              clearable
            ></el-input>
          </div>
          <div class="row">
            <el-input
              type="password"
              v-model="loginInfo.password"
              placeholder="密码"
              :prefix-icon="Lock"
              size="large"
              clearable
            ></el-input>
          </div>
          <div class="row">
            <el-button type="primary" class="btn" size="large" @click="login">
              登录系统
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { getCurrentInstance, reactive } from "vue";
import { User, Lock } from "@element-plus/icons-vue";

const { proxy } = getCurrentInstance()!;

// 登录数据
const loginInfo = reactive({
  username: "",
  password: "",
});

// 导入之前封装的函数。
import { isUsername, isPassword } from "@/utils/validate";
import { ElMessage } from "element-plus";
import axios from "axios";
import router from "@/router";

const login = async () => {
  // 思路
  // 1. 校验用户名
  // 2. 校验密码
  // 3. 当用户名和密码都合法，则发送ajax post请求登录
  // 4. 登录成功：提示登录成功的信息，将token和permissions存储到localStorage当中
  // 5. 登录失败：提示登录失败的信息
  // 6. 发送ajax post请求，是一个promise请求，如果请求发送失败，要提示：网络问题
  const username = loginInfo.username;
  const password = loginInfo.password;
  if (!isUsername(username)) {
    ElMessage({
      type: "error",
      message: "用户名格式错误",
      duration: 1200,
    });
  } else if (!isPassword(password)) {
    ElMessage({
      type: "error",
      message: "密码格式错误",
      duration: 1200,
    });
  } else {
    try {
      const response = await axios.post("/api/mis/user/login", {
        username,
        password,
      });
      const result = response.data;
      if (result.code === 200) {
        // token是字符串
        const token = result.data.token;
        // permissions从后端返回的是一个对象
        // localStorage只能存储字符串，因此需要将对象转换成json格式的字符串
        const permissions = JSON.stringify(result.data.permissions);
        localStorage.setItem("token", token);
        localStorage.setItem("permissions", permissions);
        router.push({ name: "MisHome" });
      } else {
        ElMessage({
          type: "error",
          message: "登录失败",
          duration: 1200,
        });
      }
    } catch {
      ElMessage.error("网络异常，稍后再试");
    }
  }
};
</script>

<style lang="less" scoped>
@import url("login.less");
</style>
