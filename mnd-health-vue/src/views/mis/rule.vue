<template>
  <div v-if="isAuth(['ROOT', 'RULE:SELECT'])">
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
      <el-form-item prop="ruleName">
        <el-input
          v-model="dataForm.ruleName"
          placeholder="规则名称"
          maxlength="20"
          class="input"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchHandle()">查询</el-button>
        <el-button
          type="primary"
          :disabled="!isAuth(['ROOT', 'RULE:INSERT'])"
          @click="addHandle()"
        >
          新增
        </el-button>
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
        prop="ruleName"
        header-align="left"
        align="left"
        min-width="150"
        label="规则名称"
      />
      <el-table-column
        prop="remark"
        header-align="left"
        align="left"
        min-width="350"
        label="备注信息"
      />
      <el-table-column
        prop="count"
        header-align="center"
        align="center"
        min-width="80"
        label="关联套餐数量"
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
            v-if="isAuth(['ROOT', 'RULE:UPDATE'])"
            @click="updateHandle(scope.row.ruleId)"
          >
            修改
          </el-button>
          <el-button
            type="text"
            :disabled="scope.row.count > 0"
            v-if="isAuth(['ROOT', 'RULE:DELETE'])"
            @click="deleteHandle(scope.row.ruleId)"
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
    :title="!dialog.dataForm.ruleId ? '新增' : '修改'"
    v-if="isAuth(['ROOT', 'RULE:INSERT', 'RULE:UPDATE'])"
    :close-on-click-modal="false"
    v-model="dialog.visible"
    custom-class="dialog"
    width="500px"
  >
    <el-form
      :model="dialog.dataForm"
      ref="dialogForm"
      :rules="dialog.dataRule"
      label-width="80px"
    >
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="dialog.dataForm.ruleName" maxlength="20" clearable />
      </el-form-item>
      <el-form-item label="规则内容" prop="ruleContent">
        <el-input
          v-model="dialog.dataForm.ruleContent"
          type="textarea"
          :rows="10"
          clearable
        />
      </el-form-item>
      <el-form-item label="备注信息">
        <el-input
          v-model="dialog.dataForm.remark"
          type="textarea"
          :rows="3"
          clearable
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
import { reactive, getCurrentInstance, ref, onMounted, nextTick } from "vue";
import { Delete } from "@element-plus/icons-vue";
import router from "../../router/index";
import { isAuth } from "@/utils/auth";
import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";

const dataForm = reactive({
  ruleName: null,
});

const dataRule = reactive({
  ruleName: [
    {
      required: false,
      pattern: "^[a-zA-Z0-9\u4e00-\u9fa5]{1,20}$",
      message: "规则名称不正确",
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
    // 校验成功
    data.pageIndex = 1;
    loadPageList();
  } catch {
    // 校验失败了。
  }
};

// 显示新增的modal窗口
const dialogForm = ref();

const addHandle = async () => {
  // 将id设置为空，表示当前操作是一个新增的操作。
  dialog.dataForm.ruleId = null;
  // 将备注信息置空，因为备注信息没有参与表单验证，需要手动置空
  dialog.dataForm.remark = null;
  // 显示窗口
  dialog.visible = true;
  // 等待窗口渲染完毕
  await nextTick();
  // 重置表单数据到初始状态，并清空所有的残留的校验信息。
  // 两个作用：
  // 1.将具有校验规则的表单数据重置到初始状态。
  // 2.清除之前残留的校验错误信息。
  // 这一行代码必须编写到 await nextTick(); 代码的下面，等待页面dom渲染完毕，以下代码才能正常执行。
  dialogForm.value.resetFields();

  // 另外还有一个方法：clearValidate()，它的作用只清除残留的错误的校验信息。不会重置表单数据。
};

const updateHandle = async (ruleId: number) => {
  try {
    const response = await axios.get("/api/mis/rule", {
      params: { ruleId },
      headers: { satoken: localStorage.getItem("token") },
    });

    if (response.data.code === 200) {
      Object.assign(dialog.dataForm, response.data.data.result);
      dialog.visible = true;
      await nextTick();
      // 只清除校验残留的错误信息，不会重置数据。
      dialogForm.value.clearValidate();
    } else {
      ElMessage.error(response.data.msg || "加载失败");
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
};

const deleteHandle = async (ruleId: number) => {
  try {
    await ElMessageBox.confirm("您确定要删除该规则吗？", "提示信息", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const response = await axios.delete("/api/mis/rule", {
      params: { ruleId },
      headers: { satoken: localStorage.getItem("token") },
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
    if (e !== "cancel") {
      console.log(e);
      ElMessage.error("网络异常，稍后再试");
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

const loadPageList = async () => {
  data.loading = true;
  try {
    const response = await axios.get("/api/mis/rule/page", {
      params: {
        ruleName: dataForm.ruleName,
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
      },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    if (response.data.code === 200) {
      const pageResult = response.data.data.pageResult;
      data.dataList = pageResult.records;
      data.totalCount = pageResult.total;
    } else {
      ElMessage.error(response.data.msg || "加载失败");
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  } finally {
    data.loading = false;
  }
};

loadPageList();

const dialog = reactive({
  visible: false,
  dataForm: {
    ruleId: null,
    ruleName: null,
    remark: null,
    ruleContent: null,
  },
  dataRule: {
    ruleName: [
      { required: true, message: "规则名称不能为空" },
      {
        pattern: "^[a-zA-Z0-9\u4e00-\u9fa5]{1,20}$",
        message: "规则名称不正确",
      },
    ],
    ruleContent: [
      { required: true, trigger: "blur", message: "规则内容不能为空" },
    ],
  },
});

const dataFormSubmit = async () => {
  try {
    // 保存之前做一次校验，合法的话，再提交。
    await dialogForm.value.validate();
    // 程序能够走到这里说明校验成功
    const method = dialog.dataForm.ruleId == null ? "post" : "put";
    const response = await axios[method](
      "/api/mis/rule",
      {
        ruleId: dialog.dataForm.ruleId, // 这个数据虽然在新增的时候不需要，但是修改的时候需要。
        ruleName: dialog.dataForm.ruleName,
        ruleContent: dialog.dataForm.ruleContent,
        remark: dialog.dataForm.remark,
      },
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );

    if (response.data.code === 200) {
      dialog.visible = false;
      ElMessage.success({
        message: "操作成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.success({
        message: response.data.msg || "操作失败",
        duration: 1200,
      });
    }
  } catch (e) {
    // 校验失败，或者发生了错误，都会走到这里。
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
};
</script>
