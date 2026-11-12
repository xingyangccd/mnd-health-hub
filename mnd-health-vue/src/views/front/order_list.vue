<template>
  <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
    <el-form-item prop="keyword">
      <el-input
        v-model="dataForm.keyword"
        placeholder="套餐标题 / 订单编号"
        size="default"
        class="keyword"
        maxlength="32"
        clearable
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
        <el-radio-button label="未付款"></el-radio-button>
        <el-radio-button label="已付款"></el-radio-button>
      </el-radio-group>
    </el-form-item>
  </el-form>
  <div class="order-list" v-show="!empty">
    <div class="order" v-for="one in data.dataList">
      <div class="header">
        <div class="datetime">{{ one.createTime }}</div>
        <div class="uuid">
          订单号：
          <span>{{ one.outTradeNo }}</span>
        </div>
        <div class="detail" @click="searchDetailHandle(one.snapshotId)">
          查看商品详情
        </div>
      </div>
      <div class="content">
        <img :src="one.goodsImage" class="image" />
        <div class="info">
          <h4>{{ one.goodsTitle }}</h4>
          <p>{{ one.goodsDescription }}</p>
        </div>
        <div class="price">
          <span class="label">套餐单价</span>
          <span class="value">￥{{ one.goodsPrice }}</span>
        </div>
        <div class="number">
          <span class="label">购买数量</span>
          <span class="value">×{{ one.quantity }}</span>
        </div>
        <div class="amount">
          <span class="label">合计</span>
          <span class="value">￥{{ one.totalAmount }}</span>
        </div>
        <div class="status">
          <span class="label">状态</span>
          <span class="value">{{ one.orderStatus }}</span>
        </div>
        <div class="operate">
          <el-button
            v-if="one.orderStatus == '未付款'"
            type="primary"
            :disabled="one.disabled"
            @click="paymentHandle(one.outTradeNo)"
          >
            付款
          </el-button>
          <el-button
            v-if="one.orderStatus == '未付款'"
            type="danger"
            @click="closeOrderHandle(one.id)"
          >
            取消订单
          </el-button>
          <el-button
            v-if="['已付款', '已预约'].includes(one.orderStatus)"
            type="primary"
            :disabled="one.appointCount == one.quantity"
            @click="appointHandle(one.id, one.quantity, one.appointCount)"
          >
            预约体检
          </el-button>
          <el-button v-if="one.orderStatus == '已结束'">获取发票</el-button>
          <el-button
            v-if="one.orderStatus == '已付款'"
            type="danger"
            :disabled="one.appointCount > 0"
            @click="refundHandle(one.id)"
          >
            退款
          </el-button>
        </div>
      </div>
    </div>
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
  <div class="empty" v-show="empty">
    <el-empty :image-size="200" />
  </div>

  <el-dialog
    title="体检预约"
    :close-on-click-modal="false"
    v-model="appointDialog.visible"
    width="550px"
  >
    <el-form
      :model="appointDialog.dataForm"
      ref="dialogForm"
      :rules="appointDialog.dataRule"
      label-width="80px"
    >
      <fieldset class="appointment">
        <legend>
          <h4>我的预约</h4>
        </legend>
        <el-form-item label="预约日期" prop="appointmentDate">
          <el-date-picker
            v-model="appointDialog.dataForm.appointmentDate"
            type="date"
            placeholder="选择日期"
            size="default"
            :editable="false"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
          />
          <span class="desc">提示：不可预约今日</span>
        </el-form-item>
        <el-form-item label="体检人" prop="patientName">
          <el-input
            v-model="appointDialog.dataForm.patientName"
            size="default"
            placeholder="输入姓名"
            maxlength="10"
            clearable
          />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCardNo">
          <el-input
            v-model="appointDialog.dataForm.idCardNo"
            size="default"
            placeholder="输入身份证号"
            maxlength="18"
            clearable
          />
        </el-form-item>
        <el-form-item label="电话号码" prop="phone">
          <el-input
            v-model="appointDialog.dataForm.phone"
            size="default"
            placeholder="输入电话号码"
            maxlength="11"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮寄地址" prop="address">
          <el-input
            v-model="appointDialog.dataForm.address"
            size="default"
            placeholder="输入接收体检报告的邮寄地址"
            maxlength="100"
            clearable
          />
        </el-form-item>
        <el-form-item label="公司名称" prop="company">
          <el-input
            v-model="appointDialog.dataForm.company"
            size="default"
            placeholder="输入公司名称"
            maxlength="100"
            clearable
          />
        </el-form-item>
      </fieldset>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button size="default" @click="appointDialog.visible = false"
          >取消</el-button
        >
        <el-button type="primary" size="default" @click="dataFormSubmit"
          >确定</el-button
        >
      </span>
    </template>
  </el-dialog>
