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
          :clearable="false"
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
        <el-button type="success" @click="checkinHandle()">体检签到</el-button>
        <el-button type="danger" @click="finishHandle()">完成体检</el-button>
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
            :disabled="!isAuth(['ROOT', 'APPOINTMENT:SELECT'])"
            @click="guidanceHandle(scope.row.id)"
          >
            导引单
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
    title="体检签到"
    v-if="isAuth(['ROOT', 'APPOINTMENT:UPDATE'])"
    :close-on-click-modal="false"
    v-model="checkinDialog.visible"
    width="500px"
    :before-close="closeHandle"
  >
    <div class="card-info">
      <div class="left">
        <el-form
          :model="checkinDialog.dataForm"
          ref="dialogForm"
          label-width="60px"
        >
          <el-form-item label="姓名">
            <el-input v-model="checkinDialog.dataForm.name" disabled />
          </el-form-item>
          <el-form-item label="性别">
            <el-input v-model="checkinDialog.dataForm.sex" disabled />
          </el-form-item>
          <el-form-item label="身份证">
            <el-input v-model="checkinDialog.dataForm.pid" disabled />
          </el-form-item>
        </el-form>
      </div>
      <div class="right">
        <!-- 这两个控件是排他的，通过photo_1的状态来决定显示谁。-->
        <img
          :src="checkinDialog.dataForm.photo_1"
          class="photo"
          v-if="checkinDialog.dataForm.photo_1 != null"
        />
        <div class="empty" v-if="checkinDialog.dataForm.photo_1 == null">
          <el-icon :size="60" class="icon"><Avatar /></el-icon>
        </div>
      </div>
    </div>

    <!-- 
  控件排他显示实现原理：
  通过 v-show 控制三个视觉元素的显隐，确保同一时间只有一个元素可见
  showEmpty: 显示操作指引
  showVideo: 显示摄像头实时画面  
  showPhoto: 显示拍摄的照片
  这三个状态在业务逻辑中互斥，实现排他效果
  -->
    <div class="photo-container">
      <!-- 初始状态：显示操作指引 -->
      <div class="empty" v-show="checkinDialog.showEmpty">
        <SvgIcon name="camera" class="camera" />
        <ol>
          <li>
            完成体检人身份证刷卡验证后，系统将自动启用摄像头进行人脸采集。
          </li>
          <li>
            拍摄画面中须确保仅包含单一人脸特征，如检测到多张人脸将触发系统告警。
          </li>
          <li>
            要求采集正面免冠人脸图像，禁止佩戴墨镜、口罩等影响面部识别的遮挡物。
          </li>
        </ol>
      </div>

      <!-- 拍照状态：显示摄像头实时画面 -->
      <!--video标签：HTML5原生视频播放元素，用于在网页中嵌入和播放视频内容或显示摄像头实时流。-->
      <video id="video" autoplay v-show="checkinDialog.showVideo"></video>

      <!-- 完成状态：显示拍摄的照片 -->
      <!--canvas标签：HTML5原生画布元素，提供JavaScript绘图API，用于在网页上动态生成和操作图像图形-->
      <canvas
        id="photo"
        width="460"
        height="345"
        v-show="checkinDialog.showPhoto"
      >
      </canvas>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <!-- 
          按钮状态控制：
          - 拍照按钮在showEmpty时为禁用状态（需要先刷身份证）
          - 签到按钮需要pid和photo_2同时存在才可用
          通过disabled属性实现功能逻辑的排他控制
          -->
        <el-button
          type="primary"
          :icon="checkinDialog.btnIcon"
          :disabled="checkinDialog.showEmpty"
          @click="takePhotoHandle"
        >
          {{ checkinDialog.btnText }}
        </el-button>
        <el-button @click="closeHandle">取消</el-button>
        <el-button
          type="success"
          :disabled="
            checkinDialog.dataForm.pid == null ||
            checkinDialog.dataForm.photo_2 == null
          "
          @click="dataFormSubmit"
        >
          签到
        </el-button>
      </span>
    </template>
  </el-dialog>

  <el-dialog
    title="体检导引单"
    v-if="isAuth(['ROOT', 'APPOINTMENT:SELECT'])"
    v-model="guidanceDialog.visible"
    width="800px"
  >
    <div class="guidance" id="pdfDom" :name="guidanceDialog.name">
      <h2 class="title">美年大健康体检导引单</h2>
      <div class="summary">
        <table class="base-info">
          <tr>
            <td class="label">姓名:</td>
            <td class="value">{{ guidanceDialog.name }}</td>
            <td class="label">性别:</td>
            <td class="value">{{ guidanceDialog.sex }}</td>
            <td class="label">年龄:</td>
            <td class="value">{{ guidanceDialog.age }}</td>
          </tr>
          <tr>
            <td class="label">身份证:</td>
            <td class="value">{{ guidanceDialog.pid }}</td>
            <td class="label">电话:</td>
            <td class="value">{{ guidanceDialog.tel }}</td>
            <td class="label">日期:</td>
            <td class="value">{{ guidanceDialog.date }}</td>
          </tr>
          <tr>
            <td class="label">公司:</td>
            <td colspan="5" class="value">
              {{ guidanceDialog.company }}
            </td>
          </tr>
        </table>
        <!-- 以体检流水号做二维码 -->
        <img class="qrcode" :src="guidanceDialog.qrCodeBase64" />
      </div>
      <table class="checkup">
        <tr>
          <th>序号</th>
          <th align="left">检查地点</th>
          <th align="left">检查项目</th>
          <th>体检医生</th>
        </tr>
        <tr v-for="(one, index) in guidanceDialog.checkup">
          <td align="center">{{ index + 1 }}</td>
          <td>{{ one.place }}</td>
          <td>{{ one.name }}</td>
          <td></td>
        </tr>
      </table>
      <div class="desc">
        <p>
          请注意：体检结束10天后，即可在美年大健康网站（http://www.meinianhealth.com）查询到您的体检报告。之后的5个工作日之内，您将收到本体检中心邮寄的体检报告，请留意查收！
        </p>
        <ul>
          <li>
            <el-icon><PhoneFilled /></el-icon>
            <span>体检咨询：010-12345678</span>
          </li>
          <li>
            <el-icon><PhoneFilled /></el-icon>
            <span>体检咨询：010-12345679</span>
          </li>
          <li>
            <el-icon><PhoneFilled /></el-icon>
            <span>体检咨询：010-12345670</span>
          </li>
        </ul>
      </div>
    </div>
    <div class="operate">
      <el-button
        type="primary"
        size="large"
        :icon="Document"
        @click="getPdf('pdfDom')"
      >
        下载导引单
      </el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { getPdf } from "@/utils/pdf";
