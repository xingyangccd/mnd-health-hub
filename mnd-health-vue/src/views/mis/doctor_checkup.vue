<template>
  <div
    class="person-container"
    v-if="isAuth(['ROOT', 'CHECKUP:INSERT', 'CHECKUP:UPDATE'])"
  >
    <div class="left">
      <el-descriptions
        title="【 体检人信息 】"
        direction="vertical"
        :column="5"
        border
      >
        <template #extra>
          <div class="operate">
            <el-input
              placeholder="输入 / 扫码体检编号"
              v-model="data.dataForm.uuid"
              ref="uuid"
              class="uuid"
              maxlength="32"
              @keyup.enter.native="searchHandle"
              clearable
            />
            <el-button type="primary" @click="searchHandle">执行查询</el-button>
          </div>
        </template>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><User /></el-icon>
              姓名
            </div>
          </template>
          {{ data.customer.name }}
        </el-descriptions-item>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><Male /></el-icon>
              性别
            </div>
          </template>
          {{ data.customer.sex }}
        </el-descriptions-item>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><Calendar /></el-icon>
              年龄
            </div>
          </template>
          {{ data.customer.age }}
        </el-descriptions-item>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><Phone /></el-icon>
              电话号码
            </div>
          </template>
          {{ data.customer.tel }}
        </el-descriptions-item>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><Postcard /></el-icon>
              体检编号
            </div>
          </template>
          {{ data.customer.uuid }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="right">
      <el-descriptions
        title="【 医生信息 】"
        direction="vertical"
        :column="5"
        border
      >
        <template #extra>
          <el-button type="primary" @click="selectDeptHandle"
            >选择科室</el-button
          >
        </template>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><User /></el-icon>
              姓名
            </div>
          </template>
          {{ data.doctor.name }}
        </el-descriptions-item>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><Male /></el-icon>
              性别
            </div>
          </template>
          {{ data.doctor.sex }}
        </el-descriptions-item>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><Phone /></el-icon>
              电话号码
            </div>
          </template>
          {{ data.doctor.tel }}
        </el-descriptions-item>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><OfficeBuilding /></el-icon>
              体检科室
            </div>
          </template>
          {{ data.dataForm.place }}
        </el-descriptions-item>
        <el-descriptions-item align="center">
          <template #label>
            <div class="cell-item">
              <el-icon class="icon"><Calendar /></el-icon>
              当前日期
            </div>
          </template>
          {{ data.doctor.date }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
  <div class="checkup-container">
    <table>
      <thead>
        <tr>
          <th width="50">序号</th>
          <th width="100" align="left">体检项</th>
          <th width="300" align="left">检查结果</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(one, index) in data.dataForm.checkup">
          <td align="center">{{ index + 1 }}</td>
          <td>{{ one.item }}</td>
          <td>
            <div class="value-container">
              <el-select
                v-model="one.input.select"
                class="m-2"
                placeholder="选择模板值"
              >
                <el-option v-for="v in one.value" :label="v" :value="v" />
              </el-select>
              <el-input
                v-model="one.input.value"
                :disabled="one.input.select != '其他'"
                placeholder="输入体检值"
                class="input-value"
                clearable
              />
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="operate" v-if="data.dataForm.checkup.length > 0">
      <el-button type="danger" size="large" @click="clearCheckupHandle">
        重置数据
      </el-button>
      <el-button type="primary" size="large" @click="dataFormSubmit">
        提交体检结果
      </el-button>
    </div>
  </div>

  <el-dialog
    title="选择科室"
    v-if="isAuth(['ROOT', 'CHECKUP:INSERT', 'CHECKUP:UPDATE'])"
    :close-on-click-modal="false"
    v-model="dialog.visible"
    width="300px"
  >
    <el-form
      :model="data.dataForm"
      ref="dialogForm"
      :rules="dialog.dataRule"
      label-width="80px"
    >
      <el-form-item label="科室名称" prop="place">
        <el-select
          v-model="data.dataForm.place"
          placeholder="选择科室"
          clearable
          @change="changePlaceHandle"
        >
          <el-option label="外科" value="外科" />
          <el-option label="内科" value="内科" />
          <el-option label="眼科" value="眼科" />
          <el-option label="耳鼻喉科" value="耳鼻喉科" />
          <el-option label="口腔科" value="口腔科" />
          <el-option label="放射科" value="放射科" />
          <el-option label="妇科" value="妇科" />
          <el-option label="男科" value="男科" />
          <el-option label="一般检查室" value="一般检查室" />
          <el-option label="动脉硬化检测室" value="动脉硬化检测室" />
          <el-option label="心电图室" value="心电图室" />
          <el-option label="采血室" value="采血室" />
          <el-option label="尿常规室" value="尿常规室" />
          <el-option label="超声检查室" value="超声检查室" />
          <el-option label="骨密度检查室" value="骨密度检查室" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="closeDialog">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>
<script lang="ts" setup>
import { nextTick, onMounted, onUnmounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { dayjs } from "element-plus";
import { stringIsEmpty } from "../../utils/validate";
import { isAuth } from "@/utils/auth";
import axios from "axios";

interface CheckupItem {
  item: string;
  unit: string;
  standard: string;
  value: string[];
  input: {
    select: string | null;
    value: string | null;
  };
}

const data = reactive({
  dataForm: {
    place: null,
    uuid: "",
    checkup: [] as CheckupItem[],
    template: null,
  },
  customer: {
    name: "<无>",
    sex: "<无>",
    age: "<无>",
    tel: "<无>",
    uuid: "<无>",
    date: null,
  },
  doctor: {
    name: null,
    sex: null,
    tel: null,
    date: new Date().toLocaleDateString(),
  },
});

// 执行查询，加载体检人的信息。
const searchHandle = async () => {
  // 1. 参数的校验：体检科室必须选择，体检编号必须编写，体检编号格式必须正确。
  if (stringIsEmpty(data.dataForm.place)) {
    return ElMessage.error("请选择科室");
  }
  if (stringIsEmpty(data.dataForm.uuid)) {
    return ElMessage.error("请填写体检编号");
  }
  if (!/^[A-Z0-9]{32}$/.test(data.dataForm.uuid)) {
    return ElMessage.error("体检编号格式错误");
  }

  // 2. 发送请求
  try {
    const response = await axios.get("/api/mis/appointment/info", {
      params: { appointmentNo: data.dataForm.uuid },
      headers: { satoken: localStorage.getItem("token") },
    });
    // 3.处理响应
    if (response.data.code !== 200) {
      return ElMessage.error(response.data.msg || "加载失败");
    }
    // 拿到的体检人的信息
    const result = response.data.data.result;
    data.customer.name = result.patientName;
    data.customer.sex = result.gender;
    data.customer.age = result.age;
    data.customer.tel = result.phone;
    data.customer.uuid = result.appointmentNo;
    data.customer.date = result.appointmentDate;

    // 4. 判断预约日期
    if (data.customer.date !== dayjs().format("YYYY-MM-DD")) {
      // 预约日期 不是当天，提醒一下医生
      try {
        await ElMessageBox.confirm(
          "该体检人非当日预约，确定要执行体检吗？",
          "提示信息",
          {
            confirmButtonText: "执行",
            cancelButtonText: "拒绝",
            type: "warning",
          },
        );
        // 程序执行到这里说明医生点击的是执行。
        // 加载体检项目
        loadCheckup();
      } catch {
        // 程序执行到这里说明医生点击的是拒绝。
        clearData();
      }
    } else {
      // 说明预约是当天的。
      loadCheckup();
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
};

// 加载体检人的体检项目
const loadCheckup = async () => {
  try {
    const response = await axios.get("/api/mis/checkup", {
      params: {
        appointmentNo: data.dataForm.uuid,
        place: data.dataForm.place,
      },
      headers: { satoken: localStorage.getItem("token") },
    });

    const result = response.data.data.result;
    console.log(result);
    // 获取体检项列表
    const checkupList = result.checkupList;

    // 遍历体检项列表
    for (const one of checkupList) {
      // 给one对象扩展input属性
      // input对象上有两个属性：select 和 value
      // select 和 value用来接收医生填写体检结果。
      one.input = { select: null, value: null };

      if (one.value === "无") {
        one.value = ["其他"];
        // 一定是需要医生手动录入的，因此要激活input输入框。
        one.input.select = "其他";
      } else {
        one.value = one.value.split("#");
      }
      // 1.模板template作用：和后期生成体检报告有关系。
      // 2.data.dataForm.template它全局只有一份。
      // 3.以下代码在循环过程中会产生覆盖。100个对象，有99次覆盖。
      // 4.没有关系，因为同一个科室的体检结果最终的template值都是相同的。
      data.dataForm.template = one.template;
    }

    data.dataForm.checkup = checkupList;

    // 判断是否已经存在体检结果
    // 只要该体检人在该科室存在体检结果，一定要提示医生。防止医生重复体检。
    // 当然，有体检结果的时候，我们这个结果也不再进行初始化了。
    // 即使有体检结果，医生也是全部重新录入新的结果。
    if (result.hasCheckupResults) {
      try {
        await ElMessageBox.confirm(
          "该体检人在当前科室已存在检查结果，是否重新执行检查？",
          "提示信息",
          {
            confirmButtonText: "执行",
            cancelButtonText: "拒绝",
            type: "warning",
          },
        );
        // 医生点击执行，会走到这里。
      } catch {
        // 医生点击拒绝会走到这里。
        clearData();
      }
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
};

const dialogForm = ref();
const selectDeptHandle = async () => {
  dialog.visible = true;
  await nextTick();
  dialogForm.value.clearValidate();
};

const clearCheckupHandle = async () => {
  try {
    await ElMessageBox.confirm("您确定要重新填写体检结果吗？", "提示信息", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    for (const one of data.dataForm.checkup) {
      one.input = {
        select: one.value.length === 1 ? "其他" : null,
        value: null,
      };
    }
  } catch {}
};
const dataFormSubmit = async () => {
  // 提交体检结果的时候，要求每个体检项必须有结果，不能为空。
  for (const one of data.dataForm.checkup) {
    if (
      one.input.select == null ||
      (one.input.select === "其他" && one.input.value == null)
    ) {
      return ElMessage.error("体检结果有空的，请填写");
    }
  }

  try {
    // 准备要提交的数据
    const items = data.dataForm.checkup.map((one) => ({
      name: one.item,
      unit: one.unit,
      standard: one.standard,
      value: one.input.select === "其他" ? one.input.value : one.input.select,
    }));

    // 发送ajax请求保存/更新体检结果
    const response = await axios.post(
      "/api/mis/checkup/result",
      {
        doctorName: data.doctor.name,
        appointmentNo: data.dataForm.uuid,
        place: data.dataForm.place,
        template: data.dataForm.template,
        item: items,
      },
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success({
        message: response.data.msg || "操作成功",
        duration: 1200,
        onClose: () => clearData(),
      });
    } else {
      ElMessage.error({
        message: response.data.msg || "操作失败",
        duration: 1200,
      });
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
};

const loadDoctorSummary = async () => {
  try {
    const response = await axios.get("/api/mis/user/doctor/profile", {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    if (response.data.code !== 200) {
      return ElMessage.error({
        message: response.data.msg || "医生信息加载失败",
        duration: 1200,
      });
    }

    const result = response.data.data.result;
    data.doctor.name = result.username;
    data.doctor.sex = result.gender;
    data.doctor.tel = result.mobile;
  } catch (e) {
    console.log(e);
    ElMessage.error({
      message: "网络异常，稍后再试",
      duration: 1200,
    });
  }
};

// 页面打开时加载医生的摘要信息
loadDoctorSummary();

const dialog = reactive({
  visible: false,
  dataRule: {
    place: [{ required: true, message: "没有选择科室" }],
  },
});

const changePlaceHandle = () => {
  // 科室切换，清除数据
  // 清除数据的方法在当前页面中可能会被调用多次，因此封装一个方法。
  clearData();
};

const clearData = () => {
  // 清除体检人的信息
  data.customer.name = "<无>";
  data.customer.sex = "<无>";
  data.customer.tel = "<无>";
  data.customer.age = "<无>";
  data.customer.uuid = "<无>";
  data.customer.date = null;
  // 清空体检项
  data.dataForm.uuid = "";
  data.dataForm.checkup = [];
};

// 用户选择完科室之后，点击 取消/确定 之后，输入体检编号的文本框自动获取到焦点。
const uuid = ref();
const closeDialog = async () => {
  dialog.visible = false;
  // 等弹窗隐藏完毕之后
  await nextTick();
  // 让文本框获取焦点
  uuid.value.focus();
};

// 监听F4键，只要医生按F4键，输入体检编号的文本框自动获取焦点。
// KeyboardEvent 键盘事件对象
const handleKeyup = (e: KeyboardEvent) => {
  if (e.key === "F4") {
    // 为了防止出问题，阻止一下浏览器默认行为
    e.preventDefault();
    // 执行你自己的逻辑，获取焦点的逻辑
    uuid.value.focus();
  }
};

// 组件挂载完毕之后：事件绑定。
onMounted(() => {
  document.addEventListener("keyup", handleKeyup);
});

// 组件会卸载，卸载的时候：接触绑定
onUnmounted(() => {
  document.removeEventListener("keyup", handleKeyup);
});
</script>

<style lang="less" scoped>
@import url("doctor_checkup.less");
</style>
