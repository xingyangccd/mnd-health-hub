<template>
  <div v-if="isAuth(['ROOT', 'GOODS:SELECT'])">
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
      <el-form-item prop="packageName">
        <el-input
          v-model="dataForm.packageName"
          placeholder="套餐名称"
          maxlength="50"
          class="input"
          clearable
        />
      </el-form-item>
      <el-form-item prop="packageCode">
        <el-input
          v-model="dataForm.packageCode"
          placeholder="套餐编号"
          class="input"
          maxlength="20"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="dataForm.packageType"
          class="input"
          placeholder="类别"
          clearable
        >
          <el-option label="父母体检" value="父母体检" />
          <el-option label="入职体检" value="入职体检" />
          <el-option label="职场白领" value="职场白领" />
          <el-option label="个人高端" value="个人高端" />
          <el-option label="中青年体检" value="中青年体检" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="dataForm.categoryId"
          class="input"
          placeholder="展示区"
          clearable
        >
          <el-option label="活动专区" value="1" />
          <el-option label="热卖套餐" value="2" />
          <el-option label="新品推荐" value="3" />
          <el-option label="孝敬父母" value="4" />
          <el-option label="白领精英" value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchHandle()">查询</el-button>
        <el-button
          type="primary"
          :disabled="!isAuth(['ROOT', 'GOODS:INSERT'])"
          @click="addHandle()"
        >
          新增
        </el-button>
        <el-button
          type="danger"
          :disabled="!isAuth(['ROOT', 'GOODS:DELETE'])"
          @click="deleteHandle()"
        >
          批量删除
        </el-button>
      </el-form-item>
      <el-form-item class="mold">
        <el-radio-group v-model="dataForm.statusLabel" @change="searchHandle()">
          <el-radio-button label="全部"></el-radio-button>
          <el-radio-button label="已上架"></el-radio-button>
          <el-radio-button label="已下架"></el-radio-button>
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
        prop="packageName"
        header-align="left"
        align="left"
        min-width="250"
        label="套餐名称"
      />
      <el-table-column
        prop="packageCode"
        header-align="left"
        align="left"
        min-width="130"
        label="套餐编号"
      />

      <el-table-column
        header-align="center"
        align="center"
        min-width="80"
        label="现价"
      >
        <!--如果你要自定义单元格中的内容，就需要单独编写下面的 template 标签-->
        <template #default="scope">
          <span>￥{{ scope.row.currentPrice }}</span>
        </template>
      </el-table-column>

      <el-table-column
        header-align="center"
        align="center"
        min-width="100"
        label="原价"
      >
        <template #default="scope">
          <span>￥{{ scope.row.originalPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="ruleName"
        header-align="center"
        align="center"
        min-width="100"
        label="促销方案"
      />
      <el-table-column
        prop="salesVolume"
        header-align="center"
        align="center"
        min-width="100"
        label="销量"
      />
      <el-table-column
        prop="packageType"
        header-align="center"
        align="center"
        min-width="100"
        label="类型"
      />
      <el-table-column
        header-align="center"
        align="center"
        min-width="100"
        label="体检内容"
      >
        <template #default="scope">
          <span
            :class="scope.row.hasDocument ? 'link-blue' : 'link-red'"
            @click="documentHandle(scope.row.id, scope.row.hasDocument)"
          >
            {{ scope.row.hasDocument ? "有文档" : "无文档" }}
          </span>
        </template>
      </el-table-column>
      <el-table-column
        prop="status"
        header-align="center"
        align="center"
        min-width="80"
        label="状态"
      >
        <template #default="scope">
          <!-- v-model="scope.row.status" 为true表示上架，false表示下架 -->
          <!-- active-text="上架" 开启时显示上架-->
          <!-- inactive-text="下架" 关闭时显示下架-->
          <!-- style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949;" 开启时绿色，关闭时红色-->
          <!-- :disabled="!scope.row.hasDocument" 只有当 hasDocument 为 true 时开关才可用-->
          <!-- inline-prompt 控制文本显示的位置和方式，这个配置表示文本显示在开关的内部，如果不写它，文本显示在开关的外面-->
          <el-switch
            v-model="scope.row.status"
            inline-prompt
            style="
              --el-switch-on-color: #13ce66;
              --el-switch-off-color: #ff4949;
            "
            active-text="上架"
            inactive-text="下架"
            :disabled="!scope.row.hasDocument"
            @change="changeSwitchHandle(scope.row.id, scope.row.status)"
          />
        </template>
      </el-table-column>
      <el-table-column
        header-align="center"
        align="center"
        width="150"
        label="操作"
      >
        <template #default="scope">
          <el-button
            type="text"
            :disabled="!scope.row.status"
            @click="viewHandle(scope.row.id)"
          >
            预览
          </el-button>
          <el-button
            type="text"
            v-if="isAuth(['ROOT', 'GOODS:UPDATE'])"
            :disabled="scope.row.status"
            @click="updateHandle(scope.row.id)"
          >
            修改
          </el-button>
          <!-- 什么情况下删除按钮可用？当销量为0，并且已下架，可以删除。 -->
          <el-button
            type="text"
            v-if="isAuth(['ROOT', 'GOODS:DELETE'])"
            :disabled="scope.row.salesVolume > 0 || scope.row.status"
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
    :title="!goodsDialog.dataForm.id ? '新增' : '修改'"
    v-if="isAuth(['ROOT', 'GOODS:INSERT', 'GOODS:UPDATE'])"
    :close-on-click-modal="false"
    v-model="goodsDialog.visible"
    width="750px"
  >
    <el-form
      :model="goodsDialog.dataForm"
      ref="dialogForm"
      :rules="goodsDialog.dataRule"
      label-width="80px"
    >
      <el-form-item label="套餐名称" prop="packageName">
        <el-input
          v-model="goodsDialog.dataForm.packageName"
          maxlength="50"
          clearable
        />
      </el-form-item>
      <el-form-item label="套餐编号" prop="packageCode">
        <el-input
          v-model="goodsDialog.dataForm.packageCode"
          maxlength="20"
          clearable
        />
      </el-form-item>
      <el-form-item label="简介信息" prop="description">
        <el-input
          type="textarea"
          v-model="goodsDialog.dataForm.description"
          :rows="4"
          maxlength="200"
          clearable
        />
      </el-form-item>
      <el-form-item label="套餐原价" prop="originalPrice">
        <el-input
          v-model="goodsDialog.dataForm.originalPrice"
          placeholder="输入原价"
          class="price"
          maxlength="20"
          clearable
        >
          <template #append> 元 </template>
        </el-input>
        <span class="desc">提示：价格精确到分（小数点后两位）</span>
      </el-form-item>
      <el-form-item label="套餐现价" prop="currentPrice">
        <el-input
          v-model="goodsDialog.dataForm.currentPrice"
          placeholder="输入现价"
          class="price"
          maxlength="20"
          clearable
        >
          <template #append> 元 </template>
        </el-input>
        <span class="desc">提示：价格精确到分（小数点后两位）</span>
      </el-form-item>
      <el-form-item label="折扣列表">
        <el-select
          v-model="goodsDialog.dataForm.promotionId"
          placeholder="选择折扣信息"
          clearable
        >
          <el-option
            :label="one.ruleName"
            :value="one.ruleId"
            v-for="one in goodsDialog.ruleList"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="封面图片" prop="coverImage">
        <!--EP组件库底层自动发送请求，不需要我们发请求：用户点击 → 弹出文件选择框 → 选择文件 → 自动发送POST请求-->
        <!-- :action="goodsDialog.upload.action" 指定上传时的请求路径 -->
        <!-- :headers="goodsDialog.upload.headers" 上传请求头（通常用于身份验证，如token） -->
        <!-- :data="goodsDialog.upload.data" 上传附加数据（如业务参数） -->
        <!-- :show-file-list="false" 不显示文件列表，适用于单文件上传，如果显示的话：每个文件会显示文件名、大小、状态 -->
        <!-- accept=".jpg,.jpeg" 只接受jpg和jpeg格式的图片 -->
        <!-- :on-success="imageUploadSuccess" 上传成功回调函数 -->
        <!-- :before-upload="imageBeforeUpload" 上传前校验函数（可用于文件类型、大小校验） -->
        <!-- :on-error="imageUploadError" 上传失败回调函数 -->
        <el-upload
          class="image-uploader"
          :action="goodsDialog.upload.action"
          :headers="goodsDialog.upload.headers"
          :data="goodsDialog.upload.data"
          :show-file-list="false"
          accept=".jpg,.jpeg,.png"
          :on-success="imageUploadSuccess"
          :before-upload="imageBeforeUpload"
          :on-error="imageUploadError"
        >
          <!-- 如果已上传图片，显示预览图 -->
          <img
            v-if="goodsDialog.imageUrl"
            :src="goodsDialog.imageUrl"
            class="image"
          />
          <!-- 未上传图片时显示上传图标 -->
          <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="套餐类别" prop="packageType">
        <el-select
          v-model="goodsDialog.dataForm.packageType"
          placeholder="检查类别"
          clearable
        >
          <el-option label="父母体检" value="父母体检" />
          <el-option label="入职体检" value="入职体检" />
          <el-option label="职场白领" value="职场白领" />
          <el-option label="个人高端" value="个人高端" />
          <el-option label="中青年体检" value="中青年体检" />
        </el-select>
      </el-form-item>
      <el-form-item label="特征标签">
        <div class="tag-row">
          <el-input
            class="tag-input"
            v-model="goodsDialog.newTag"
            clearable
            @keyup.enter="enterTag"
          />
          <span class="desc">提示：输入标签后按回车键</span>
        </div>
        <div class="tags">
          <el-tag
            v-for="one in goodsDialog.dataForm.tags"
            closable
            :disable-transitions="false"
            @close="closeTag(one)"
          >
            {{ one }}
          </el-tag>
        </div>
      </el-form-item>
      <el-form-item label="展示区" prop="categoryId">
        <el-select
          v-model="goodsDialog.dataForm.categoryId"
          placeholder="选择展示区"
          clearable
        >
          <el-option label="活动专区" value="1" />
          <el-option label="热卖套餐" value="2" />
          <el-option label="新品推荐" value="3" />
          <el-option label="孝敬父母" value="4" />
          <el-option label="白领精英" value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="体检内容">
        <!-- 
        商品体检项目动态表单行
        功能：循环渲染体检项目配置，每行包含类别选择、项目名称、内容和删除操作
        结构说明：
          - :gutter="10" 列间距10像素(栅格列之间的间距)
          - v-for 遍历goodsDialog.item数组，动态生成多个配置行
          - :key="$index" 使用索引作为key
        
        el-col 列布局(EP的栅格布局，EP规定一行共24个栅格：把一行等分成24份)：
          - 第1列(span=6，6就是表示占6份)：检查类别下拉选择，包含科室/实验室/医技/其他检查
          - 第2列(span=6)：体检项目名称输入框，最多50字符
          - 第3列(span=11)：体检内容详细说明输入框，最多500字符  
          - 第4列(span=1)：删除按钮，点击移除当前行配置
      -->
        <el-row
          :gutter="10"
          class="item-row"
          v-for="(one, $index) in goodsDialog.item"
          :key="$index"
        >
          <el-col :span="6">
            <el-select v-model="one.type" placeholder="检查类别" clearable>
              <el-option label="科室检查" value="科室检查" />
              <el-option label="实验室检查" value="实验室检查" />
              <el-option label="医技检查" value="医技检查" />
              <el-option label="其他检查" value="其他检查" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="one.title"
              placeholder="体检项目"
              maxlength="50"
              clearable
            />
          </el-col>
          <el-col :span="11">
            <el-input
              v-model="one.content"
              placeholder="体检内容"
              maxlength="500"
              clearable
            />
          </el-col>
          <el-col :span="1">
            <el-button
              type="primary"
              :icon="Delete"
              @click="deleteItem($index)"
            />
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="danger" @click="addItem">添加项目</el-button>
        <el-button @click="goodsDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="dataFormSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>

  <el-dialog
    title="提示信息"
    v-if="isAuth(['ROOT', 'GOODS:INSERT', 'GOODS:UPDATE'])"
    v-model="documentDialog.visible"
    width="350px"
  >
    <div class="message-content">
      <el-icon :size="18" class="icon">
        <WarningFilled />
      </el-icon>
      <p>
        请您选择【上传】或者【下载】体检内容文档？如果未上传体检内容文档，则体检套餐将无法上架。
      </p>
    </div>
    <!-- 
    1. 上传功能：
       - 使用el-upload组件实现Excel文件上传
       - 上传地址：documentDialog.upload.action
       - 附加数据：documentDialog.data
       - 请求头：documentDialog.upload.headers（通常包含认证信息）
       - 只接受.xlsx格式文件
       - 上传前校验：documentBeforeUpload（校验文件格式、大小等）
       - 上传成功回调：documentUploadSuccess（处理上传结果）
       - 上传失败回调：documentUploadError（错误处理）
       - 显示为带Upload图标的成功样式按钮
  
    2. 下载功能：
       - 使用el-button实现文件下载
       - 带Download图标的主样式按钮
       - 禁用条件：!documentDialog.data.hasDocument（未通过审核时禁用下载）
       - 点击事件：documentDownloadHandle（触发下载操作）
  -->
    <template #footer>
      <span class="document-dialog-footer">
        <el-upload
          :action="uploadAction"
          :show-file-list="false"
          :headers="documentDialog.upload.headers"
          accept=".xlsx,.xls"
          :before-upload="documentBeforeUpload"
          :on-success="documentUploadSuccess"
          :on-error="documentUploadError"
        >
          <el-button type="success" :icon="Upload" class="uploadBtn"
            >上传</el-button
          >
        </el-upload>
        <el-button
          type="primary"
          :icon="Download"
          class="downloadBtn"
          :disabled="!documentDialog.data.hasDocument"
          @click="documentDownloadHandle"
        >
          下载
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {
  reactive,
  getCurrentInstance,
  ref,
  onMounted,
  inject,
  computed,
} from "vue";
//引入ElementPlus组件库中的四个图标，要给按钮设置图标
import { Delete, Download, Upload, Plus } from "@element-plus/icons-vue";
import router from "../../router/index";
import { isAuth } from "@/utils/auth";
import {
  ElMessage,
  ElMessageBox,
  type UploadFile,
  type UploadFiles,
  type UploadRawFile,
} from "element-plus";
import axios from "axios";
import { nextTick } from "vue";

// 注入全局的一个变量
const minioUrl = inject("minioUrl");

interface DataForm {
  packageName: null | string;
  packageCode: null | string;
  packageType: null | string;
  categoryId: null | number;
  statusLabel: number | string;
  status: null | boolean;
}

const dataForm: DataForm = reactive({
  packageName: null,
  packageCode: null,
  packageType: null,
  categoryId: null,
  statusLabel: "全部",
  status: null,
});

const dataRule = reactive({
  packageName: [
    {
      pattern: "^[a-zA-Z0-9\u4e00-\u9fa5]{1,50}$",
      message: "关键字内容不正确",
    },
  ],
  packageCode: [
    { min: 6, message: "编号不能少于6个字符" },
    { pattern: "^[a-zA-Z0-9]{6,20}$", message: "编号格式错误" },
  ],
});

const addHandle = async () => {
  // 弹出新增的模态窗口
  // 重置表单
  dialogForm.value?.resetFields();
  // 清空数据（有一些数据清除不了，需要手动清除。）
  goodsDialog.dataForm.id = null;
  goodsDialog.dataForm.promotionId = null;
  goodsDialog.dataForm.coverImage = null;
  goodsDialog.newTag = null;
  goodsDialog.dataForm.tags = [];
  goodsDialog.imageUrl = null;
  goodsDialog.item = [{}];

  // 加载规则列表
  await loadRuleList();

  // 显示弹窗
  goodsDialog.visible = true;
};

const deleteHandle = async (id?: number) => {
  const ids = id ? [id] : data.selections.map((row: any) => row.id);
  if (ids.length === 0) {
    ElMessage.warning("请选择要删除的记录");
    return;
  }

  // 发送ajax请求删除数据
  try {
    // 让用户确认是否要删除
    await ElMessageBox.confirm("您确定要删除套餐吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    // 走到这里说明用户点击了确定，发送ajax请求删除
    const response = await axios.delete("/api/mis/goods/batch", {
      headers: { satoken: localStorage.getItem("token") },
      data: { ids },
    });

    if (response.data.code === 200) {
      ElMessage.success({
        message: "删除成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.error("删除失败");
    }
  } catch {
    // 用户点击了取消，或者出现异常，静默处理。
  }
};

const form = ref();

const searchHandle = async () => {
  // 用户点击查询/全部/已上架/已下架都走这个函数
  // 校验表单是否合法
  try {
    await form.value.validate();
  } catch {
    return;
  }
  // 如果合法则加载分页数据
  data.pageIndex = 1;
  loadPageList();
};

const data = reactive({
  dataList: [],
  pageIndex: 1,
  pageSize: 10,
  totalCount: 0,
  loading: false,
  selections: [],
});

const selectionChangeHandle = (vals: any) => {
  data.selections = vals;
};

const selectable = (row: any) => {
  // 销量是0，并且下架了。
  return row.salesVolume === 0 && !row.status;
};

const documentHandle = (id: any, hasDocument: any) => {
  documentDialog.data.hasDocument = hasDocument;
  documentDialog.data.id = id;
  documentDialog.visible = true;
};

const changeSwitchHandle = async (id: number, status: number) => {
  try {
    const response = await axios.put(
      `/api/mis/goods/${id}/status/${status}`,
      null,
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );
    if (response.data.code === 200) {
      ElMessage({
        type: "success",
        message: "操作成功",
        duration: 1200,
        onClose: () => {
          loadPageList();
        },
      });
    } else {
      ElMessage.error(response.data.message || "操作失败");
    }
  } catch {
    ElMessage.error("网络异常，稍后再试");
  }
};

// 实现路由跳转
const viewHandle = (id: number) => {
  // 编程式路由导航，参数通过params传递。
  router.push({
    name: "FrontGoods",
    params: { id: id },
  });

  // 封装一个路由对象
  // const route = router.resolve({
  //   name: "FrontGoods",
  //   params: { id: id },
  // });

  // 以新的页面打开（_blank代表新窗口）
  // window.open(route.href, "_blank");
};

const updateHandle = async (id: number) => {
  // 1.先将弹窗显示出来
  goodsDialog.visible = true;

  // 2.等待这个窗口渲染完毕
  await nextTick();

  // 重置表单的数据以及校验状态（清空表单，清空表单上的曾经的校验失败的提示信息）
  dialogForm.value.resetFields(); // prop相关的全部重置了。非prop还没有重置

  // 3. 清空页面上仍然残留的数据
  goodsDialog.dataForm.id = id; // 修改的操作（id为null表示新增）
  goodsDialog.dataForm.coverImage = null;
  goodsDialog.dataForm.tags = null;
  goodsDialog.dataForm.promotionId = null;
  goodsDialog.imageUrl = null;
  goodsDialog.item = [{}];
  goodsDialog.newTag = null;

  // 4. 重新回填页面
  try {
    // 并行加载数据（两个异步的ajax请求加载数据）
    await Promise.all([loadRuleList(), loadGoodsDetail(id)]);
  } catch {
    ElMessage.error("数据加载失败");
  }
};

const loadGoodsDetail = async (id: number) => {
  try {
    // 发送get请求，根据id查询套餐的信息。
    const response = await axios.get(`/api/mis/goods/${id}`, {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    const result = response.data.data.goods;

    // 回填所有数据
    goodsDialog.dataForm.packageCode = result.packageCode;
    goodsDialog.dataForm.packageName = result.packageName;
    goodsDialog.dataForm.description = result.description;
    goodsDialog.dataForm.coverImage = result.coverImage;
    goodsDialog.dataForm.originalPrice = result.originalPrice;
    goodsDialog.dataForm.currentPrice = result.currentPrice;
    goodsDialog.dataForm.packageType = result.packageType;
    goodsDialog.dataForm.tags = result.tags || [];
    goodsDialog.dataForm.categoryId = result.categoryId
      ? String(result.categoryId)
      : null;
    goodsDialog.imageUrl = `${minioUrl}/${result.coverImage}`;
    goodsDialog.dataForm.promotionId = result.ruleId || null;

    // 处理详细的体检项目。
    // 将后端返回的体检项目一个一个取出来放到 item 数组当中。
    // 定义一个键值对映射对象。
    const typeMap = {
      departmentExam: "科室检查",
      labExam: "实验室检查",
      medicalExam: "医技检查",
      otherExam: "其他检查",
    };
    const items: any[] = [];
    // Object.entries(typeMap) 的执行结果
    // [["labExam", "科室检查"],[...],[],[]]
    for (const [key, label] of Object.entries(typeMap)) {
      const exams = result[key]; // result.labExam
      if (Array.isArray(exams) && exams.length > 0) {
        // 有体检项目.
        for (const one of exams) {
          items.push({
            type: label,
            title: one.title,
            content: one.content,
          });
        }
      }
    }

    goodsDialog.item = items.length ? items : [{}];
  } catch {
    ElMessage.error("网络异常，稍后再试");
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

const goodsDialog: any = reactive({
  visible: false,
  newTag: null,
  item: [{}],
  imageUrl: null, // 这个路径必须是MinIO服务器上图片的绝对路径，不能是相对路径。
  ruleList: [],
  dataForm: {
    id: null,
    packageName: null,
    packageCode: null,
    description: null,
    originalPrice: null,
    currentPrice: null,
    promotionId: null,
    coverImage: null, // 这个路径存储的是图片在minio服务器上存储的相对路径。
    packageType: null,
    tags: [],
    categoryId: null,
  },
  upload: {
    action: "/api/mis/goods/images",
    headers: {
      satoken: localStorage.getItem("token"),
    },
    data: {
      id: null,
    },
  },

  dataRule: {
    packageName: [
      { required: true, message: "名称不能为空" },
      { min: 2, message: "名称不能少于2个字符" },
      { pattern: "^[a-zA-Z0-9\u4e00-\u9fa5]{2,50}$", message: "名称格式错误" },
    ],
    packageCode: [
      { required: true, message: "编号不能为空" },
      { min: 6, message: "编号不能少于6个字符" },
      { pattern: "^[a-zA-Z0-9]{6,20}$", message: "编号格式错误" },
    ],
    description: [{ required: true, message: "简介信息不能为空" }],
    originalPrice: [
      {
        required: true,
        message: "价格不能为空",
      },
      {
        pattern:
          "(^[1-9]([0-9]+)?(\\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\\.[0-9]([0-9])?$)",
        message: "价格不正确",
      },
    ],
    currentPrice: [
      {
        required: true,
        message: "价格不能为空",
      },
      {
        pattern:
          "(^[1-9]([0-9]+)?(\\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\\.[0-9]([0-9])?$)",
        message: "价格不正确",
      },
    ],
    coverImage: [
      {
        required: true,
        message: "没有上传封面图片",
      },
    ],
    packageType: [
      {
        required: true,
        message: "没有选择套餐类别",
      },
    ],
  },
});

// file是EP组件库自动给我们传过来的。
const imageBeforeUpload = (file: UploadRawFile) => {
  const size = file.size / 1024 / 1024;
  if (size > 5) {
    ElMessage.error("图片大小不能超过5MB");
    // 终止文件上传。
    return false;
  }
  // 返回true表示文件可以继续上传。
  return true;
};

// 文件上传成功之后的回调
// 这个回调函数在文件上传成功之后自动被调用，这是EP组件库自动完成的，我们不需要管。
// 这个回调函数会自动接收三个参数。
// 重点了解response参数即可：response是用来专门接收服务器端响应的。
const imageUploadSuccess = (
  response: any,
  uploadFile: UploadFile,
  uploadFiles: UploadFiles,
) => {
  if (response.code === 200) {
    // 获取返回的路径
    const path = response.data.result;
    console.log(path);
    // 重点是给两个属性赋值
    // 1.一个属性是相对路径，要提交到数据库的。
    goodsDialog.dataForm.coverImage = path;
    // 2.一个属性是绝对路径，要在页面上显示的。
    goodsDialog.imageUrl = `${minioUrl}/${path}`;
    console.log(goodsDialog.imageUrl);
  }
};

// 图片上传失败之后会自动执行这个函数，EP会自动将错误传递给e参数。
const imageUploadError = (e: any) => {
  ElMessage.error("图片上传失败");
  console.log(e); // 便于调试
};

const enterTag = () => {
  const newTag = goodsDialog.newTag?.trim();
  if (!newTag) return;
  if (goodsDialog.dataForm.tags.includes(newTag)) {
    ElMessage.warning("不能重复添加标签");
    return;
  }
  goodsDialog.dataForm.tags.push(newTag);
  goodsDialog.newTag = null;
};
const closeTag = (tag: string) => {
  // 获取元素在数组中的下标
  const index = goodsDialog.dataForm.tags.indexOf(tag);
  if (index > -1) {
    // 元素是存在的
    goodsDialog.dataForm.tags.splice(index, 1);
  }
};

const deleteItem = (index: string | number) => {
  if (goodsDialog.item.length === 1) {
    ElMessage.warning("至少保留一个体检项");
  } else {
    goodsDialog.item.splice(index, 1);
  }
};
const addItem = () => {
  goodsDialog.item.push({});
};

interface ExamItem {
  title: string;
  content: string;
}

const dialogForm = ref();
const dataFormSubmit = async () => {
  // 表单校验
  try {
    await dialogForm.value.validate();
  } catch {
    return;
  }
  // item数组中的数据收集到4个数组当中。
  // 准备4个数组。
  let departmentExam: ExamItem[] = [];
  let labExam: ExamItem[] = [];
  let medicalExam: ExamItem[] = [];
  let otherExam: ExamItem[] = [];

  // 遍历item数组
  for (const item of goodsDialog.item) {
    // 对象解构语法
    const { type, title, content } = item;
    if (!type) {
      ElMessage.error("检查类别不能为空");
      return;
    }
    if (!title?.trim()) {
      ElMessage.error("体检项目不能为空");
      return;
    }
    if (!content?.trim()) {
      ElMessage.error("体检内容不能为空");
      return;
    }

    // 创建体检条目对象
    const examItem: ExamItem = {
      title: title.trim(),
      content: content.trim(),
    };
    // 判断type，将体检条目对象放到对应的数组当中
    if (type === "科室检查") departmentExam.push(examItem);
    else if (type === "实验室检查") labExam.push(examItem);
    else if (type === "医技检查") medicalExam.push(examItem);
    else if (type === "其他检查") otherExam.push(examItem);
  }

  // 发送ajax请求，实现保存操作
  // 准备数据
  const { dataForm } = goodsDialog;
  const sendData = {
    id: dataForm.id,
    packageCode: dataForm.packageCode,
    packageName: dataForm.packageName,
    description: dataForm.description,
    coverImage: dataForm.coverImage,
    originalPrice: dataForm.originalPrice,
    currentPrice: dataForm.currentPrice,
    packageType: dataForm.packageType,
    tags: dataForm.tags,
    categoryId: dataForm.categoryId,
    promotionId: dataForm.promotionId,
    departmentExam: departmentExam.length ? departmentExam : null,
    labExam: labExam.length ? labExam : null,
    medicalExam: medicalExam.length ? medicalExam : null,
    otherExam: otherExam.length ? otherExam : null,
  };

  // 发送请求
  try {
    const method = !!dataForm.id ? "put" : "post";
    const response = await axios[method]("/api/mis/goods", sendData, {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    if (response.data.code === 200) {
      ElMessage.success("操作成功");
      goodsDialog.visible = false;
      loadPageList();
    } else {
      ElMessage.error(response.data.msg || "操作失败");
    }
  } catch {
    ElMessage.error("网络异常，稍后再试");
  }
};

const documentDialog = reactive({
  visible: false, // 记得调试完改成false
  upload: {
    headers: {
      satoken: localStorage.getItem("token"),
    },
  },
  data: {
    id: null,
    hasDocument: null,
  },
});

// 重点的计算属性：这个计算属性什么时候会重新计算？
// 该计算属性关联的 documentDialog.data.id 是一个响应式数据，只要它发生变化，计算属性会重新计算。
const uploadAction = computed(() => {
  return `/api/mis/goods/${documentDialog.data.id}/examItems`;
});

const documentBeforeUpload = (file: UploadRawFile) => {
  const fileSize = file.size / 1024 / 1024;
  if (fileSize > 20) {
    ElMessage.error("文件不能超过20MB");
    return false;
  }
  return true;
};
const documentUploadSuccess = (response: any) => {
  if (response.code === 200) {
    documentDialog.visible = false;
    ElMessage.success({
      message: "文件上传成功",
      duration: 1200,
      onClose: () => loadPageList(),
    });
  } else {
    ElMessage.error(response.msg || "上传失败");
  }
};

const documentUploadError = (error: any) => {
  console.log("文件上传失败：", error);
  ElMessage.error("文件上传失败，请稍后再试");
};

// 基于ajax请求的下载
const documentDownloadHandle = async () => {
  try {
    const res = await axios.get(
      `/api/mis/goods/${documentDialog.data.id}/examItems`,
      {
        headers: { satoken: localStorage.getItem("token") },
        responseType: "blob",
      },
    );

    // 判断一下响应的内容类型，如果响应的内容类型是一个json字符串，那就是下载失败了。
    // 获取响应头当中的内容类型
    const contentType = String(res.headers["content-type"]);
    if (contentType.indexOf("application/json") !== -1) {
      // 条件成立的话，说明响应的内容类型是：application/json
      // 下载失败了
      const text = await res.data.text();
      ElMessage.error(JSON.parse(text).msg || "下载失败");
      return;
    }

    // 生成临时的下载链接
    const url = URL.createObjectURL(new Blob([res.data]));
    // 创建一个隐藏的a标签
    const a = document.createElement("a");
    // 设置a标签的href属性
    a.href = url;
    // 指定下载的文件名
    a.download = `${documentDialog.data.id}.xlsx`;
    // 模拟鼠标单击超链接触发下载
    a.click();
    // 释放临时的内存空间
    URL.revokeObjectURL(url);
    // 隐藏modal窗口。
    documentDialog.visible = false;
  } catch {
    ElMessage.error("网络异常，稍后再试");
  }
};

// 封装一个分页查询的函数
const loadPageList = async () => {
  // 显示加载进度条
  data.loading = true;

  // 将 statusLabel 转换成 status
  if (dataForm.statusLabel === "全部") {
    dataForm.status = null;
  } else if (dataForm.statusLabel === "已上架") {
    dataForm.status = true;
  } else if (dataForm.statusLabel === "已下架") {
    dataForm.status = false;
  } else {
    ElMessage.error("套餐状态错误");
  }

  // 准备数据和配置
  const configAndData: any = {
    headers: {
      // 自己研究一下axios中拦截器怎么写（专门拦截请求，在每一次请求前设置请求头）
      satoken: localStorage.getItem("token"),
    },
    params: {
      pageNo: data.pageIndex,
      pageSize: data.pageSize,
      packageName: dataForm.packageName,
      packageCode: dataForm.packageCode,
      packageType: dataForm.packageType,
      categoryId: dataForm.categoryId,
      status: dataForm.status,
    },
  };
  try {
    // 发送请求
    const response = await axios.get("/api/mis/goods/list", configAndData);
    if (response.data.code === 200) {
      // 回填数据
      const result = response.data.data.pageResult;
      for (let goods of result.records) {
        goods.status = goods.status === 1;
      }
      data.dataList = result.records;
      data.totalCount = result.total;
    } else {
      ElMessage.error(response.data.msg || "数据加载失败");
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
  // 隐藏加载进度条
  data.loading = false;
};

loadPageList();

// 加载规则列表
const loadRuleList = async () => {
  try {
    const response = await axios.get("/api/mis/rule/list", {
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });
    goodsDialog.ruleList = response.data.data.ruleList;
  } catch {
    ElMessage.error("加载规则列表失败");
  }
};
</script>

<style lang="less" scoped>
@import url("goods.less");
</style>