</template>
<script lang="ts" setup>
import {
  reactive,
  ref,
  type Ref,
  getCurrentInstance,
  inject,
  nextTick,
} from "vue";
import { Search } from "@element-plus/icons-vue";
import router from "../../router/index";

import { dayjs, ElMessage, ElMessageBox } from "element-plus";
import isBetween from "dayjs/plugin/isBetween";
import axios from "axios";
dayjs.extend(isBetween);

let empty = ref(false);

interface DataForm {
  keyword: null | string;
  statusLabel: string;
  status: null | number;
}

const dataForm: DataForm = reactive({
  keyword: null,
  statusLabel: "全部",
  status: null,
});

const dataRule = reactive({
  keyword: [
    {
      required: false,
      pattern: "^[a-zA-Z0-9\u4e00-\u9fa5]{1,32}$",
      message: "关键字内容不正确",
    },
  ],
});

const minioUrl = inject("minioUrl");

interface Order {
  createTime: string;
  outTradeNo: string;
  snapshotId: string;
  goodsImage: string;
  goodsTitle: string;
  goodsDescription: string;
  goodsPrice: number;
  quantity: number;
  totalAmount: number;
  orderStatus: string;
  disabled: boolean;
  appointCount: number;
  id: number;
}
interface Data {
  dataList: Order[];
  pageIndex: number;
  pageSize: number;
  totalCount: number;
  loading: boolean;
}
const data: Data = reactive({
  dataList: [],
  pageIndex: 1,
  pageSize: 10,
  totalCount: 0,
  loading: false,
});

const sizeChangeHandle = (pageSize: number) => {
  data.pageSize = pageSize;
  data.pageIndex = 1;
  loadPageList();
};

const currentChangeHandle = (pageNo: number) => {
  data.pageIndex = pageNo;
  loadPageList();
};

const refundHandle = async (orderId: number) => {
  try {
    await ElMessageBox.confirm("您确定要退款吗？", "提示信息", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    // 用户确定退款
    const { data: responseData } = await axios.put(
      "/api/front/order/refund",
      { orderId },
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );

    if (responseData.code === 200) {
      ElMessage.success({
        message: "退款申请已提交，请稍后查看到账情况",
        duration: 1200,
      });
    } else {
      ElMessage.error({
        message: "退款失败，请联系客服",
        duration: 1200,
      });
    }
  } catch (e) {
    // 用户点击了取消，走到这里
    // 以上程序执行过程中出现错误也会走到这里
    if (e === "cancel") return;
    ElMessage.error({
      message: "退款失败，请联系客服",
      duration: 1200,
    });
  }
};

const dialogForm = ref();

const appointHandle = async (
  id: any,
  quantity: number,
  appointCount: number,
) => {
  if (appointCount === 0) {
    // 用户第一次预约，需要用户确认（因为预约之后不能退款了）
    try {
      await ElMessageBox.confirm(
        "预约成功后不支持退款，确定要预约吗？",
        "确认信息",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
        },
      );
    } catch {
      // 用户取消了，或出错了。
      return;
    }

    // 用户确认了预约，显示弹窗
    appointDialog.visible = true;
    appointDialog.dataForm.orderId = id;
    await nextTick();
    // 重置表单数据，清空所有错误提示信息。（弹窗中所有的字段都需要校验，都带prop。）
    dialogForm.value.resetFields();
  }
};

const closeOrderHandle = (id: number) => {};
const paymentHandle = (no: string) => {};

const form = ref();

const searchHandle = async () => {
  try {
    await form.value.validate();
    // 如果程序能够走到这里，说明校验成功了。
    data.pageIndex = 1;
    loadPageList();
  } catch {
    // 验证失败，不做任何处理。
  }
};

