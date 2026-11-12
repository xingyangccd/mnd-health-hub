<template>
  <div v-if="isAuth(['ROOT', 'FLOW_REGULATION:SELECT'])">
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
      <el-form-item>
        <el-select
          v-model="dataForm.place"
          style="width: 250px"
          placeholder="科室名称"
          clearable
        >
          <el-option v-for="one in data.placeList" :label="one" :value="one" />
        </el-select>
      </el-form-item>
      <el-form-item prop="blueUuid">
        <el-input
          v-model="dataForm.blueUuid"
          placeholder="蓝牙信标UUID"
          maxlength="32"
          class="input"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchHandle()">查询</el-button>
        <el-button
          type="primary"
          :disabled="!isAuth(['ROOT', 'FLOW_REGULATION:INSERT'])"
          @click="addHandle()"
        >
          新增
        </el-button>
        <el-button
          type="danger"
          :disabled="!isAuth(['ROOT', 'FLOW_REGULATION:DELETE'])"
          @click="deleteHandle()"
        >
          批量删除
        </el-button>
      </el-form-item>
      <el-form-item class="mold">
        <el-switch
          v-model="dataForm.mode"
          size="large"
          active-text="自动调流"
          inactive-text="手动调流"
          :before-change="changeModeHandle"
        />
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
        prop="departmentName"
        header-align="center"
        align="center"
        min-width="250"
        label="科室名称"
      />
      <el-table-column
        prop="beaconUuid"
        header-align="center"
        align="center"
        min-width="270"
        label="蓝牙信标UUID"
      />
      <el-table-column
        prop="currentCount"
        header-align="center"
        align="center"
        min-width="120"
        label="实际排队人数"
      />
      <el-table-column
        prop="maxCapacity"
        header-align="center"
        align="center"
        min-width="120"
        label="最大排队人数"
      />
      <el-table-column
        prop="weight"
        header-align="center"
        align="center"
        min-width="120"
        label="调流权重"
      />
      <el-table-column
        prop="priority"
        header-align="center"
        align="center"
        min-width="120"
        label="调流优先级"
      />
      <el-table-column
        header-align="center"
        align="center"
        width="150"
        label="操作"
      >
        <template #default="scope">
          <el-button
            type="text"
            v-if="isAuth(['ROOT', 'FLOW_REGULATION:UPDATE'])"
            :disabled="scope.row.status"
            @click="updateHandle(scope.row.id)"
          >
            修改
          </el-button>
          <el-button
            type="text"
            v-if="isAuth(['ROOT', 'FLOW_REGULATION:DELETE'])"
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

  <el-dialog
    :title="!dialog.dataForm.id ? '新增' : '修改'"
    v-if="isAuth(['ROOT', 'FLOW_REGULATION:INSERT', 'FLOW_REGULATION:UPDATE'])"
    :close-on-click-modal="false"
    v-model="dialog.visible"
    width="450px"
  >
    <el-form
      :model="dialog.dataForm"
      ref="dialogForm"
      :rules="dialog.dataRule"
      label-width="80px"
    >
      <el-form-item label="科室名称" prop="place">
        <el-input
          v-model="dialog.dataForm.place"
          placeholder="输入科室名称"
          maxlength="40"
          clearable
        />
      </el-form-item>
      <el-form-item label="蓝牙信标" prop="blueUuid">
        <el-input
          v-model="dialog.dataForm.blueUuid"
          placeholder="输入蓝牙信标UUID"
          maxlength="32"
          clearable
        />
      </el-form-item>
      <el-form-item label="排队上限">
        <el-input-number
          v-model="dialog.dataForm.maxNum"
          :min="1"
          :max="500"
          :step="10"
          step-strictly
        />
      </el-form-item>
      <el-form-item label="限流权重">
        <el-slider
          v-model="dialog.dataForm.weight"
          :step="1"
          show-stops
          :min="1"
          :max="10"
        />
      </el-form-item>
      <el-form-item label="优先级">
        <el-slider
          v-model="dialog.dataForm.priority"
          :step="1"
          show-stops
          :min="1"
          :max="10"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="dataFormSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { nextTick, reactive, ref } from "vue";
import { isAuth } from "@/utils/auth";
import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";

interface DataForm {
  place: null | string;
  blueUuid: null | string;
  mode: string | number | boolean | undefined;
}

const dataForm: DataForm = reactive({
  place: null,
  blueUuid: null,
  mode: undefined,
});

const data = reactive({
  placeList: [],
  dataList: [],
  pageIndex: 1,
  pageSize: 10,
  totalCount: 0,
  loading: false,
  selections: [],
});

const dataRule = reactive({
  blueUuid: [
    {
      required: false,
      pattern: "^[a-zA-Z0-9]{32}$",
      message: "蓝牙信标UUID格式错误",
    },
  ],
});

const form = ref();
const searchHandle = async () => {
  try {
    await form.value.validate();
    data.pageIndex = 1;
    loadPageList();
  } catch {}
};

const dialogForm = ref();
const addHandle = async () => {
  // 重置表单数据
  dialog.dataForm.id = null;
  dialog.dataForm.place = null;
  dialog.dataForm.blueUuid = null;
  dialog.dataForm.maxNum = 10;
  dialog.dataForm.weight = 1;
  dialog.dataForm.priority = 1;
  dialog.visible = true;
  await nextTick();
  // 重置数据，并且清空残留的错误提示信息。
  dialogForm.value.resetFields();
};

