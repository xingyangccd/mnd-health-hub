<template>
  <!-- 编写业务端框架页面的导航部分 -->
  <div class="container">
    <!-- 头部 -->
    <header>
      <!-- logo部分 -->
      <div class="logo-container" @click="router.push({ name: 'FrontIndex' })">
        <img src="../../assets/front/index/logo.png" class="logo" />
        <div>
          <h3>美年大健康</h3>
          <p>www.mnhealth.com</p>
        </div>
      </div>
      <!-- 搜索框部分 -->
      <div class="search-container">
        <div>
          <!-- 搜索框的div -->
          <el-input
            v-model="header.keyword"
            size="large"
            maxlength="50"
            placeholder="请输入套餐名或编号"
            class="keyword-input"
            clearable
          >
            <template #append>
              <el-button @click="searchGoods">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div>
          <!-- tag标签的div：effect="light" 设置主题是高亮-->
          <el-tag
            v-for="item in header.tags"
            :key="item.label"
            :type="item.type"
            class="tag"
            round
            effect="light"
            @click="tagHandle(item.label)"
          >
            {{ item.label }}
          </el-tag>
        </div>
      </div>
      <!-- 登录/注册部分 -->
      <div class="actions-container">
        <el-button
          type="primary"
          size="large"
          v-if="dialog.status == 'logout'"
          @click="showDialog"
        >
          登录/注册
        </el-button>
        <div
          class="btn"
          v-if="dialog.status == 'login'"
          @click="router.push({ name: 'FrontMine' })"
        >
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </div>
        <div class="btn" v-if="dialog.status == 'login'" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出系统</span>
        </div>
      </div>
    </header>

    <!-- 二级路由出口 -->
    <!-- 
        :key是组件的唯一标识
        只要 :key的值不同，该组件就会重新加载
        这样可以保证 Vue 组件的生命周期钩子会重新执行
     -->
    <RouterView :key="router.currentRoute.value.fullPath"></RouterView>
  </div>

  <!-- 页脚 -->
  <footer>
    <!-- 灰色区域 -->
    <div class="about-us-container">
      <ul class="list">
        <li class="item">
          <img src="../../assets/front/index/d1.svg" />
          <div>
            <h3>全国连锁</h3>
            <p>覆盖全国主要城市的专业体检网络，随时随地享受便捷服务</p>
          </div>
        </li>
        <li class="item">
          <img src="../../assets/front/index/d2.svg" />
          <div>
            <h3>智能管理</h3>
            <p>数字化健康档案系统，为您提供个性化的健康管理方案</p>
          </div>
        </li>
        <li class="item">
          <img src="../../assets/front/index/d3.svg" />
          <div>
            <h3>权威专家</h3>
            <p>由三甲医院资深医师组成的专业团队，确保检查结果准确可靠</p>
          </div>
        </li>
        <li class="item">
          <img src="../../assets/front/index/d4.svg" />
          <div>
            <h3>贴心服务</h3>
            <p>一对一健康顾问，报告专业解读，后续健康指导全程陪伴</p>
          </div>
        </li>
      </ul>
    </div>

    <!-- 蓝色区域 -->
    <div class="contact-us-container">
      <ul class="list">
        <!-- 美年大健康和电话 -->
        <li class="left">
          <!-- 美年大健康图片 -->
          <img src="../../assets/front/index/service.png" />
          <!-- 电话 -->
          <p class="tel">400-6666-8888</p>
        </li>
        <!-- 了解美年 -->
        <li>
          <ul class="link-list">
            <li><a href="">了解美年</a></li>
            <li><a href="">公司概况</a></li>
            <li><a href="">成长轨迹</a></li>
            <li><a href="">健康生态体系</a></li>
            <li><a href="">价值理念</a></li>
          </ul>
        </li>
        <!-- 健康资讯 -->
        <li>
          <ul class="link-list">
            <li><a href="">健康资讯</a></li>
            <li><a href="">最新动态</a></li>
            <li><a href="">媒体聚焦</a></li>
            <li><a href="">党建风采</a></li>
          </ul>
        </li>
        <!-- 服务支持 -->
        <li>
          <ul class="link-list">
            <li><a href="">服务支持</a></li>
            <li><a href="">常见问题</a></li>
            <li><a href="">在线预约</a></li>
            <li><a href="">报告查询</a></li>
            <li><a href="">服务条款</a></li>
          </ul>
        </li>
        <!-- 合作共赢 -->
        <li>
          <ul class="link-list">
            <li><a href="">合作共赢</a></li>
            <li><a href="">合作伙伴</a></li>
            <li><a href="">加入我们</a></li>
          </ul>
        </li>
        <!-- 公众号二维码和下载体检app的二维码 -->
        <li class="right">
          <div class="erCode">
            <p>微信公众号</p>
            <img src="../../assets/front/index/erCode-1.png" />
          </div>
          <div class="erCode">
            <p>下载体检APP</p>
            <img src="../../assets/front/index/erCode-2.png" />
          </div>
        </li>
      </ul>
    </div>
  </footer>

  <el-dialog
    v-model="dialog.visible"
    title="手机快速登录"
    width="400"
    class="dialog"
  >
    <el-row>
      <el-col :span="24">
        <el-input
          v-model="dialog.phone"
          placeholder="输入手机号快捷登录"
          size="large"
          maxlength="11"
          clearable
        >
          <template #prepend>
            <el-icon><Iphone /></el-icon>
          </template>
        </el-input>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="16">
        <el-input
          v-model="dialog.code"
          placeholder="输入短信验证码"
          size="large"
          maxlength="6"
          clearable
        >
          <template #prepend>
            <el-icon><Message /></el-icon>
          </template>
        </el-input>
      </el-col>
      <el-col :span="8">
        <el-button
          size="large"
          class="receive-btn"
          type="primary"
          plain
          @click="sendSmsCode"
          :disabled="dialog.disabled"
        >
          {{ dialog.btnContent }}
        </el-button>
      </el-col>
    </el-row>
    <el-button type="primary" class="login-btn" size="large" @click="login">
      登录系统
    </el-button>
  </el-dialog>

  <!-- AI 助手悬浮球 -->
  <AiAssistant />

