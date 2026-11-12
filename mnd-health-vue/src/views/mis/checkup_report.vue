<template>
  <div v-if="isAuth(['ROOT', 'CHECKUP_REPORT:SELECT'])">
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
      <el-form-item prop="patientName">
        <el-input
          v-model="dataForm.patientName"
          placeholder="姓名"
          maxlength="20"
          class="input"
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
      <el-form-item prop="expressNo">
        <el-input
          v-model="dataForm.expressNo"
          placeholder="快递单号"
          maxlength="24"
          class="input"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchHandle">查询</el-button>
      </el-form-item>
      <el-form-item>
        <el-upload
          :action="upload.action"
          name="file"
          accept=".xlsx"
          :headers="upload.headers"
          :show-file-list="false"
          :before-upload="excelBeforeUpload"
          :on-success="excelUploadSuccess"
          :on-error="excelUploadError"
        >
          <el-button type="primary">导入运单</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item class="mold">
        <el-radio-group v-model="dataForm.statusLabel" @change="searchHandle">
          <el-radio-button label="全部"></el-radio-button>
          <el-radio-button label="未生成"></el-radio-button>
          <el-radio-button label="已生成"></el-radio-button>
          <el-radio-button label="已寄出"></el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <el-table
      :data="data.dataList"
      :header-cell-style="{ background: '#f5f7fa' }"
      border
      v-loading="data.loading"
    >
      <el-table-column
        type="index"
        header-align="center"
        align="center"
        width="120"
        label="序号"
        fixed
      >
        <template #default="scope">
          <span>{{
            (data.pageIndex - 1) * data.pageSize + scope.$index + 1
          }}</span>
        </template>
      </el-table-column>
      <el-table-column
        header-align="center"
        align="center"
        label="姓名"
        min-width="150"
        fixed
      >
        <template #default="scope">
          <span
            class="customer-name"
            @dblclick="
              copyAddressHandle(
                scope.row.patientName,
                scope.row.phone,
                scope.row.address,
              )
            "
            >{{ scope.row.patientName }}</span
          >
        </template>
      </el-table-column>
      <el-table-column
        prop="gender"
        header-align="center"
        align="center"
        label="性别"
        width="130"
      />
      <el-table-column
        prop="age"
        header-align="center"
        align="center"
        label="年龄"
        width="130"
      />
      <el-table-column
        prop="hidePhone"
        header-align="center"
        align="center"
        label="电话"
        width="180"
      />
      <el-table-column
        prop="status"
        header-align="center"
        align="center"
        label="报告状态"
        width="140"
      />
      <el-table-column
        prop="expressNo"
        header-align="center"
        align="center"
        label="快递单号"
        width="280"
      />
      <el-table-column
        prop="expressDate"
        header-align="center"
        align="center"
        label="寄出日期"
        width="170"
      />
      <el-table-column
        fixed="right"
        header-align="center"
        align="center"
        width="250"
        label="操作"
      >
        <template #default="scope">
          <el-button
            type="text"
            :disabled="scope.row.disabled"
            @click="createReportHandle(scope.row.id)"
          >
            生成报告
          </el-button>
          <el-button
            type="text"
            :disabled="scope.row.status != '已生成'"
            @click="
              downloadReportHandle(scope.row.patientName, scope.row.fileUrl)
            "
          >
            下载报告
          </el-button>
          <el-button
            type="text"
            :disabled="scope.row.status != '已生成'"
            @click="
              identifyWaybillHandle(
                scope.row.id,
                scope.row.patientName,
                scope.row.phone,
              )
            "
          >
            录入运单
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

  <el-dialog
    title="录入运单"
    v-if="isAuth(['ROOT', 'CHECKUP_REPORT:UPDATE'])"
    :close-on-click-modal="false"
    v-model="dialog.visible"
    width="420px"
  >
    <el-alert
      title="请上传快递运单照片，系统将自动识别运单中的信息，然后请您核对无误后，提交运单信息"
      type="success"
      :closable="false"
    />
    <el-upload
      :action="dialog.upload.action"
      name="file"
      accept=".jpg,.jpeg,.png"
      :headers="dialog.upload.headers"
      :show-file-list="false"
      :before-upload="waybillBeforeUpload"
      :on-success="waybillUploadSuccess"
      :on-error="waybillUploadError"
    >
      <el-button type="primary">上传运单</el-button>
    </el-upload>
    <el-divider border-style="dashed">认真核对下方运单信息</el-divider>
    <table class="waybill-table">
      <tr>
        <th>收件人姓名：</th>
        <td>{{ dialog.data.recName }}</td>
      </tr>
      <tr>
        <th>收件人电话：</th>
        <td>{{ dialog.data.recPhone }}</td>
      </tr>
      <tr>
        <th>运单号码：</th>
        <td>{{ dialog.data.expressNo }}</td>
      </tr>
    </table>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="
            dialog.data.expressNo == null ||
            dialog.data.patientName != dialog.data.recName ||
            dialog.data.recPhone.substr(7, 4) != dialog.data.phoneEnd
          "
          @click="dataFormSubmit"
        >
          提交运单
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import { isAuth } from "@/utils/auth";
import { ElMessage, ElNotification } from "element-plus";

