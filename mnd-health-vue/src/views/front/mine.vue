<template>
  <div class="summary-container">
    <el-card class="box-card" shadow="never">
      <div class="info">
        <div class="left">
          <el-avatar :size="45" shape="square" :src="data.photoUrl">
            <el-icon size="25">
              <UserFilled />
            </el-icon>
          </el-avatar>
        </div>
        <div class="right">
          <div class="base">
            <span>姓名：{{ data.customerName }}</span>
            <span>性别：{{ data.gender }}</span>
            <span>电话号码：{{ data.phone }}</span>
            <div class="operate" @click="updateHandle">
              <el-icon :size="18">
                <Edit />
              </el-icon>
              <div>修改资料</div>
            </div>
          </div>
          <p>注册时间：{{ data.registerTime }}</p>
        </div>
      </div>
      <el-divider />
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="statistic-card">
            <el-statistic :value="data.amount" suffix="元">
              <template #title>
                <div class="title">累计消费金额</div>
              </template>
            </el-statistic>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="statistic-card">
            <el-statistic :value="data.count" suffix="笔">
              <template #title>
                <div class="title">有效订单数量</div>
              </template>
            </el-statistic>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="statistic-card">
            <el-statistic :value="data.number" suffix="个">
              <template #title>
                <div class="title">体检套餐数量</div>
              </template>
            </el-statistic>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>

  <el-dialog
    title="修改资料"
    :close-on-click-modal="false"
    v-model="dialog.visible"
    width="420px"
  >
    <el-form
      :model="dialog.dataForm"
      ref="dialogForm"
      :rules="dialog.dataRule"
      label-width="60px"
    >
      <el-form-item label="姓名" prop="customerName">
        <el-input
          v-model="dialog.dataForm.customerName"
          placeholder="输入姓名"
          maxlength="10"
          size="default"
          class="input"
          clearable
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select
          v-model="dialog.dataForm.gender"
          placeholder="选择性别"
          size="default"
          clearable
          style="width: 100%"
        >
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="电话" prop="phone">
        <el-input
          v-model="dialog.dataForm.phone"
          placeholder="输入电话"
          maxlength="11"
          size="default"
          class="input"
          clearable
          style="width: 100%"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button size="default" @click="dialog.visible = false">
          取消
        </el-button>
        <el-button type="primary" size="default" @click="dataFormSubmit">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref, type Ref, getCurrentInstance, nextTick } from "vue";
import router from "../../router/index";
import { UserFilled } from "@element-plus/icons-vue";
import axios from "axios";
import { ElMessage } from "element-plus";

// 手动定义类型，解决TS类型检查问题。
interface CustomerInfo {
  customerName: null | string;
  gender: null | string;
  phone: null | string;
  photoUrl: string | undefined;
  registerTime: null | string;
  count: number;
  number: number;
  amount: number;
}

// 清空数据
const data: CustomerInfo = reactive({
  customerName: null,
  gender: null,
  phone: null,
  photoUrl: undefined,
  registerTime: null,
  count: 0,
  number: 0,
  amount: 0,
});

const dialogForm = ref();

const updateHandle = async () => {
  // 显示窗口
  dialog.visible = true;
  // 等待窗口渲染完毕
  await nextTick();
  // 重置表单（清空错误的校验信息）
  dialogForm.value.resetFields();
  // 获取数据（不需要查询数据库，直接从响应式对象中获取用户信息就行）
  const { customerName, gender, phone } = data;
  dialog.dataForm = {
    customerName: customerName === "未填写" ? null : customerName,
    gender: gender === "未填写" ? null : gender,
    phone: phone || "",
  };
};

const loadCustomerInfo = async () => {
  // 获取token
  const token = localStorage.getItem("token");
  if (!token) {
    return;
  }

  try {
    const { data: responseData } = await axios.get(
      "/api/front/customer/summary",
      {
        headers: {
          satoken: token,
        },
      },
    );

    const { result } = responseData.data;
    Object.assign(data, {
      customerName: result.customerName || "未填写",
      gender: result.gender || "未填写",
      phone: result.phone || "",
      photoUrl: result.photoUrl || "",
      registerTime: result.registerTime || "",
      // ?? 空值合并运算符：result.totalCount 不是null也不是undefined，就用它自己的值，如果是null或者undefined就用0
      count: result.totalCount ?? 0, // 订单总量
      number: result.totalQuantity ?? 0, // 套餐总个数
      amount: result.totalAmount ?? 0, // 消费总金额
    });
  } catch (error) {
    console.log("加载用户信息失败", error);
  }
};

// 页面打开就加载用户信息
loadCustomerInfo();

// -----------------------------------------------------------

interface DataForm {
  customerName: string | null;
  gender: string | null;
  phone: string | null;
}

const dialog = reactive({
  // 控制可见性
  visible: false,
  // 表单数据
  dataForm: {
    customerName: null,
    gender: null,
    phone: null,
  } as DataForm,
  // 校验规则
  dataRule: {
    customerName: [
      { pattern: "^[\u4e00-\u9fa5]{2,10}$", message: "姓名格式错误" },
    ],
    phone: [
      { required: true, message: "电话不能为空" },
      { pattern: "^1[1-9]\\d{9}$", message: "电话格式错误" },
    ],
  },
});

const dataFormSubmit = async () => {
  try {
    // 做一个表单的校验
    await dialogForm.value.validate();

    // 获取token
    const token = localStorage.getItem("token");
    if (!token) return;

    const { data: responseData } = await axios.put(
      "/api/front/customer",
      dialog.dataForm,
      {
        headers: {
          satoken: token,
        },
      },
    );

    if (responseData.code === 200) {
      dialog.visible = false;
      ElMessage.success({
        message: responseData.msg || "个人资料更新成功",
        duration: 1200,
        onClose: () => loadCustomerInfo(),
      });
    } else {
      ElMessage.error(responseData.msg || "更新失败，请重试");
    }
  } catch {
    ElMessage.error("网络异常，稍后再试");
  }
};
</script>

<style lang="less" scoped>
@import url("mine.less");
</style>