const deleteHandle = async (id?: number) => {
  const ids = id ? [id] : data.selections.map((item: any) => item.id);

  if (ids.length === 0) {
    return ElMessage.warning("请选择要删除的数据");
  }

  try {
    await ElMessageBox.confirm("您确定要删除数据吗？", "提示信息", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });

    const response = await axios.delete("/api/mis/flow-regulation", {
      data: { ids },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    if (response.data.code === 200) {
      ElMessage.success({
        message: response.data.msg,
        duration: 1200,
        onClose: () => {
          loadDeptNames();
          loadPageList();
        },
      });
    } else {
      ElMessage.error(response.data.msg || "操作失败");
    }
  } catch (error) {
    // 用户取消删除或网络异常
    if (error === "cancel") {
      return;
    }
    console.error("删除失败:", error);
    ElMessage.error("网络异常，请稍后重试");
  }
};

const changeModeHandle = async () => {
  console.log(dataForm.mode);
  try {
    const response = await axios.put(
      "/api/mis/flow-regulation/mode",
      {
        mode: !dataForm.mode,
      },
      {
        headers: { satoken: localStorage.getItem("token") },
      },
    );
    if (response.data.data.result) {
      ElMessage.success("切换成功");
      // 继续切换EP组件的状态
      return true;
    } else {
      ElMessage.error(response.data.msg || "切换失败");
      // 不会切换EP组件库的状态
      return false;
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
    // 不会切换EP组件库的状态
    return false;
  }
};

const selectionChangeHandle = (rows: any) => {
  data.selections = rows;
};

const selectable = () => {
  return true;
};

const updateHandle = async (id: any) => {
  dialog.dataForm.id = id;
  try {
    const response = await axios.get("/api/mis/flow-regulation/config", {
      params: { id },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    if (response.data.code === 200) {
      const result = response.data.data.result;
      dialog.dataForm.place = result.departmentName;
      dialog.dataForm.blueUuid = result.beaconUuid;
      dialog.dataForm.maxNum = result.maxCapacity;
      dialog.dataForm.weight = result.weight;
      dialog.dataForm.priority = result.priority;

      dialog.visible = true;
      await nextTick();
      dialogForm.value.clearValidate();
    } else {
      ElMessage.error({
        message: response.data.msg || "查询科室调流规则失败",
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

const sizeChangeHandle = (pageSize: number) => {
  data.pageSize = pageSize;
  data.pageIndex = 1;
  loadPageList();
};
const currentChangeHandle = (pageNo: number) => {
  data.pageIndex = pageNo;
  loadPageList();
};

const test1 = () => {};
const test2 = () => {};
const test3 = () => {};

const dialog = reactive({
  visible: false,
  dataForm: {
    id: null,
    place: null,
    blueUuid: null,
    maxNum: 10,
    weight: 1,
    priority: 1,
  },
  dataRule: {
    place: [
      { required: true, message: "科室名称不能为空" },
      { min: 2, message: "科室名称不能少于2个字符" },
      {
        pattern: "^[a-zA-Z0-9\u4e00-\u9fa5\(\)]{2,40}$",
        message: "科室名称格式错误",
      },
    ],
    blueUuid: [
      { required: true, message: "蓝牙信标UUID不能为空" },
      { pattern: "^[a-zA-Z0-9]{32}$", message: "蓝牙信标UUID格式错误" },
    ],
  },
});

// 保存和修改都走这个函数
const dataFormSubmit = async () => {
  // 表单校验
  try {
    await dialogForm.value.validate();
  } catch {
    return;
  }

  try {
    const method = dialog.dataForm.id ? "put" : "post";
    const response = await axios[method](
      "/api/mis/flow-regulation",
      {
        id: dialog.dataForm.id,
        departmentName: dialog.dataForm.place,
        beaconUuid: dialog.dataForm.blueUuid,
        maxCapacity: dialog.dataForm.maxNum,
        weight: dialog.dataForm.weight,
        priority: dialog.dataForm.priority,
      },
      {
        headers: { satoken: localStorage.getItem("token") },
      },
    );

    if (response.data.code === 200) {
      ElMessage.success({
        message: "操作成功",
        duration: 1200,
        onClose: () => {
          dialog.visible = false;
          loadPageList();
          loadDeptNames();
        },
      });
    } else {
      ElMessage.error({
        message: "操作失败",
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

const loadDeptNames = async () => {
  try {
    const response = await axios.get("/api/mis/flow-regulation/dept-names", {
      headers: { satoken: localStorage.getItem("token") },
    });

    console.log(response);

    console.log(response.data.code);

    if (response.data.code === 200) {
      data.placeList = response.data.data.result;
    } else {
      ElMessage.error({
        message: response.data.msg || "科室加载失败",
        duration: 1200,
      });
    }
  } catch (e) {
    console.log(e);
    ElMessage.error({
      message: "网络异常稍后再试",
      duration: 1200,
    });
  }
};

loadDeptNames();

const loadMode = async () => {
  try {
    const response = await axios.get("/api/mis/flow-regulation", {
      headers: { satoken: localStorage.getItem("token") },
    });
    if (response.data.code === 200) {
      dataForm.mode = response.data.data.result;
    } else {
      ElMessage.error({
        message: response.data.msg || "调流模式加载失败",
        duration: 1200,
      });
    }
  } catch (e) {
    console.log(e);
    ElMessage.error({
      message: "网络异常稍后再试",
      duration: 1200,
    });
  }
};

loadMode();

const loadPageList = async () => {
  data.loading = true;
  try {
    const response = await axios.get("/api/mis/flow-regulation/list", {
      params: {
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
        deptName: dataForm.place,
        beaconUuid: dataForm.blueUuid,
      },
      headers: { satoken: localStorage.getItem("token") },
    });

    if (response.data.code === 200) {
      const { records, total } = response.data.data.pageResult;
      data.totalCount = total;
      data.dataList = records;
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

loadPageList();
</script>

<style lang="less" scoped>
@import url("flow_regulation.less");
</style>