import { dayjs } from "element-plus";
import isSameOrBefore from "dayjs/plugin/isSameOrBefore";
import axios from "axios";
dayjs.extend(isSameOrBefore);

interface DataForm {
  patientName: null | string;
  phone: null | string;
  expressNo: null | string;
  statusLabel: string;
  status: null | number | undefined;
}

const dataForm: DataForm = reactive({
  patientName: null,
  phone: null,
  expressNo: null,
  statusLabel: "全部",
  status: null,
});

const data = reactive({
  dataList: [],
  pageIndex: 1,
  pageSize: 10,
  totalCount: 0,
  loading: false,
});

const dataRule = reactive({
  patientName: [
    {
      required: false,
      pattern: "^[\u4e00-\u9fa5]{1,10}$",
      message: "姓名格式错误",
    },
  ],
  phone: [
    { required: false, pattern: "^1[1-9]\\d{9}$", message: "电话格式错误" },
  ],
  expressNo: [
    {
      required: false,
      pattern: "^[0-9a-zA-Z]{10,24}$",
      message: "快递单号格式错误",
    },
  ],
});

const upload = reactive({
  action: "/api/mis/checkup/reports",
  headers: {
    satoken: localStorage.getItem("token"),
  },
});

const form = ref();
const searchHandle = async () => {
  try {
    await form.value.validate();
    data.pageIndex = 1;
    loadPageList();
  } catch {}
};

const excelBeforeUpload = () => {};
const excelUploadSuccess = () => {};
const excelUploadError = () => {};

const copyAddressHandle = async (
  patientName: string,
  phone: string,
  address: string,
) => {
  try {
    const info = `${patientName},${phone},${address}`;
    await navigator.clipboard.writeText(info); // 这一行代码作用：将信息写入到剪贴板
    ElMessage.success("已复制");
  } catch (e) {
    console.log("复制失败", e);
    ElMessage.error("复制失败，请手动复制");
  }
};

const createReportHandle = async (id: number) => {
  try {
    ElNotification({
      title: "提示信息",
      message: "体检报告正在生成，请稍后",
      duration: 1200,
    });

    const response = await axios.post(
      "/api/mis/report",
      { id },
      {
        headers: { satoken: localStorage.getItem("token") },
      },
    );

    if (response.data.data.result) {
      ElMessage.success({
        message: "体检报告创建成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.error({
        message: response.data.msg || "体检报告创建失败",
        duration: 1200,
      });
    }
  } catch (e) {
    console.log(e);
    ElMessage.error({
      message: "网络异常，稍后再试",
      duration: 1200,
    });
  }
};

