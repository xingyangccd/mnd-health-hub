<template>
  <div v-if="isAuth(['ROOT', 'ORDER:SELECT'])">
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
      <el-form-item prop="packageCode">
        <el-input
          v-model="dataForm.packageCode"
          placeholder="套餐编号"
          maxlength="20"
          class="input"
          clearable
        />
      </el-form-item>
      <el-form-item prop="keyword">
        <el-input
          v-model="dataForm.keyword"
          placeholder="套餐名称"
          class="keyword"
          maxlength="50"
          clearable
        />
      </el-form-item>
      <el-form-item prop="phone">
        <el-input
          v-model="dataForm.phone"
          placeholder="电话号码"
          maxlength="11"
          class="input"
          clearable
        />
      </el-form-item>
      <el-form-item class="range">
        <el-date-picker
          v-model="dataForm.dateRange"
          type="daterange"
          range-separator="~"
          start-placeholder="起始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="dataForm.orderStatus"
          class="input"
          placeholder="订单状态"
          clearable
        >
          <el-option label="未付款" value="1" />
          <el-option label="已关闭" value="2" />
          <el-option label="已付款" value="3" />
          <el-option label="已退款" value="4" />
          <el-option label="已预约" value="5" />
          <el-option label="已结束" value="6" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchHandle()">查询</el-button>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          v-if="isAuth(['ROOT', 'ORDER:UPDATE'])"
          :icon="Refresh"
          @click="checkPaymentResultHandle()"
          >同步付款结果</el-button
        >
      </el-form-item>
    </el-form>

    <el-table
      :data="data.dataList"
      :header-cell-style="{ background: '#f5f7fa' }"
      border
      v-loading="data.loading"
      @selection-change="selectionChangeHandle"
      @expand-change="expand"
      :row-key="data.getRowKeys"
      :expand-row-keys="data.expands"
    >
      <el-table-column type="expand">
        <template #default="scope">
          <div class="content-container">
            <div class="left-panel">
              <el-card class="box-card" shadow="never">
                <div class="info">
                  <div class="left">
                    <el-avatar
                      :size="57"
                      shape="square"
                      :src="scope.row.photoUrl"
                    >
                      <el-icon size="35"><UserFilled /></el-icon>
                    </el-avatar>
                  </div>
                  <div class="right">
                    <h4 class="customer-name">{{ scope.row.customerName }}</h4>
                    <p class="customer-desc">
                      <el-icon class="icon"><User /></el-icon>
                      <span class="value">{{ scope.row.gender }}</span>
                      <el-icon class="icon"><Phone /></el-icon>
                      <span class="value">{{ scope.row.phone }}</span>
                      <el-icon class="icon"><Calendar /></el-icon>
                      <span class="value">{{ scope.row.registerTime }}</span>
                    </p>
                  </div>
                </div>
                <el-divider />
                <el-row :gutter="16">
                  <el-col :span="6">
                    <div class="statistic-card">
                      <el-statistic
                        :value="scope.row.quantity - scope.row.num"
                        suffix="个"
                      >
                        <template #title>
                          <div class="title">可预约体检</div>
                        </template>
                      </el-statistic>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="statistic-card">
                      <el-statistic :value="scope.row.num" suffix="个">
                        <template #title>
                          <div class="title">已预约体检</div>
                        </template>
                      </el-statistic>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="statistic-card">
                      <el-statistic :value="scope.row.quantity" suffix="个">
                        <template #title>
                          <div class="title">总计数量</div>
                        </template>
                      </el-statistic>
                    </div>
                  </el-col>
                </el-row>
              </el-card>
              <el-descriptions :column="1" class="order-code" border>
                <el-descriptions-item label="订单编号：" label-align="center">
                  {{ scope.row.outTradeNo }}
                </el-descriptions-item>
                <el-descriptions-item label="付款编号：" label-align="center">
                  {{
                    scope.row.transactionId == null
                      ? "无"
                      : scope.row.transactionId
                  }}
                </el-descriptions-item>
                <el-descriptions-item label="退款编号：" label-align="center">
                  {{
                    scope.row.outRefundNo == null ? "无" : scope.row.outRefundNo
                  }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <div class="right-panel">
              <el-table
                :data="data.appointment"
                :header-cell-style="{ background: '#f5f7fa' }"
                height="350"
                border
              >
                <el-table-column
                  label="序号"
                  type="index"
                  label-align="center"
                  align="center"
                  min-width="90"
                >
                  <template #default="scope">
                    <span>{{ scope.$index + 1 }}</span>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="patientName"
                  label="体检人"
                  label-align="center"
                  align="center"
                  min-width="180"
                />
                <el-table-column
                  prop="gender"
                  label="性别"
                  label-align="center"
                  align="center"
                  min-width="120"
                />
                <el-table-column
                  prop="age"
                  label="年龄"
                  label-align="center"
                  align="center"
                  min-width="120"
                />
                <el-table-column
                  prop="phone"
                  label="电话"
                  label-align="center"
                  align="center"
                  min-width="180"
                />
                <el-table-column
                  prop="appointmentDate"
                  label="体检日"
                  label-align="center"
                  align="center"
                  min-width="180"
                />
                <el-table-column
                  prop="status"
                  label="状态"
                  label-align="center"
                  align="center"
                  min-width="130"
                />
              </el-table>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        type="selection"
        header-align="center"
        align="center"
        width="50"
        :selectable="selectable"
      />
      <el-table-column
        type="index"
        header-align="center"
        align="center"
        width="100"
        label="序号"
      >
        <template #default="scope">
          <span>{{
            (data.pageIndex - 1) * data.pageSize + scope.$index + 1
          }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="goodsTitle"
        header-align="left"
        align="left"
        min-width="220"
        label="套餐名称"
      />
      <el-table-column
        header-align="center"
        align="center"
        min-width="80"
        label="价格"
      >
        <template #default="scope">
          <span>￥{{ scope.row.goodsPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="quantity"
        header-align="center"
        align="center"
        min-width="100"
        label="数量"
      />
      <el-table-column
        header-align="center"
        align="center"
        min-width="100"
        label="总计"
      >
        <template #default="scope">
          <span>￥{{ scope.row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="orderStatus"
        header-align="center"
        align="center"
        min-width="100"
        label="状态"
      />
      <el-table-column
        prop="createTime"
        header-align="center"
        align="center"
        min-width="100"
        label="下单时间"
      />
      <el-table-column
        prop="refundTime"
        header-align="center"
        align="center"
        min-width="100"
        label="退款时间"
      />
      <el-table-column
        header-align="center"
        align="center"
        width="200"
        label="操作"
      >
        <template #default="scope">
          <el-button
            type="text"
            v-if="isAuth(['ROOT', 'ORDER:SELECT'])"
            @click="viewHandle(scope.row.snapshotId)"
          >
            预览
          </el-button>
          <el-button
            type="text"
            v-if="isAuth(['ROOT'])"
            :disabled="scope.row.orderStatus != '已关闭'"
            @click="deleteHandle(scope.row.orderId)"
          >
            删除
          </el-button>
          <el-button
            type="text"
            v-if="isAuth(['ROOT', 'ORDER:UPDATE'])"
            :disabled="scope.row.orderStatus != '已付款'"
            @click="updateHandle(scope.row.outTradeNo)"
          >
            线下退款
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"
      :current-page="data.pageIndex"
      :page-sizes="[10, 20, 50]"
      :page-size="data.pageSize"
      :total="data.totalCount"
      layout="total, sizes, prev, pager, next, jumper"
    >
    </el-pagination>
  </div>
</template>

<script lang="ts" setup>
import { inject, reactive } from "vue";
import {
  Refresh,
  UserFilled,
  User,
  Phone,
  Calendar,
} from "@element-plus/icons-vue";
import { isAuth } from "@/utils/auth";
import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import { ref } from "vue";
import router from "@/router";

const dataForm = reactive({
  packageCode: null,
  keyword: null,
  phone: null,
  dateRange: [],
  orderStatus: null,
});

const dataRule = reactive({
  packageCode: [
    { min: 6, message: "编号不能少于6个字符" },
    { pattern: "^[a-zA-Z0-9]{6,20}$", message: "编号格式错误" },
  ],
  keyword: [
    { pattern: "^[a-zA-Z0-9\u4e00-\u9fa5]{1,50}$", message: "名称格式错误" },
  ],
  phone: [{ pattern: "^1[1-9]\\d{9}$", message: "电话号码格式错误" }],
});

const form = ref();
const searchHandle = async () => {
  try {
    await form.value.validate();
    // 验证通过会执行这里的代码
    data.pageIndex = 1;
    loadPageList();
  } catch {
    // 静默处理
  }
};

const checkPaymentResultHandle = async () => {
  // 从响应式对象data中获取selections，它是当前已选中的复选框对应的行数组。
  const { selections } = data;

  if (!selections.length) {
    ElMessage.warning("请选择要同步的数据");
    return;
  }

  try {
    // 获取所有的订单流水号
    const outTradeNos = selections.map((item: any) => item.outTradeNo);

    const response = await axios.post(
      "/api/mis/order/sync-results",
      { outTradeNos },
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );

    if (response.data.code === 200) {
      // 获取更新的行数
      const rows = response.data.data.rows;
      if (rows > 0) {
        ElMessage.success({
          message: `同步了${rows}个订单`,
          duration: 1200,
          onClose: () => loadPageList(),
        });
      } else {
        ElMessage.warning("您选择这些订单都没有付款");
      }
    } else {
      ElMessage.error("同步失败");
    }
  } catch (e) {
    console.log("同步失败：", e);
    ElMessage.error("网络异常，稍后再试");
  }
};

const data = reactive({
  dataList: [],
  pageIndex: 1,
  pageSize: 10,
  totalCount: 0,
  loading: false,
  selections: [],
  expands: [] as any[],
  getRowKeys(row: any) {
    return row.orderId;
    //return 10;
  },
  appointment: [],
});

const selectionChangeHandle = (values: any) => {
  // 被选中行的数组赋值给响应式对象 data.selections
  data.selections = values;
};

// 在折叠面板状态发生变化时执行这个回调函数。
// 展开 --> 折叠，折叠 --> 展开，这个函数都会执行。
// 这个函数调用的时候，EP组件库会自动传过来两个参数。
const expand = async (row: any, expandedRows: any) => {
  // 第一个参数：row：用户当前点击的行的行对象数据。
  // 第二个参数：expandedRows（数组）：存储的是当前展开行的行对象数组。
  //console.log(row);
  //console.log(expandedRows);

  // 编写一行代码，这一行代码直接控制折叠面板中永远只能打开1个。
  data.expands = data.expands.includes(row.orderId) ? [] : [row.orderId];

  if (!data.expands.length) {
    // 这是一个折叠的动作。
    // 清空响应式数据
    data.appointment = [];
    return;
  }

  // 执行到这里说明当前是展开动作。发送ajax请求加载数据。
  try {
    const response = await axios.get("/api/mis/appointment", {
      params: { orderId: row.orderId },
      headers: { satoken: localStorage.getItem("token") },
    });
    const list = response.data.data?.result || [];

    const statusMap: Record<string, string> = {
      "1": "未签到",
      "2": "已签到",
      "3": "已完成",
      "4": "已取消",
    };
    for (let one of list) {
      one.status = statusMap[String(one.status)];
    }

    data.appointment = list;
  } catch (e) {
    console.log("加载预约记录失败:", e);
    // 清空数据
    data.appointment = [];
  }
};

// 只有订单状态是1(未付款)和2(已关闭)的才可以被选中。
const selectable = (row: any) => ["未付款", "已关闭"].includes(row.orderStatus);

const viewHandle = (snapshotId: string) => {
  router.push({
    name: "FrontGoodsSnapshot",
    params: {
      id: snapshotId,
      mode: "mis",
    },
  });
};

const deleteHandle = async (orderId: number) => {
  try {
    await ElMessageBox.confirm("您确定要删除订单记录吗？", "提示信息", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const response = await axios.delete(`/api/mis/order/${orderId}`, {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    if (response.data.code === 200) {
      ElMessage.success({
        message: "删除成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.error({
        message: "删除失败",
        duration: 1200,
      });
    }
  } catch (e) {
    // 用户点击了取消以及报错都会走到这里。
    if (e !== "cancel") {
      ElMessage.error("网络异常，稍后再试");
    }
  }
};

const updateHandle = async (outTradeNo: number) => {
  try {
    await ElMessageBox.confirm("您确定要执行线下退款吗？", "提示信息", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const response = await axios.post(
      "/api/mis/order/refund/offline",
      { outTradeNo },
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success({
        message: "退款成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.error({
        message: response.data.msg || "退款失败",
        duration: 1200,
      });
    }
  } catch (e) {
    // 用户可能点击了取消，也可能程序出错了。
    if (e !== "cancel") {
      // 其他错误（不是用户点击取消）
      ElMessage.error("网络异常，稍后再试");
      console.log(e);
    }
  }
};

const sizeChangeHandle = (pageSize: number) => {
  data.pageSize = pageSize;
  data.pageIndex = 1;
  loadPageList();
};
const currentChangeHandle = (pageNo: number) => {
  data.pageIndex = pageNo;
  loadPageList();
};

const minioUrl = inject("minioUrl");
// 封装一个加载分页数据的函数
const loadPageList = async () => {
  data.loading = true;

  // 将用户填写的日期解构出来
  const [startDate, endDate] = dataForm.dateRange || [];

  // 发送ajax请求，查询分页数据
  try {
    const response = await axios.get("/api/mis/order/list", {
      params: {
        packageCode: dataForm.packageCode,
        keyword: dataForm.keyword,
        phone: dataForm.phone,
        orderStatus: dataForm.orderStatus,
        startDate: startDate,
        endDate: endDate,
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
      },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    // 获取响应的分页数据
    const pageResult = response.data.data.pageResult;

    // 取出具体的数据列表
    const list = pageResult.records;

    const orderStatusMap: Record<string, string> = {
      "1": "未付款",
      "2": "已关闭",
      "3": "已付款",
      "4": "已退款",
      "5": "已预约",
      "6": "已结束",
    };

    for (let one of list) {
      one.orderStatus = orderStatusMap[String(one.orderStatus)];
      one.photoUrl = `${minioUrl}/${one.photoUrl}`;
    }

    // 回填数据
    data.dataList = list;
    // 设置总记录条数
    data.totalCount = pageResult.total;
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  } finally {
    data.loading = false;
  }
};

// 页面加载时就调用。
loadPageList();
</script>

<style lang="less" scoped>
@import url("order.less");
</style>