</template>

<script lang="ts" setup>
// 一定要导入
import AiAssistant from "@/components/AiAssistant.vue";
import router from "@/router";
import { getCurrentInstance, reactive } from "vue";
import { SwitchButton, User } from "@element-plus/icons-vue";

// 获取当前组件实例，组件实例中有proxy属性，解构拿到proxy
// 以后通过proxy访问全局属性/全局变量（isAuth函数可以使用它来访问。）
// proxy 不是当前组件实例。getCurrentInstance()函数的执行结果是当前组件实例。
// 最后的感叹号是：非空断言操作符
const { proxy } = getCurrentInstance()!;

// 定义响应式对象
const header = reactive({
  // 对应头部区域的文本框
  keyword: "",
  // 文本框下面的tag标签，以下tag标签的内容
  tags: [
    {
      label: "青少年体检",
      type: "info",
    },
    {
      label: "婚前体检",
      type: "info",
    },
    {
      label: "精英体检",
      type: "info",
    },
  ] as const, // 断言为 字面量类型
});

header.keyword = router.currentRoute.value.query.keyword as string;

import { isPhone, stringIsEmpty, isSmsCode } from "../../utils/validate";
import { Iphone, Message } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import axios from "axios";

// 定义 Dialog 类型
interface DialogState {
  visible: boolean;
  phone: string;
  code: string;
  disabled: boolean;
  btnContent: string;
  num: number;
  status: "login" | "logout";
  timer: number | undefined;
}

const dialog: DialogState = reactive({
  visible: false, // 决定登录弹窗是否可见
  phone: "", // 用户填写的手机号
  code: "", // 用户填的验证码
  disabled: false, // false表示按钮可用，true表示按钮不可用
  btnContent: "获取短信验证码", // 按钮上显示的文本
  num: 0, // 倒计时用的
  status: "logout",
  timer: undefined, // 保存定时器
});

const dataRule = reactive({
  phone: [
    { required: true, pattern: "^1[1-9]\d{9}$", message: "手机号码错误" },
  ],
});

// 发送短信验证码
const sendSmsCode = async () => {
  const phone = dialog.phone;
  if (stringIsEmpty(phone)) {
    ElMessage.error({
      message: "请填写手机号",
      duration: 1200,
    });
    return;
  }

  if (!isPhone(phone)) {
    ElMessage.error({
      message: "手机号格式错误",
      duration: 1200,
    });
    return;
  }

  // 手机号填写了，并且手机号格式合法。
  // 防止重复点击
  if (dialog.disabled) {
    // 条件成立的时候，说明在1分钟之内，已经发过验证码了。
    return;
  }

  // 发送ajax请求
  try {
    const response = await axios.post("/api/front/customer/sms-code", {
      phone,
    });
    const result = response.data.data.result;
    const msg = response.data.msg;
    if (result) {
      // 提示成功
      ElMessage.success({
        message: msg || "短信已发送",
        duration: 1200,
      });

      // 立即进入倒计时
      let remaining = 60; // 还剩60秒
      dialog.disabled = true;
      dialog.btnContent = `${remaining}秒后重新获取`;

      // 如果已经存在旧的定时器，请清除掉旧的定时器。
      if (dialog.timer) {
        clearInterval(dialog.timer);
      }

      // 重新设置一个全新的定时器
      dialog.timer = setInterval(() => {
        remaining--;
        if (remaining > 0) {
          dialog.btnContent = `${remaining}秒后重新获取`;
        } else {
          dialog.disabled = false;
          dialog.btnContent = "获取短信验证码";
          clearInterval(dialog.timer);
          dialog.timer = undefined;
        }
      }, 1000);
    } else {
      ElMessage.error({
        message: msg || "短信发送失败，请稍后再试",
        duration: 1200,
      });
    }
  } catch (error) {
    ElMessage.error({
      message: "网络异常，稍后再试",
      duration: 1200,
    });
  }
};