import { reactive, ref, onMounted } from "vue";
import { ElMessage, ElMessageBox, ElNotification } from "element-plus";
import {
  Camera,
  RefreshRight,
  Document,
  Avatar,
} from "@element-plus/icons-vue";
import router from "../../router/index";
import { dayjs } from "element-plus";
import isBetween from "dayjs/plugin/isBetween";
import { isAuth } from "@/utils/auth";
import axios from "axios";

dayjs.extend(isBetween);

interface DataForm {
  patientName: null | string;
  phone: null | string;
  appointmentDate: string;
  statusLabel: string;
  status: null | number | undefined;
}

const dataForm: DataForm = reactive({
  patientName: null,
  phone: null,
  appointmentDate: dayjs().format("YYYY-MM-DD"),
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

const checkinHandle = () => {
  // 获取今天的日期
  const today = dayjs().format("YYYY-MM-DD");
  if (today !== dataForm.appointmentDate) {
    ElMessage.warning({
      message: `请将日期修改为${today}`,
      duration: 1200,
    });
  } else {
    // 重置数据
    const { dataForm } = checkinDialog;
    dataForm.name = null;
    dataForm.sex = null;
    dataForm.pid = null;
    dataForm.photo_1 = null;
    dataForm.photo_2 = null;

    // 三选一：默认empty被激活。
    checkinDialog.showEmpty = true;
    checkinDialog.showVideo = false;
    checkinDialog.showPhoto = false;

    // 显示弹窗
    checkinDialog.visible = true;
  }
};

// 完成体检
const finishHandle = async () => {
  try {
    const { value } = await ElMessageBox.prompt(
      "请输入体检单编号，结束本次体检",
      "提示信息",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputPlaceholder: "体检编号",
        type: "warning",
        inputPattern: /^[0-9a-zA-Z]{32}$/, // 指定文本框中输入的文本内容格式要求。
        inputErrorMessage: "体检编号不正确", // 如果格式错误有错误提示信息。
      },
    );
    // 发送ajax请求，完成体检
    const response = await axios.post(
      "/api/mis/appointment/finish",
      {
        appointmentNo: value,
        status: 3,
      },
      {
        headers: { satoken: localStorage.getItem("token") },
      },
    );

    const result = response.data.data.result;
    if (result) {
      ElMessage.success({
        message: "操作成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.error({
        message: "操作失败",
        duration: 1200,
      });
    }
  } catch (e) {
    console.log(e);
    if (e !== "cancel") {
      ElMessage.error({
        message: "网络异常，稍后再试",
        duration: 1200,
      });
    }
  }
};
const selectionChangeHandle = (rows: any) => {};
const selectable = (row: any) => {
  return true;
};

const guidanceHandle = async (id: number) => {
  try {
    const response = await axios.get("/api/mis/appointment/guidance-info", {
      params: { id },
      headers: { satoken: localStorage.getItem("token") },
    });

    if (response.data.code === 200) {
      const { result } = response.data.data;
      guidanceDialog.name = result.patientName;
      guidanceDialog.sex = result.gender;
      guidanceDialog.age = result.age;
      guidanceDialog.pid = result.idCardNo;
      guidanceDialog.tel = result.phone;
      guidanceDialog.date = result.appointmentDate;
      guidanceDialog.company = result.company || "无";
      guidanceDialog.checkup = result.checkupSet;
      guidanceDialog.qrCodeBase64 = result.qrCodeBase64;
      guidanceDialog.visible = true;
    } else {
      ElMessage.error(response.data.msg || "加载失败");
    }
  } catch (e) {
    console.log(e);
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

// 封装一个分页查询的函数
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
    dataForm.status = statusLabelMap[dataForm.statusLabel];
    const response = await axios.get("/api/mis/appointment/list", {
      params: {
        patientName: dataForm.patientName,
        phone: dataForm.phone,
        appointmentDate: dataForm.appointmentDate,
        status: dataForm.status,
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
      },
      headers: { satoken: localStorage.getItem("token") },
    });

    if (response.data.code === 200) {
      const { records, total } = response.data.data.pageResult;
      const statusMap: Record<string, string> = {
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
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  } finally {
    data.loading = false;
  }
};

// 页面打开时分页查询
loadPageList();

interface DataForm2 {
  name: null | string;
  sex: null | string;
  pid: null | string;
  photo_1: null | string;
  photo_2: null | string;
}

const checkinDialog = reactive({
  visible: false,
  btnText: "拍照",
  btnIcon: Camera,
  showEmpty: true,
  showVideo: false,
  showPhoto: false,
  streamTrack: null as any,
  dataForm: {
    name: null,
    sex: null,
    pid: null,
    photo_1: null,
    photo_2: null,
  } as DataForm2,
});

const closeHandle = () => {
  // 1. 停止摄像头（释放硬件资源）
  const video = document.querySelector("#video") as HTMLVideoElement;
  if (video) {
    video.pause(); // 停止播放
    video.srcObject = null; // 释放流引用
    video.load(); // 重置元素
  }
  if (checkinDialog.streamTrack) {
    checkinDialog.streamTrack.stop(); // 停止视频轨道
    checkinDialog.streamTrack = null; // 释放引用，帮助垃圾回收
  }

  // 2. 重置界面显示状态
  checkinDialog.showEmpty = true;
  checkinDialog.showVideo = false;
  checkinDialog.showPhoto = false;

  // 3. 重置按钮状态
  checkinDialog.btnText = "拍照";
  checkinDialog.btnIcon = Camera;

  // 4. 清空表单数据
  checkinDialog.dataForm.photo_2 = null;
  checkinDialog.dataForm.photo_1 = null;
  checkinDialog.dataForm.name = null;
  checkinDialog.dataForm.sex = null;
  checkinDialog.dataForm.pid = null;

  // 5. 关闭弹窗
  checkinDialog.visible = false;
};

const takePhotoHandle = () => {
  if (checkinDialog.btnText === "拍照") {
    // 拍照模式
    const video = document.querySelector("#video");
    const canvas = document.querySelector("#photo");
    // 从canvas对象当中获取一个画布对象(上下文对象)
    const context = (canvas as any).getContext("2d");

    // 将视频当前帧绘制到画布上
    context.drawImage(video, 0, 0, 460, 345);

    checkinDialog.showEmpty = false;
    checkinDialog.showVideo = false;
    checkinDialog.showPhoto = true;

    ElMessage.success({
      message: "拍照成功",
      duration: 1200,
    });

    // 切换按钮为重拍状态
    checkinDialog.btnText = "重拍";
    checkinDialog.btnIcon = RefreshRight;

    // 将画布导出为base64格式，并将它保存到响应式对象上（将来要提交）
    checkinDialog.dataForm.photo_2 = (canvas as any).toDataURL("image/jpeg");
  } else {
    // 重拍模式
    checkinDialog.showEmpty = false;
    checkinDialog.showVideo = true;
    checkinDialog.showPhoto = false;

    // 更新按钮文本以及图标
    checkinDialog.btnText = "拍照";
    checkinDialog.btnIcon = Camera;
  }
};

const dataFormSubmit = async () => {
  // 发送ajax请求，执行签到的业务流程
  try {
    const response = await axios.put(
      "/api/mis/appointment/checkin",
      {
        idCardNo: checkinDialog.dataForm.pid,
        patientName: checkinDialog.dataForm.name,
        photo_1: checkinDialog.dataForm.photo_1,
        photo_2: checkinDialog.dataForm.photo_2,
      },
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );

    if (response.data.code === 200 && response.data.data.result) {
      // 显示一个签到成功的通知
      ElNotification.success({
        title: "通知消息",
        message: "签到成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
      // 重置数据
      checkinDialog.showEmpty = true;
      checkinDialog.showVideo = false;
      checkinDialog.showPhoto = false;
      checkinDialog.btnIcon = Camera;
      checkinDialog.btnText = "拍照";
      checkinDialog.dataForm.name = null;
      checkinDialog.dataForm.sex = null;
      checkinDialog.dataForm.pid = null;
      checkinDialog.dataForm.photo_1 = null;
      checkinDialog.dataForm.photo_2 = null;
    } else {
      ElMessage.error({
        message: response.data.msg || "签到失败",
        duration: 1200,
      });
    }
  } catch (e) {
    console.log(e);
    ElMessage.error("网络异常，稍后再试");
  }
};

/*
 * 鱼住科技的读卡器推送过来的数据是先经过base16编码，再进行base64编码，然后推送过来。
 * 我们接收到推送的数据后，要先经过base64解码，再经过base16解码，最终才能拿到原始的内容。
 * base64的解码函数JS内置就有：window.atob() 函数
 * base16的解码函数没有，以下这个函数就是base16的解码函数，从鱼住的index.js文件中拷贝过来的。
 */
function hex2a(hex: string) {
  let str_list = "";
  for (let i = 0; i < hex.length && hex.substr(i, 2) !== "00"; i += 2) {
    const a = hex.charCodeAt(i);
    const b = hex.charCodeAt(i + 1);
    const c = b * 256 + a;
    str_list += String.fromCharCode(c);
  }

  return str_list.toString();
}

/*
 * 该函数用于把YYYYMMDD格式的日期字符串转换成YYYY-MM-DD格式
 * 这个函数也是从index.js文件中拷贝的
 */
function parseDateString(str: string, deco: string, zero: boolean) {
  let year = str.substr(0, 4);
  let month = str.substr(4, 2);
  let date = str.substr(6);
  if (zero) {
    // 如果zero为true则去掉月和日的前导0
    month = month.substr(0, 1) === "0" ? month.substr(1) : month;
    date = date.substr(0, 1) === "0" ? date.substr(1) : date;
  }
  return `${year}${deco}${month}${deco}${date}`;
}

// 浏览器是 WebSocket 客户端。读卡器是WebSocket服务器。
// 以下代码功能：连接本地读卡器服务，接收并解析身份证信息。

// WebSocket 服务器地址（本地读卡器服务）
const webUrl = "ws://127.0.0.1:30004/ws";
// 创建 WebSocket 连接实例
let ws = new WebSocket(webUrl);

// WebSocket 连接建立成功回调
ws.onopen = function (evt) {
  console.log("身份读取WebSocket已连接");
};

// WebSocket 连接关闭回调
ws.onclose = function (evt) {
  console.log("身份读取WebSocket已关闭");
};

// WebSocket 接收到消息回调（核心处理逻辑）
ws.onmessage = async function (messageEvent) {
  // 业务逻辑判断：只有在登记对话框可见且为空时才处理读卡信息
  if (!checkinDialog.visible || !checkinDialog.showEmpty) {
    return;
  }

  // 解析WebSocket消息为JSON对象
  // 消息格式由读卡器SDK协议定义
  const jsonObject = JSON.parse(messageEvent.data);

  // 判断读卡操作是否成功：Ret=0表示成功，其他值表示失败
  if (jsonObject.Ret == 0) {
    // 判断消息类型：10001=被动接收读卡结果，10000=主动读取身份证内容
    // 这里处理的是读卡器感应到身份证后自动推送的结果
    if (jsonObject.Cmd == 10001) {
      // 将推送过来的数据，通过base64解码
      const userParam = JSON.parse(window.atob(jsonObject.UserParam));

      // ========== 开始提取和解析身份证信息 ==========
      // 虽然刚开始已经对全文进行了base64解码，但具体到信息中的姓名需要再进行base64的解码，
      // 姓名处理：Base64解码 → base16解码 → 去除尾部空格
      // 注意：身份证姓名字段固定为15字符，尾部用空格填充
      let name = hex2a(window.atob(userParam.CardInfo.Name)).trim();
      const maskedName = name.length > 1 ? name.substring(0, 1) + "**" : name;
      name = "老杜带你学编程";

      // 性别处理：1=男，其他=女
      const sex =
        hex2a(window.atob(userParam.CardInfo.Sex)) === "1" ? "男" : "女";

      // 身份证号码
      //const pid = hex2a(window.atob(userParam.CardInfo.No));
      const pid = "110105198510011234";

      // 生日处理：格式转换（如：19920315 → 1992-03-15）
      const temp = hex2a(window.atob(userParam.CardInfo.Birthday));
      //const birthday = parseDateString(temp, "-", true);
      const birthday = "1985-10-01";

      // 照片处理：构建Base64图片URL，供img标签直接显示
      // 注意：这个前缀是固定的，告诉浏览器，后面的base64编码的字符串是一个jpg的图片。
      const image = "data:image/jpg;base64," + userParam.BmpInfo;

      // 身份证有效期起始日
      const validityPeriodBegin = hex2a(
        window.atob(userParam.CardInfo.ValidityPeriodBegin),
      );
      const expiryBegin = parseDateString(validityPeriodBegin, "-", true);

      // 身份证有效期截止日处理：可能是具体日期或"长期"
      const validityPeriodEnd = hex2a(
        window.atob(userParam.CardInfo.ValidityPeriodEnd),
      ).trim();
      const expiryEnd =
        validityPeriodEnd !== "长期"
          ? parseDateString(validityPeriodEnd, "-", true)
          : validityPeriodEnd;

      // 有效期检查：如果不是长期有效，检查是否在有效期内
      if (expiryEnd !== "长期") {
        // 使用dayjs检查当前时间是否在有效期内
        let valid = dayjs().isBetween(expiryBegin, expiryEnd);
        if (!valid) {
          // 身份证过期，显示错误提示
          ElMessage.error({
            message: "身份证已过期",
            duration: 1200,
          });
          return; // 过期身份证不再进行后续处理
        }
        console.log("身份证有效期状态:", valid);
      }

      console.log(name, sex, pid, birthday, image);

      // 开启摄像头的函数
      const startCamera = () => {
        // 1. 获取页面中的video元素，用于显示摄像头画面
        const video = document.querySelector("#video") as HTMLVideoElement;

        // 2. 检测浏览器是否支持摄像头API（标准API或各浏览器前缀API）
        //    mediaDevices: 标准API  | webkitGetUserMedia: 旧版Chrome/Safari  | mozGetUserMedia: 旧版Firefox
        const mediaDevices =
          navigator.mediaDevices ||
          (navigator as any).webkitGetUserMedia ||
          (navigator as any).mozGetUserMedia;

        // 3. 浏览器不支持摄像头，提示错误并返回
        if (!mediaDevices) {
          ElMessage.error("当前浏览器不支持摄像头");
          return;
        }

        // 4. 调用浏览器摄像头API，获取视频流
        //    video: true 表示获取视频，audio: false 表示不获取音频
        navigator.mediaDevices
          .getUserMedia({ video: true, audio: false })
          .then((stream) => {
            // 5. 将获取到的视频流绑定到video元素，显示摄像头画面
            video.srcObject = stream;

            // 6. 保存视频流轨道，便于关闭弹窗时释放摄像头资源
            checkinDialog.streamTrack = stream.getTracks()[0];

            // 7. 视频元数据加载完成后触发，开始播放
            video.onloadedmetadata = () => {
              video.play(); // 开始播放摄像头画面

              // 8. 切换UI显示状态：隐藏空状态和照片，显示摄像头画面
              checkinDialog.showEmpty = false;
              checkinDialog.showPhoto = false;
              checkinDialog.showVideo = true;
            };
          })
          .catch((e) => {
            console.log(e);
            // 9. 用户拒绝授权或摄像头不可用等错误
            ElMessage.error("开启摄像头失败，请检查摄像头权限");
          });
      };

      // ========== 信息提取完成 ==========
      // 核验体检人的身份信息
      try {
        const response = await axios.get("/api/mis/appointment/today/status", {
          params: {
            patientName: name,
            gender: sex,
            idCardNo: pid,
          },
          headers: { satoken: localStorage.getItem("token") },
        });

        const result = response.data.data.result;
        // result可能为：0 1 -1
        switch (result) {
          case -1:
            ElMessage.warning("已签到不能重复签到");
            break;
          case 1:
            checkinDialog.dataForm.name = name;
            checkinDialog.dataForm.sex = sex;
            checkinDialog.dataForm.pid = pid;
            checkinDialog.dataForm.photo_1 = image;
            // 开启摄像头
            startCamera();
            break;
          default:
            ElMessage.error("该体检人未预约");
        }
      } catch (e) {
        console.log(e);
        ElMessage.error("网络异常，稍后再试");
      }
    }
  } else {
    // 读卡失败处理：输出错误信息
    console.log("websocket 协议调用失败，原因：" + jsonObject.ErrInfo);
  }
};

interface GuidanceDialog {
  visible: boolean;
  name: null | string;
  sex: null | string;
  age: null | string;
  pid: null | string;
  tel: null | string;
  date: null | string;
  company: null | string;
  qrCodeBase64: undefined | string;
  checkup: Checkup[];
}

interface Checkup {
  place: string;
  name: string;
}
const guidanceDialog: GuidanceDialog = reactive({
  visible: false,
  name: null,
  sex: null,
  age: null,
  pid: null,
  tel: null,
  date: null,
  company: null,
  qrCodeBase64: undefined,
  checkup: [],
});
</script>

<style lang="less" scoped>
@import url("customer_checkin.less");
</style>
