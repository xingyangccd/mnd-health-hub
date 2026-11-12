<template>
  <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
    <el-form-item prop="keyword">
      <el-input
        v-model="dataForm.keyword"
        placeholder="套餐名称 / 姓名 / 电话"
        size="default"
        class="keyword"
        clearable
      />
    </el-form-item>
    <el-form-item class="date">
      <el-date-picker
        v-model="dataForm.date"
        type="date"
        placeholder="选择日期"
        :editable="false"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>
    <el-form-item>
      <el-button
        size="default"
        type="primary"
        :icon="Search"
        @click="searchHandle()"
        >查询</el-button
      >
    </el-form-item>
    <el-form-item class="mold">
      <el-radio-group
        v-model="dataForm.statusLabel"
        size="default"
        @change="searchHandle()"
      >
        <el-radio-button label="全部"></el-radio-button>
        <el-radio-button label="已结束"></el-radio-button>
      </el-radio-group>
    </el-form-item>
  </el-form>
  <div class="table-container" v-show="!empty">
    <el-table
      :data="data.dataList"
      class="appointment-table"
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
        prop="goodsTitle"
        header-align="center"
        align="center"
        label="套餐名称"
        min-width="250"
        fixed
      />
      <el-table-column
        prop="patientName"
        header-align="center"
        align="center"
        label="体检人"
        min-width="120"
      />
      <el-table-column
        prop="appointmentDate"
        header-align="center"
        align="center"
        label="预约日期"
        min-width="120"
      />
      <el-table-column
        prop="status"
        header-align="center"
        align="center"
        label="状态"
        min-width="120"
      />
      <el-table-column
        fixed="right"
        header-align="center"
        align="center"
        width="150"
        label="操作"
      >
        <template #default="scope">
          <el-button
            type="text"
            :disabled="scope.row.fileUrl == null"
            @click="downloadHandle(scope.row.name, scope.row.fileUrl)"
          >
            体检报告
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

  <div class="empty" v-show="empty">
    <el-empty :image-size="200" />
  </div>
</template>

<script lang="ts" setup>
import { inject, reactive, ref } from "vue";
import { Search } from "@element-plus/icons-vue";
import { dayjs, ElMessage } from "element-plus";
import isBetween from "dayjs/plugin/isBetween";
import axios from "axios";

// 装上 isBetween 这个插件
// dayjs 的核心包只包含最基础的功能，其他功能都放在插件里，需要手动加载
dayjs.extend(isBetween);

let empty = ref(false);

interface DataForm {
  keyword: null | string;
  date: null | string;
  status: null | number;
  statusLabel: string;
}

const dataForm: DataForm = reactive({
  keyword: null,
  date: null,
  status: null,
  statusLabel: "全部",
});
const dataRule = reactive({
  keyword: [
    {
      required: false,
      pattern: "^[a-zA-Z0-9\u4e00-\u9fa5]{1,30}$",
      message: "关键字内容不正确",
    },
  ],
});
const data = reactive({
  dataList: [],
  pageIndex: 1,
  pageSize: 10,
  totalCount: 0,
  loading: false,
});

const form = ref();
const searchHandle = async () => {
  try {
    await form.value.validate();
    data.pageIndex = 1;
    loadPageList();
  } catch {}
};

const downloadHandle = (name: string, fileUrl: string) => {
  // 按照正常严谨方式应该再编写ajax请求去下载。ajax请求可以鉴权。
  // 但这里我粗略的实现一下（这个是没有走controller，没有鉴权的，直接从minio服务器上下载的。）
  // 风险是：只要有人知道你reportId，就可以下载你的体检报告。
  document.location.href = fileUrl;
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

const loadPageList = async () => {
  data.loading = true;
  try {
    dataForm.status = dataForm.statusLabel === "全部" ? null : 3;
    const response = await axios.get("/api/front/appointment/list", {
      params: {
        keyword: dataForm.keyword,
        status: dataForm.status,
        appointmentDate: dataForm.date,
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
      },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    const pageResult = response.data.data.pageResult;
    const statusMap: Record<string, string> = {
      "1": "未签到",
      "2": "已签到",
      "3": "已完成",
      "4": "已取消",
    };
    pageResult.records.forEach((one: any) => {
      one.status = statusMap[String(one.status)];
      if (one.fileUrl) {
        one.fileUrl = `${minioUrl}/${one.fileUrl}`;
      }
    });
    data.dataList = pageResult.records;
    data.totalCount = pageResult.total;
    empty.value = pageResult.records.length === 0;
  } catch (error) {
    console.error("加载预约列表失败:", error);
    ElMessage.error("网络异常，请稍后重试");
  } finally {
    data.loading = false;
  }
};

loadPageList();
</script>

<style lang="less" scoped>
@import url("appointment_list.less");
</style>