const login = async () => {
  // 获取用户填写的手机号和验证码
  const { phone, code } = dialog;

  if (stringIsEmpty(phone)) {
    ElMessage.error({ message: "请填写手机号", duration: 1200 });
    return;
  }
  if (!isPhone(phone)) {
    ElMessage.error({ message: "手机号格式错误", duration: 1200 });
    return;
  }
  if (stringIsEmpty(code)) {
    ElMessage.error({ message: "请填写验证码", duration: 1200 });
    return;
  }
  if (!isSmsCode(code)) {
    ElMessage.error({ message: "验证码格式错误", duration: 1200 });
    return;
  }
  // 发送ajax请求，登录
  try {
    const { data } = await axios.post("/api/front/customer/login", {
      phone,
      code,
    });

    const { code: statusCode, msg, data: responseData } = data;

    if (statusCode === 200 && responseData.result) {
      // 登录成功（将第二个对象参数中的属性值对应赋值给第一个参数dialog对象）
      Object.assign(dialog, {
        phone: null,
        code: null,
        visible: false,
        status: "login",
      });

      // 存储令牌
      localStorage.setItem("token", responseData.token);

      // 成功的提示
      ElMessage.success({
        message: msg || "登录成功",
        duration: 1200,
      });
    } else {
      // 登录失败
      ElMessage.error({
        message: msg || "登录失败，请重试",
        duration: 1200,
      });
    }
  } catch {
    ElMessage.error("网络异常，稍后再试");
  }
};

// 显示弹窗
const showDialog = () => {
  dialog.visible = true;
};

const logout = async () => {
  try {
    const token = localStorage.getItem("token");
    if (!token) {
      dialog.status = "logout";
      // 路由跳转：跳转到首页面
      router.push({ name: "FrontIndex" });
      return;
    }

    // 有token，发送ajax delete请求，完成客户登出。
    const response = await axios.delete("/api/front/customer/auth/session", {
      headers: {
        satoken: token,
      },
    });

    if (response.data.code === 200) {
      // 清除token
      localStorage.removeItem("token");
      dialog.status = "logout";
      router.push({ name: "FrontIndex" });

      ElMessage.success({
        message: "已退出系统",
        duration: 1200,
      });
    }
  } catch {
    ElMessage.error({
      message: "退出失败，请稍后再试",
      duration: 1200,
    });
  }
};

// 检查用户是否已经登录
const checkLogin = async () => {
  const token = localStorage.getItem("token");
  if (!token) {
    dialog.status = "logout";
    return;
  }

  // 发送ajax get请求，检查用户是否已经登录
  try {
    const response = await axios.get("/api/front/customer/auth/status", {
      headers: {
        satoken: token,
      },
    });
    const respData = response.data;
    // ?? 空值合并运算符
    // 当 respData.data?.result 的结果不是null也不是undefined的时候，就使用它的值。
    // 当 respData.data?.result 的结果是null或者undefined的时候，就使用false。
    const isLogin = respData.data?.result ?? false;
    dialog.status = isLogin ? "login" : "logout";

    if (!isLogin) {
      // token是无效的。
      //localStorage.removeItem("token");
    }
  } catch {
    dialog.status = "logout";
    localStorage.removeItem("token");
  }
};

// 页面打开的时候就调用方法，检查用户是否已经登录
checkLogin();

const tagHandle = (val: any) => {
  header.keyword = val;
};

const searchGoods = () => {
  router.push({
    name: "FrontGoodsList",
    // 传递查询参数
    query: {
      keyword: header.keyword,
      // 为了避免走缓存，添加时间戳
      _: new Date().getTime(),
    },
  });
};
</script>

<style lang="less" scoped>
// 在单独的less文件中定义样式
@import url("main.less");
</style>
