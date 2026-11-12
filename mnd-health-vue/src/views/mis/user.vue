<template>
  <div v-if="isAuth(['ROOT', 'USER:SELECT'])">
    <!-- 没有el-form同样可以显示页面，为什么用它？主要是用它来完成表单验证功能 -->
    <!-- :inline="true" 表示控件排成一行 -->
    <!-- :model="dataForm" 表单数据绑定 -->
    <!-- :rules="dataRule" 关联验证规则 -->
    <!-- ref="form" 这个给表单起个名，以后获取表单通过名字来获取 -->
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
      <!-- prop="name" 对应的就是 dataRule 中的 name 属性，这样就关联上验证了 -->
      <el-form-item prop="name">
        <!-- class="input" 是自己设置的样式，用来设置表单控件宽度，因为默认情况下，表单控件太宽了 -->
        <el-input
          v-model="dataForm.name"
          placeholder="姓名"
          maxlength="10"
          class="input"
          clearable
          @clear="handleClear"
        />
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="dataForm.sex"
          class="input"
          placeholder="性别"
          clearable
          @clear="handleClear"
        >
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="dataForm.role"
          class="input"
          placeholder="角色"
          clearable
        >
          <!-- 这个位置将来模型层有数据了，自然就显示了 -->
          <el-option
            v-for="one in dataForm.roleList"
            :label="one.roleName"
            :value="one.roleId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="dataForm.deptId"
          class="input"
          placeholder="部门"
          clearable
        >
          <!-- 这个位置也是这样，将来发送ajax请求之后就有数据了 -->
          <el-option
            v-for="one in dataForm.deptList"
            :label="one.deptName"
            :value="one.deptId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="dataForm.status"
          class="input"
          placeholder="状态"
          clearable
        >
          <el-option label="在职" value="1" />
          <el-option label="离职" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <!-- 这些事件对应的函数还没有写，后面写 -->
        <el-button type="primary" @click="searchHandle()">查询</el-button>
        <!-- 当前用户至少有 ROOM或者USER:INSERT中的任何一个权限，这个按钮才能显示 -->
        <el-button
          type="primary"
          :disabled="!isAuth(['ROOT', 'USER:INSERT'])"
          @click="addHandle()"
        >
          新增
        </el-button>
        <!-- 当前用户至少有 ROOT或者USER:DELETE中的任何一个权限，这个按钮才能显示 -->
        <el-button
          type="danger"
          :disabled="!isAuth(['ROOT', 'USER:DELETE'])"
          @click="deleteHandle()"
        >
          批量删除
        </el-button>
      </el-form-item>
    </el-form>

    <!-- :data="data.dataList" 为表格提供数据，现代开发不需要写v-for，
     直接底层自动从数组中取出数据遍历生成表格行，data.dataList存储的是
     当前页数据，不是数据库中全部数据 -->
    <!-- :header-cell-style 设置表头背景色 -->
    <!-- border 表示表格的每个单元格都有边框 -->
    <!-- v-loading="data.loading" 决定是否显示加载动画 -->
    <!-- @selection-change="selectionChangeHandle" 勾选复选框时的回调 -->
    <el-table
      :data="data.dataList"
      :header-cell-style="{ background: '#f5f7fa' }"
      border
      v-loading="data.loading"
      @selection-change="selectionChangeHandle"
    >
      <!-- type="selection" 规定这一列是复选框列 -->
      <!-- header-align="center" 表示表头内容居中 -->
      <!-- align="center" 表示列中数据居中 -->
      <!-- :selectable="handleSelectable"，
       原理：handleSelectable函数执行结果是true，则复选框可选，false复选框禁止选中。
       自动给handleSelectable函数传递当前行数据。
        -->
      <el-table-column
        type="selection"
        header-align="center"
        align="center"
        width="50"
        :selectable="handleSelectable"
      />

      <!-- 这一列是序号，不是主键值。 -->
      <!-- type="index" 告诉EP组件，这一列不用绑定数据，是生成行号的 -->
      <el-table-column
        type="index"
        header-align="center"
        align="center"
        width="100"
        label="序号"
      >
        <!-- 
        scope 是一个回调参数，它包含了当前这行表格数据的所有信息。
        可以把它理解成一个数据快递包，ElTable 会自动把这一行的数据
        “打包”好，通过 scope 传递给你。 
        scope.$index: 当前行在【当前页面】的索引（从0开始）
        scope.row: 当前行的完整数据对象 
        scope.column: 当前列的信息
        scope.store: 表格的状态管理
    -->
        <template #default="scope">
          <!-- 这是序号的算法 -->
          <span>{{
            (data.pageIndex - 1) * data.pageSize + scope.$index + 1
          }}</span>
        </template>
      </el-table-column>
      <!-- prop="name" 这个 name是后端java对象的属性名 -->
      <el-table-column
        prop="realName"
        header-align="center"
        align="center"
        min-width="100"
        label="姓名"
      />
      <el-table-column
        prop="gender"
        header-align="center"
        align="center"
        min-width="60"
        label="性别"
      />
      <el-table-column
        prop="mobile"
        header-align="center"
        align="center"
        min-width="130"
        label="电话"
      />
      <!-- :show-overflow-tooltip="true" 设置为true表示采用缩略显示 -->
      <el-table-column
        prop="email"
        header-align="center"
        align="center"
        min-width="240"
        label="邮箱"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        prop="hireDate"
        header-align="center"
        align="center"
        min-width="130"
        label="入职日期"
      />
      <el-table-column
        prop="roles"
        header-align="center"
        align="center"
        min-width="150"
        label="角色"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        prop="deptName"
        header-align="center"
        align="center"
        min-width="120"
        label="部门"
      />
      <el-table-column
        prop="userStatus"
        header-align="center"
        align="center"
        min-width="100"
        label="状态"
      />
      <el-table-column
        header-align="center"
        align="center"
        width="150"
        label="操作"
      >
        <template #default="scope">
          <!-- type="text" 表示按钮没有边框，看起来像文字。 -->
          <el-button
            type="text"
            v-if="isAuth(['ROOT', 'USER:UPDATE'])"
            @click="updateHandle(scope.row.userId)"
          >
            修改
          </el-button>
          <el-button
            type="text"
            v-if="isAuth(['ROOT', 'USER:UPDATE'])"
            :disabled="scope.row.userStatus == '离职' || scope.row.isSuperAdmin"
            @click="dismissHandle(scope.row.userId)"
          >
            离职
          </el-button>
          <el-button
            type="text"
            :disabled="scope.row.isSuperAdmin"
            v-if="isAuth(['ROOT', 'USER:DELETE'])"
            @click="deleteHandle(scope.row.userId)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- EP组件库的分页组件 -->
    <!-- :current-page 当前页码 -->
    <!-- :page-size 每页显示多少条记录 -->
    <!-- :total 总记录条数 -->
    <!-- :page-sizes  它定义了一个下拉选择框，让用户可以快速切换每页显示的数据条数-->
    <!-- layout 分页最下面一行显示什么内容，顺序是怎样的 -->
    <el-pagination
      @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"
      :current-page="data.pageIndex"
      :page-sizes="[10, 20, 50]"
      :page-size="data.pageSize"
      :total="data.totalCount"
      layout="total, sizes, prev, pager, next, jumper"
    ></el-pagination>
  </div>

  <!-- :title 是动态的，是新增还是修改，看id是不是null，是null就是新增 -->
  <!-- :close-on-click-modal="false" 设置点击模态窗口之外的阴影是否隐藏模态窗口，false不隐藏 -->
  <!-- v-model="dialog.visible" 控制模态窗口隐藏/显示 -->
  <el-dialog
    v-if="isAuth(['ROOT', 'USER:INSERT', 'USER:UPDATE'])"
    :title="!dialog.dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    v-model="dialog.visible"
    width="450px"
  >
    <!-- :model="dialog.dataForm" 表单上绑定的数据 -->
    <!-- ref="dialogForm" 给表单起个名字 -->
    <!-- :rules="dialog.dataRule" 给表单关联校验规则 -->
    <el-form
      :model="dialog.dataForm"
      ref="dialogForm"
      :rules="dialog.dataRule"
      label-width="80px"
    >
      <!-- prop="username"是将username的校验规则绑定到表单项上 -->
      <el-form-item label="用户名" prop="username" v-if="!dialog.update">
        <el-input v-model="dialog.dataForm.username" maxlength="20" clearable />
      </el-form-item>
      <!-- v-if="!dialog.update" 用来决定密码框显示还是隐藏 -->
      <el-form-item label="密码" prop="password" v-if="!dialog.update">
        <el-input
          type="password"
          v-model="dialog.dataForm.password"
          maxlength="20"
          clearable
        />
      </el-form-item>

      <el-form-item label="姓名" prop="realName">
        <el-input v-model="dialog.dataForm.realName" maxlength="10" clearable />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="dialog.dataForm.gender" clearable>
          <el-option label="男" value="男"></el-option>
          <el-option label="女" value="女"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="电话" prop="mobile">
        <el-input v-model="dialog.dataForm.mobile" maxlength="11" clearable />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="dialog.dataForm.email" maxlength="200" clearable />
      </el-form-item>
      <el-form-item label="入职日期" prop="hireDate">
        <!-- type="date" 表示日期的格式只需要年月日 -->
        <!-- :editable="false" 表示用户不可输入，只能鼠标操作 -->
        <!-- value-format="YYYY-MM-DD" 表示js代码读取日期时的日期格式 -->
        <el-date-picker
          v-model="dialog.dataForm.hireDate"
          type="date"
          placeholder="选择日期"
          :editable="false"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <!-- multiple 表示可以多选 -->
        <el-select
          v-model="dialog.dataForm.role"
          placeholder="选择角色"
          class="dialog-input"
          multiple
          clearable
        >
          <el-option
            v-for="one in dataForm.roleList"
            :key="one.roleId"
            :label="one.roleName"
            :value="one.roleId"
            :disabled="one.roleName == '超级管理员'"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="部门">
        <el-select
          v-model="dialog.dataForm.deptId"
          placeholder="选择部门"
          class="dialog-input"
          clearable
        >
          <el-option
            v-for="one in dataForm.deptList"
            :key="one.deptId"
            :label="one.deptName"
            :value="one.deptId"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <!-- 页脚区 -->
    <template #footer>
      <span class="dialog-footer">
        <!-- 直接将 dialog.visible 设置为false 就隐藏了。 -->
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="dataFormSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import { isAuth } from "@/utils/auth";
import { dayjs, ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";
import { nextTick } from "vue";

// 这个form变量代表 el-form 组件实例本身。
const form = ref();
const dialogForm = ref();

type Dept = {
  deptName: string;
  deptId: number;
};

type Role = {
  roleId: number;
  roleName: string;
};

type DataForm = {
  name: string | null;
  sex: string | null;
  role: number | null;
  deptId: number | null;
  deptList: Dept[];
  status: number | null;
  roleList: Role[];
};

// 页面打开的时候，查询所有的角色，加载角色列表
const loadRoleList = async () => {
  try {
    const response = await axios.get("/api/mis/role", {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    const result = response.data;
    if (result.code === 200) {
      dataForm.roleList = result.data.roleList || [];
    } else {
      ElMessage.error(result.msg || "加载角色列表失败");
    }
  } catch {
    ElMessage.error("网络异常，请稍后再试");
  }
};

// 页面打开的时候调用。
loadRoleList();

// 页面打开的时候，查询所有的角色，加载角色列表
const loadDeptList = async () => {
  try {
    const response = await axios.get("/api/mis/dept", {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    const result = response.data;
    if (result.code === 200) {
      dataForm.deptList = result.data.deptList || [];
    } else {
      ElMessage.error(result.msg || "加载部门列表失败");
    }
  } catch {
    ElMessage.error("网络异常，请稍后再试");
  }
};

// 页面打开的时候调用。
loadDeptList();

const dataForm: DataForm = reactive({
  name: null,
  sex: null,
  role: null,
  deptId: null,
  deptList: [],
  status: null,
  roleList: [],
});

const dataRule = reactive({
  // 在这里编写具体的校验规则：
  // 1.是否为必填项
  // 2.是否满足指定的正则表达式
  // 3.具体提示什么错误信息
  name: [
    {
      // 用来指定该项是否为必填项
      required: false,
      // 用来指定正则表达式的
      pattern: "^[\u4e00-\u9fa5]{1,10}$",
      // 用来指定报错的具体信息的
      message: "姓名格式错误",
    },
  ],
});

const searchHandle = async () => {
  // 做表单校验：验证用户输入的查询条件是合法的。
  // 校验是一个异步请求
  const valid = await form.value.validate();
  if (valid) {
    // 分页查询
    data.pageIndex = 1; // 页码置为1
    loadPageList();
  }
};

// 显示新增用户的modal窗口
const addHandle = async () => {
  // 这个函数执行，可以确定用户一定是做新增操作
  dialog.update = false;
  dialog.dataForm.id = null;
  // 显示弹窗（异步的）
  dialog.visible = true;
  // 等待弹窗显示完毕
  await nextTick();
  // 重置表单（清空表单中的内容，恢复成最初的样子）
  // 只能重置带有prop属性的数据
  dialogForm.value.resetFields();
  // 由于部门没有prop属性，所以无法重置，手动重置
  dialog.dataForm.deptId = null;
};

// 单个删除与批量删除共用同一个函数
const deleteHandle = async (userId?: number) => {
  // 收集被删除数据的id集合。
  const userIds = userId ? [userId] : data.selections.map((row: any) => row.userId);

  // 处理用户没有选中记录怎么办？
  if (userIds.length === 0) {
    ElMessage.warning("请选择要删除的记录");
    return;
  }

  // 真正删除
  try {
    // 提示用户确定删除吗？
    await ElMessageBox.confirm("您确定要删除吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    // 程序能够走到这里，说明用户点击的是确定。而非取消。
    // 发送ajax请求
    const response = await axios.delete("/api/mis/user/batch", {
      headers: { satoken: localStorage.getItem("token") },
      data: { userIds },
    });

    if (response.data.data.rows > 0) {
      ElMessage.success({
        message: "操作成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.warning("删除失败");
    }
  } catch (e) {
    // 用户取消或其他错误，静默处理...
  }
};

//-------------------------------------------表格、分页组件--------------------------------------------
const data = reactive({
  // 为表格准备的数据
  dataList: [],
  // 页码
  pageIndex: 1,
  // 每页显示多少条记录
  pageSize: 10,
  // 总记录条数
  totalCount: 0,
  // 控制是否显示加载动画：true表示显示加载动画，false表示不显示加载动画
  loading: false,
  // 被选中的记录
  selections: [],
});

const selectionChangeHandle = (values: any) => {
  // values 是什么：EP组件库会将当前选中的复选框以数组的方式传递给我们。
  // values这个数组当中存储是被选中的row对象。
  data.selections = values;
};

const updateHandle = async (userId: any) => {
  // 先重置表单：清除一下表单上可能残留的验证信息
  if (dialogForm.value) {
    // 重置表单的（重置的是有prop属性的），并且前端验证框架中的错误提示信息
    dialogForm.value.resetFields();
  }

  try {
    const response = await axios.get(`/api/mis/user/${userId}`, {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    if (response.data.code === 200) {
      // 将查询回来的数据赋值到响应式对象的属性上
      const result = response.data.data.user;
      console.log(result);
      dialog.dataForm.id = userId;
      dialog.dataForm.realName = result.realName;
      dialog.dataForm.gender = result.gender;
      dialog.dataForm.mobile = result.mobile;
      dialog.dataForm.email = result.email;
      dialog.dataForm.hireDate = result.hireDate;
      dialog.dataForm.role = JSON.parse(result.roleIds);
      dialog.dataForm.deptId = result.deptId;
      // 显示弹窗
      dialog.update = true;
      dialog.visible = true;
    } else {
      ElMessage.error(response.data.msg || "根据用户ID查询失败");
    }
  } catch {
    ElMessage.error("网络异常，稍后再试");
  }
};

const dismissHandle = async (userId: number) => {
  try {
    await ElMessageBox.confirm("确定要给当前人员办理离职吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const response = await axios.put(`/api/mis/user/${userId}`, null, {
      headers: { satoken: localStorage.getItem("token") },
    });

    if (response.data.code === 200) {
      ElMessage.success({
        message: "离职成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.warning(response.data.msg || "离职失败");
    }
  } catch {
    // 用户取消操作或者报错，静默处理
  }
};

// 当修改每页显示的记录条数时触发这个回调。
const sizeChangeHandle = (pageSize: number) => {
  data.pageIndex = 1;
  data.pageSize = pageSize;
  loadPageList();
};

// 当翻页的时候，页码发生变化的时候，这个回调函数会执行。
const currentChangeHandle = (pageNo: number) => {
  // 设置页码
  data.pageIndex = pageNo;
  // 重新加载数据
  loadPageList();
};

// ------------------------------------------用户信息修改----------------------------------------------------------
const dialog = reactive({
  // 控制模态窗口显示/隐藏
  visible: false,
  // 区分是修改还是新增的标记
  update: false,
  // 给表单绑定数据
  dataForm: {
    // 新增时不需要id，修改时需要id
    id: null,
    username: null,
    // 新增员工时需要密码框，修改时不需要密码框，因为密码只能员工自己修改
    password: null,
    realName: null,
    gender: null,
    mobile: null,
    email: null,
    // 入职日期希望有默认值，使用了dayjs库
    hireDate: dayjs(new Date()).format("YYYY-MM-DD"),
    // 对应选中的角色
    role: null,
    // 对应选中的部门
    deptId: null,
    // 默认值1表示在职
    userStatus: 1,
  },
  dataRule: {
    // 校验规则
    username: [
      { required: true, message: "用户名不能为空" },
      { pattern: "^[a-zA-Z0-9]{5,20}$", message: "用户名格式错误" },
    ],
    password: [
      { required: true, message: "密码不能为空" },
      { pattern: "^[a-zA-Z0-9]{6,20}$", message: "密码格式错误" },
    ],
    realName: [
      { required: true, message: "姓名不能为空" },
      { pattern: "^[\u4e00-\u9fa5]{2,10}$", message: "姓名格式错误" },
    ],
    gender: [{ required: true, message: "性别不能为空" }],
    mobile: [
      { required: true, message: "电话不能为空" },
      { pattern: "^1[1-9]\\d{9}$", message: "电话格式错误" },
    ],
    email: [
      { required: true, message: "邮箱不能为空" },
      {
        pattern: "^([a-zA-Z]|[0-9])(\\w|\\-)+@[a-zA-Z0-9]+\\.([a-zA-Z]{2,4})$",
        message: "邮箱格式错误",
      },
    ],
    hireDate: [{ required: true, message: "入职日期不能为空" }],
    role: [{ required: true, message: "角色不能为空" }],
    userStatus: [{ required: true, message: "状态不能为空" }],
  },
});

// 发送ajax请求，保存用户信息（同时这个函数又可以完成修改用户信息的功能）
const dataFormSubmit = async () => {
  // 先完成表单的校验，如果表单校验没问题，再发送ajax请求。
  // 校验是一个异步请求
  //const valid = await dialogForm.value.validate();

  // 表单校验的正规写法
  try {
    // 原理：校验表单是异步的，校验过程中只要某项校验失败，底层就会抛异常，进入catch中执行。
    await dialogForm.value.validate();
  } catch {
    return;
  }

  // 程序能够执行到这里说明表单中每一项校验都是合法的。
  // 准备数据
  const sendData = {
    // 新增的时候实际上不需要提交userId，但是提交也没关系，因为后端form没有这个属性。
    // 新增不提交，修改时提交
    userId: dialog.dataForm.id,
    // 修改的时候实际上不需要提交username，但是提交也没关系，因为后端form没有这个属性。
    // 新增时提交，修改时不提交
    username: dialog.dataForm.username,
    // 修改的时候实际上不需要提交password，但是提交也没关系，因为后端form没有这个属性。
    // 新增时提交，修改时不提交
    password: dialog.dataForm.password,
    // 以下的这些数据在新增和修改的时候都需要提交
    realName: dialog.dataForm.realName,
    gender: dialog.dataForm.gender,
    mobile: dialog.dataForm.mobile,
    email: dialog.dataForm.email,
    hireDate: dialog.dataForm.hireDate,
    roleIds: dialog.dataForm.role,
    deptId: dialog.dataForm.deptId,
  };

  // 准备config
  const config = {
    headers: {
      satoken: localStorage.getItem("token"),
    },
  };

  // 发送ajax请求
  try {
    // dialog.dataForm.id 有值说明是修改，是null表示是新增
    const response = !!dialog.dataForm.id
      ? await axios.put("/api/mis/user", sendData, config)
      : await axios.post("/api/mis/user", sendData, config);

    const result = response.data;
    if (result.code === 200) {
      ElMessage({
        type: "success",
        message: "操作成功",
        duration: 1200,
        onClose: () => {
          // 隐藏模态窗口
          dialog.visible = false;
          // 重新加载分页数据
          loadPageList();
        },
      });
    } else {
      ElMessage.error(result.msg || "操作失败");
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
};

// 封装一个分页查询的函数
const loadPageList = async () => {
  // 显示加载进度条
  data.loading = true;
  // 发送ajax请求获取分页数据
  try {
    const response = await axios.get("/api/mis/user/list", {
      params: {
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
        realName: dataForm.name, // 空字符串会提交给服务器: ""
        gender: dataForm.sex,
        roleId: dataForm.role,
        deptId: dataForm.deptId,
        userStatus: dataForm.status,
      },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    if (response.data.code === 200) {
      const result = response.data.data.pageResult;
      const records = result.records;
      for (let item of records) {
        if (item.userStatus == 1) {
          item.userStatus = "在职";
        } else if (item.userStatus == 2) {
          item.userStatus = "离职";
        }
      }
      data.dataList = records;
      data.totalCount = result.total;
    } else {
      ElMessage.error(response.data.msg);
    }

    data.loading = false;
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，请稍后再试");
  }
};

// 页面打开时就加载第一页
loadPageList();

// 测试结果：
// EP组件库中：当组件是下拉列表时，清空之后默认赋值 undefined，前端提交时undefined数据不会提交。
// EP组件库中：当组件是文本框时，清空之后默认赋值空字符串 ""，前端提交时会自动提交空字符串给后端。
const handleClear = () => {
  //console.log(dataForm.sex);
  console.log("姓名：", dataForm.name);
};

const handleSelectable = (row: any) => !row.isSuperAdmin;
//const handleSelectable = (row: any) => !row.roles?.split(",").includes("超级管理员");
</script>

<style scoped></style>