const searchDetailHandle = (snapshotId: string) => {
  router.push({
    name: "FrontGoodsSnapshot",
    params: {
      id: snapshotId,
      mode: "front",
    },
  });
};

const loadPageList = async () => {
  data.loading = true;
  try {
    if (dataForm.statusLabel === "全部") {
      dataForm.status = null;
    } else if (dataForm.statusLabel === "未付款") {
      dataForm.status = 1;
    } else {
      dataForm.status = 3;
    }

    const response = await axios.get("/api/front/order/list", {
      params: {
        keyword: dataForm.keyword,
        orderStatus: dataForm.status,
        pageNo: data.pageIndex,
        pageSize: data.pageSize,
      },
      headers: {
        satoken: localStorage.getItem("token"),
      },
    });

    const pageResult = response.data.data.pageResult;

    // Record 这是TS内置的类型。可以直接用。
    // 作用是限定一个对象中key和value的类型。
    const statusEnum: Record<string, string> = {
      "1": "未付款",
      "2": "已关闭",
      "3": "已付款",
      "4": "已退款",
      "5": "已预约",
      "6": "已结束",
    };

    console.log(pageResult);
    const list = pageResult.records;
    for (let one of list) {
      one.goodsImage = `${minioUrl}/${one.goodsImage}`;
      one.orderStatus = statusEnum[String(one.orderStatus)];
    }

    data.dataList = list;
    data.totalCount = pageResult.total;

    // 当返回的数据时0条记录，显示空盒子。
    empty.value = list.length === 0;
  } catch (error) {
    console.log(error);
    ElMessage.error("网络异常，稍后再试");
  } finally {
    data.loading = false;
  }
};

// 页面打开就加载数据
loadPageList();

const appointDialog = reactive({
  visible: false,
  dataForm: {
    orderId: null,
    appointmentDate: null,
    patientName: null,
    idCardNo: null,
    phone: null,
    address: null,
    company: null,
  },
  dataRule: {
    appointmentDate: [{ required: true, message: "日期不能为空" }],
    patientName: [
      { required: true, message: "姓名不能为空" },
      { pattern: "^[\u4e00-\u9fa5]{2,10}$", message: "姓名格式错误" },
    ],
    idCardNo: [
      { required: true, message: "身份证号不能为空" },
      { pattern: "^[0-9Xx]{18}$", message: "身份证号格式错误" },
    ],
    phone: [
      { required: true, message: "电话号码不能为空" },
      { pattern: "^1[1-9]\\d{9}$", message: "电话号码格式错误" },
    ],
    address: [
      { required: true, message: "邮寄地址不能为空" },
      {
        pattern: "^[0-9A-Za-z\u4e00-\u9fa5\\-_#]{10,100}$",
        message: "邮寄地址格式错误",
      },
    ],
    company: [
      {
        required: false,
        pattern: "^[0-9A-Za-z\u4e00-\u9fa5\\-_#]{2,100}$",
        message: "公司名称不正确",
      },
    ],
  },
});

const disabledDate = (date: string) => {
  //只能预约未来60天的体检
  let ok = dayjs(date).isBetween(dayjs(), dayjs().add(60, "day"));
  return !ok;
};

const dataFormSubmit = async () => {
  try {
    await dialogForm.value.validate();

    // 再次提醒
    await ElMessageBox.confirm("确认提交预约信息吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const response = await axios.post(
      "/api/front/appointment",
      {
        orderId: appointDialog.dataForm.orderId,
        appointmentDate: appointDialog.dataForm.appointmentDate,
        patientName: appointDialog.dataForm.patientName,
        idCardNo: appointDialog.dataForm.idCardNo,
        phone: appointDialog.dataForm.phone,
        address: appointDialog.dataForm.address,
        company: appointDialog.dataForm.company || "",
      },
      {
        headers: {
          satoken: localStorage.getItem("token"),
        },
      },
    );

    if (response.data.code === 200) {
      appointDialog.visible = false;
      ElMessage.success({
        message: "预约成功",
        duration: 1200,
        onClose: () => loadPageList(),
      });
    } else {
      ElMessage.error(response.data.data.result || "预约失败");
    }
  } catch (e) {
    if (e === "cancel") return;
    console.log(e);
    ElMessage.error("预约失败，请稍后再试");
  }
};
</script>

<style lang="less" scoped>
@import url("order_list.less");
</style>