const downloadReportHandle = async (patientName: string, fileUrl: string) => {
  try {
    const response = await axios.get("/api/mis/report/download", {
      params: { name: patientName, fileUrl: fileUrl },
      headers: { satoken: localStorage.getItem("token") },
      responseType: "blob",
    });

    // 检查响应类型
    if (response.data.type === "application/json") {
      const text = await response.data.text();
      const json = JSON.parse(text);
      ElMessage.error(json.msg || "下载失败");
      return;
    }

    // 创建下载链接
    const url = URL.createObjectURL(response.data);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${patientName}的体检报告.docx`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url); // 释放内存
  } catch (error) {
    console.error("下载失败:", error);
    ElMessage.error("下载失败，请稍后重试");
  }
};

// 显示录入运单的弹窗
const identifyWaybillHandle = (
  id: number,
  patientName: string,
  phone: string,
) => {
  dialog.data.recName = "";
  dialog.data.recPhone = "";
  dialog.data.expressNo = "";

  dialog.data.id = id;
  dialog.data.patientName = patientName;
  dialog.data.phoneEnd = phone.substring(7, 11);

  dialog.visible = true;
};

const sizeChangeHandle = (pageSize: number) => {
  data.pageIndex = 1;
  data.pageSize = pageSize;
  loadPageList();
};
const currentChangeHandle = (pageNo: number) => {
  data.pageIndex = pageNo;
  loadPageList();
};

const dialog = reactive({
  visible: false,
  upload: {
    action: "/api/mis/report/waybill",
    headers: {
      satoken: localStorage.getItem("token"),
    },
    data: {
      id: null,
    },
  },
  data: {
    id: null,
    recName: "",
    recPhone: "",
    expressNo: "",
    patientName: null,
    phoneEnd: null,
  } as {
    id: null | number;
    recName: string;
    recPhone: string;
    expressNo: string;
    patientName: null | string;
    phoneEnd: null | string;
  },
});

const waybillBeforeUpload = (file: File) => {
  const maxSize = 5;
  const fileSizeMB = file.size / 1024 / 1024;
  if (fileSizeMB > maxSize) {
    ElMessage.error(`图片不能超过${maxSize}MB`);
    // 取消请求的方法
    return false;
  }
  ElMessage.info("运单识别中");
  // 继续发送请求。
  return true;
};

const waybillUploadSuccess = (response: any) => {
  if (response.code !== 200) {
    ElMessage.error(response.msg || "识别失败");
    return;
  }

  const result = response.data.result;
  if (!result) {
    ElMessage.error("识别结果为空");
    return;
  }

  // 提取识别的结果
  const { recName, recPhone, expressNo } = result;
  dialog.data.recName = recName;
  dialog.data.recPhone = recPhone;
  dialog.data.expressNo = expressNo;

  // 验证收件人的信息是否正确
  const phoneSuffix = recPhone.slice(-4);
  const isMatch =
    recName === dialog.data.patientName && phoneSuffix === dialog.data.phoneEnd;
  if (!isMatch) {
    ElMessage.warning("收件人姓名或电话与体检人不符，请确认");
  } else {
    ElMessage.success("运单识别成功");
  }
};

const waybillUploadError = (e: Error) => {
  console.log(e);
  ElMessage.error("图片上传失败，请重试");
};

const dataFormSubmit = async () => {
  const expressNo = dialog.data.expressNo;
  const expressDate = dayjs().format("YYYY-MM-DD");

  if (!expressNo || expressNo.trim() === "") {
    ElMessage.warning("请先识别或输入快递单号");
    return;
  }

  try {
    const response = await axios.put(
      "/api/mis/report/waybill/save",
      {
        id: dialog.data.id,
        expressNo: expressNo.trim(),
        expressDate,
      },
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success({
        message: "运单提交成功",
        duration: 1200,
        onClose: () => {
          dialog.visible = false;
          loadPageList();
        },
      });
    } else {
      ElMessage.error(response.data.msg || "运单提交失败");
    }
  } catch (error) {
    console.error("提交运单失败:", error);
    ElMessage.error("网络异常，请稍后重试");
  }
};

// 封装分页查询的函数
const loadPageList = async () => {
  try {
    data.loading = true;

    // 处理 statusLabel 转换为 status
    const statusMap: Record<string, null | number> = {
      全部: null,
      未生成: 1,
      已生成: 2,
      已寄出: 3,
    };
    dataForm.status = statusMap[dataForm.statusLabel];

    const response = await axios.get("/api/mis/report/list", {
      params: {
        patientName: dataForm.patientName,
        phone: dataForm.phone,
        expressNo: dataForm.expressNo,
        status: dataForm.status,
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
      },
      headers: { satoken: localStorage.getItem("token") },
    });

    if (response.data.code === 200) {
      const { records, total } = response.data.data.pageResult;

      const statusDisplay: Record<string, string> = {
        "1": "未生成",
        "2": "已生成",
        "3": "已寄出",
      };

      for (const one of records) {
        one.status = statusDisplay[one.status];
        // 给one对象扩展一个属性（每一行记录都扩展一个属性）
        one.disabled = dayjs()
          .subtract(10, "day")
          .isSameOrBefore(dayjs(one.appointmentDate));
      }

      data.dataList = records;
      data.totalCount = total;
    } else {
      ElMessage.error({
        message: response.data.msg || "加载失败",
        duration: 1200,
      });
    }
  } catch (e) {
    console.log(e);
    ElMessage.error({
      message: "网络异常，稍后再试",
      duration: 1200,
    });
  } finally {
    data.loading = false;
  }
};

// 页面打开时加载第一页数据
loadPageList();
</script>
<style lang="less" scoped>
@import url("checkup_report.less");
</style>
