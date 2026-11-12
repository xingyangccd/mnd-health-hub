<template>
  <!-- 容器div -->
  <div class="panel-container">
    <!-- IM聊天窗口div -->
    <div class="chat">
      <div v-if="isLoggedIn" class="chat-page">
        <UIKitProvider language="zh-CN" theme="light">
          <!-- 这个代码表示使用 ChatLayoutForMis 组件，并且给该组件绑定： conversation-change 事件。-->
          <ChatLayoutForMis @conversation-change="onConversationChange" />
        </UIKitProvider>
      </div>
    </div>
    <!-- 客户信息div -->
    <div class="customer-info">
      <el-card class="box-card" shadow="never">
        <div class="info">
          <div class="left">
            <el-avatar :size="57" shape="square" :src="data.customer.photoUrl">
              <el-icon size="35"><UserFilled /></el-icon>
            </el-avatar>
          </div>
          <div class="right">
            <h4 class="customer-name">{{ data.customer.customerName }}</h4>
            <p class="customer-desc">
              <el-icon class="icon"><User /></el-icon>
              <span class="value">{{ data.customer.gender }}</span>
              <el-icon class="icon"><Phone /></el-icon>
              <span class="value">{{ data.customer.phone }}</span>
              <el-icon class="icon"><Calendar /></el-icon>
              <span class="value">{{ data.customer.registerTime }}</span>
            </p>
          </div>
        </div>
        <el-divider />
        <el-row :gutter="16">
          <el-col :span="10">
            <div class="statistic-card">
              <el-statistic :value="data.statistic.totalAmount" suffix="元">
                <template #title>
                  <div class="title">累计消费金额</div>
                </template>
              </el-statistic>
            </div>
          </el-col>
          <el-col :span="7">
            <div class="statistic-card">
              <el-statistic :value="data.statistic.count" suffix="笔">
                <template #title>
                  <div class="title">有效订单数量</div>
                </template>
              </el-statistic>
            </div>
          </el-col>
          <el-col :span="7">
            <div class="statistic-card">
              <el-statistic :value="data.statistic.quantity" suffix="个">
                <template #title>
                  <div class="title">体检套餐数量</div>
                </template>
              </el-statistic>
            </div>
          </el-col>
        </el-row>
      </el-card>
      <el-table
        v-loading="data.order.loading"
        :data="data.order.dataList"
        border
        style="width: 100%"
        class="orders-table"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column
          type="index"
          header-align="center"
          align="center"
          width="80"
          label="序号"
        >
          <template #default="scope">
            <span>{{
              (data.order.pageIndex - 1) * data.order.pageSize +
              scope.$index +
              1
            }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="goodsTitle"
          label="套餐名称"
          header-align="left"
          align="left"
          min-width="200"
        />
        <el-table-column
          prop="createDate"
          label="购买日期"
          header-align="center"
          align="center"
          min-width="120"
        />
        <el-table-column
          prop="orderStatus"
          label="状态"
          header-align="center"
          align="center"
          min-width="100"
        />
      </el-table>
      <el-pagination
        @size-change="sizeChangeHandle"
        @current-change="currentChangeHandle"
        :current-page="data.order.pageIndex"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="data.order.pageSize"
        :total="data.order.totalCount"
        layout="total, sizes, prev, pager, next, jumper"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script setup lang="ts">
import { UIKitProvider } from "@tencentcloud/chat-uikit-vue3";
import { ref, onMounted, reactive, inject } from "vue";
import { useLoginState } from "@tencentcloud/chat-uikit-vue3";
import ChatLayoutForMis from "@/components/ChatLayoutForMis.vue";
import axios from "axios";
import { User, Phone, Calendar, UserFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const { login } = useLoginState();
const isLoggedIn = ref(false);

onMounted(async () => {
  try {
    // 调用后端接口获取 IM 登录凭证
    const response = await axios.get("/api/mis/customer/im/account", {
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
    isLoggedIn.value = true;
  } catch (error) {
    console.error("IM登录失败:", error);
  }
});

const onConversationChange = (userId: string) => {
  // userId 格式：customer_1，提取数字
  // customer_1 转换为 1（1数据库表的主键值）
  const customerId = userId.replace(/^customer_/, "");
  data.customer.id = customerId;
  // 调用加载客户信息的方法
  loadCustomerInfo();
  loadOrders();
};

// 定义响应式数据对象，用于存储客户相关信息
const data = reactive({
  // 客户基本信息
  customer: {
    id: "", // 客户ID
    customerName: "--", // 客户姓名
    gender: "--", // 客户性别
    phone: "--", // 客户电话
    photoUrl: "--", // 客户头像
    registerTime: "--", // 创建时间
  },
  // 客户统计信息
  statistic: {
    totalAmount: 0, // 总金额
    count: 0, // 总次数
    quantity: 0, // 数量
  },
  // 订单相关信息
  order: {
    dataList: [], // 订单数据列表
    pageIndex: 1, // 当前页码
    pageSize: 5, // 每页显示条数
    totalCount: 0, // 总记录数
    loading: false, // 加载状态
  },
});

const sizeChangeHandle = (pageSize: number) => {
  data.order.pageSize = pageSize;
  data.order.pageIndex = 1;
  loadOrders();
};
const currentChangeHandle = (pageNo: number) => {
  data.order.pageIndex = pageNo;
  loadOrders();
};

const minioUrl = inject("minioUrl");
const loadCustomerInfo = async () => {
  try {
    // 获取customerId
    const customerId = data.customer.id;
    // 发送ajax请求获取用户信息。
    const response = await axios.get("/api/mis/customer/summary", {
      params: { customerId },
      headers: { satoken: localStorage.getItem("token") },
    });
    if (response.data.code === 200) {
      const result = response.data.data.result;
      data.customer.customerName = result.customerName || "未填写";
      data.customer.gender = result.gender || "未填写";
      data.customer.phone = result.phone;
      data.customer.registerTime = result.registerTime;
      data.customer.photoUrl = `${minioUrl}/${result.photoUrl}`;
      data.statistic.totalAmount = result.totalAmount;
      data.statistic.count = result.totalCount;
      data.statistic.quantity = result.totalQuantity;
    } else {
      ElMessage.error(response.data.msg || "加载失败");
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
};

// 加载当前客户的订单信息
const loadOrders = async () => {
  const { customer, order } = data;
  console.log("客户的ID：", customer.id);
  order.loading = true;
  try {
    const response = await axios.get("/api/mis/order/list", {
      params: {
        pageNo: order.pageIndex,
        pageSize: order.pageSize,
        customerId: customer.id,
      },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    if (response.data.code === 200) {
      const statusMap: Record<string, string> = {
        "1": "未付款",
        "2": "已关闭",
        "3": "已付款",
        "4": "已退款",
        "5": "已预约",
        "6": "已结束",
      };

      const { records, total } = response.data.data.pageResult;

      for (let one of records) {
        one.orderStatus = statusMap[String(one.orderStatus)];
      }

      order.dataList = records;
      order.totalCount = total;
    } else {
      ElMessage.error(response.data.msg || "加载失败");
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  } finally {
    order.loading = false;
  }
};
</script>

<style scoped>
@import url("customer_im.less");
</style>
