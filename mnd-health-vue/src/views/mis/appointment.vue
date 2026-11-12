<template>
  <div v-if="isAuth(['ROOT', 'APPOINTMENT:SELECT'])">
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
      <el-form-item>
        <el-date-picker
          v-model="dataForm.appointmentDate"
          type="date"
          placeholder="选择日期"
          :editable="false"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          clearable
        />
      </el-form-item>
      <el-form-item prop="patientName">
        <el-input
          v-model="dataForm.patientName"
          placeholder="姓名"
          maxlength="10"
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
      <el-form-item>
        <el-button type="primary" @click="searchHandle()">查询</el-button>
        <el-button
          type="danger"
          :disabled="!isAuth(['ROOT', 'APPOINTMENT:DELETE'])"
          @click="deleteHandle()"
        >
          批量删除
        </el-button>
      </el-form-item>
      <el-form-item class="mold">
        <el-radio-group v-model="dataForm.statusLabel" @change="searchHandle()">
          <el-radio-button label="全部"></el-radio-button>
          <el-radio-button label="未签到"></el-radio-button>
          <el-radio-button label="已签到"></el-radio-button>
          <el-radio-button label="已结束"></el-radio-button>
          <el-radio-button label="已关闭"></el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <el-table
      :data="data.dataList"
      :header-cell-style="{ background: '#f5f7fa' }"
      border
      v-loading="data.loading"
      @selection-change="selectionChangeHandle"
    >
      <el-table-column
        type="selection"
        :selectable="selectable"
        header-align="center"
        align="center"
        width="50"
        fixed
      />
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
        prop="patientName"
        header-align="center"
        align="center"
        label="姓名"
        width="200"
        fixed
      />
      <el-table-column
        prop="gender"
        header-align="center"
        align="center"
        label="性别"
        width="100"
      />
      <el-table-column
        prop="age"
        header-align="center"
        align="center"
        label="年龄"
        width="100"
      />
      <el-table-column
        prop="phone"
        header-align="center"
        align="center"
        label="联系电话"
        width="150"
      />
      <el-table-column
        prop="idCardNo"
        header-align="center"
        align="center"
        label="身份证号"
        width="190"
      />
      <el-table-column
        prop="company"
        header-align="center"
        align="center"
        label="公司名称"
        width="200"
      />
      <el-table-column
        prop="goodsTitle"
        header-align="center"
        align="center"
        label="体检套餐"
        min-width="200"
      />
      <el-table-column
        prop="status"
        header-align="center"
        align="center"
        label="状态"
        width="120"
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
            :disabled="
              !isAuth(['ROOT', 'APPOINTMENT:DELETE']) ||
              scope.row.status != '未签到'
            "
            @click="deleteHandle(scope.row.id)"
          >
            删除
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
import { isAuth } from "@/utils/auth";
import { reactive, ref } from "vue";
import { dayjs, ElMessage, ElMessageBox } from "element-plus";
import isBetween from "dayjs/plugin/isBetween";
import axios from "axios";
dayjs.extend(isBetween);

interface DataForm {
  patientName: null | string;
  phone: null | string;
  appointmentDate: null | string;
  statusLabel: string;
  status: null | number;
}

const dataForm: DataForm = reactive({
  patientName: null,
  phone: null,
  appointmentDate: null,
  statusLabel: "全部",
  status: null,
});

const dataRule = reactive({
  patientName: [
    { pattern: "^[\u4e00-\u9fa5]{1,10}$", message: "姓名格式错误" },
  ],
  phone: [{ pattern: "^1[1-9]\\d{9}$", message: "电话号码格式错误" }],
});

const data = reactive({
  dataList: [],
  pageIndex: 1,
  pageSize: 10,
  totalCount: 0,
  loading: false,
  selections: [],
});

const form = ref();
const searchHandle = async () => {
  try {
    await form.value.validate();
    data.pageIndex = 1;
    loadPageList();
  } catch {}
};

const deleteHandle = async (id?: number) => {
  const ids = id ? [id] : data.selections.map((item: any) => item.id);
  if (!ids || ids.length === 0) {
    ElMessage.warning("请选择要删除的预约记录");
    return;
  }
  try {
    await ElMessageBox.confirm("您确定要删除预约记录吗？", "提示信息", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    const response = await axios.delete("/api/mis/appointment", {
      data: { ids },
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
      ElMessage.error(response.data.msg || "删除失败");
    }
  } catch (error) {
    if (error === "cancel") return;
    console.error("删除预约失败:", error);
    ElMessage.error("网络异常，稍后再试");
  }
};

const selectionChangeHandle = (rows: any) => {
  data.selections = rows;
};

const selectable = (row: any) => {
  return row.status == "未签到";
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

const loadPageList = async () => {
  data.loading = true;
  try {
    const statusLabelMap: Record<string, null | number> = {
      全部: null,
      未签到: 1,
      已签到: 2,
      已结束: 3,
      已关闭: 4,
    };
    dataForm.status = statusLabelMap[dataForm.statusLabel] ?? null;
    const response = await axios.get("/api/mis/appointment/list", {
      params: {
        patientName: dataForm.patientName,
        phone: dataForm.phone,
        appointmentDate: dataForm.appointmentDate,
        status: dataForm.status,
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
      },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    if (response.data.code === 200) {
      const { records, total } = response.data.data.pageResult;
      let statusMap: Record<string, string> = {
        "1": "未签到",
        "2": "已签到",
        "3": "已结束",
        "4": "已关闭",
      };
      for (let one of records) {
        one.status = statusMap[String(one.status)];
      }
      data.dataList = records;
      data.totalCount = total;
    } else {
      ElMessage.error(response.data.msg || "加载失败");
    }
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
@import url("appointment.less");
</style>
