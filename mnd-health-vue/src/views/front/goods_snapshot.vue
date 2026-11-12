<template>
  <div class="goods-detail">
    <img :src="data.coverImage" class="cover" />
    <div class="property">
      <h3 class="title">{{ data.packageName }} {{ data.packageCode }}</h3>
      <p class="desc">{{ data.description }}</p>
      <div class="row">
        <label>官网价格：</label>
        <div class="info">
          <span class="current-price">{{ data.currentPrice }}</span>
          <span class="initial-price">￥{{ data.originalPrice }}</span>
        </div>
      </div>
      <div class="row">
        <label>享有折扣：</label>
        <div class="info">
          <img
            src="../../assets/front/goods/discount.png"
            class="discount-img"
          />
          <span class="discount">{{
            data.ruleName != null ? data.ruleName : "无"
          }}</span>
        </div>
      </div>
      <!--下面的标签是新添加的-->
      <div class="row">
        <label>商品类型：</label>
        <div class="info">虚拟卡（电子卡密）</div>
      </div>
      <div class="row">
        <label>适用人群：</label>
        <div class="info">{{ data.packageType }}</div>
      </div>
      <div class="row">
        <label>服务承诺：</label>
        <div class="info">
          <img
            src="../../assets/front/goods/property-icon-1.png"
            class="property-icon"
          />
          <span class="service-tag">专业品质</span>
          <img
            src="../../assets/front/goods/property-icon-2.png"
            class="property-icon"
          />
          <span class="service-tag">官方直营</span>
          <img
            src="../../assets/front/goods/property-icon-3.png"
            class="property-icon"
          />
          <span class="service-tag">随时改</span>
          <img
            src="../../assets/front/goods/property-icon-4.png"
            class="property-icon"
          />
          <span class="service-tag">随时退</span>
        </div>
      </div>
    </div>
  </div>

  <el-divider />
  <div class="goods-content">
    <el-descriptions title="商品摘要信息" :column="3" size="large" border>
      <el-descriptions-item label="体检名称" label-align="center">
        {{ data.packageName }} {{ data.packageCode }}
      </el-descriptions-item>
      <el-descriptions-item label="体检类型" label-align="center">{{
        data.packageType
      }}</el-descriptions-item>
      <el-descriptions-item label="适用人群" label-align="center">
        <span class="tag" v-for="one in data.tags">{{ one }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="体检机构" label-align="center">
        天津美年大健康体检中心（卫津南路109号京燕大厦5层）
      </el-descriptions-item>
      <el-descriptions-item label="体检项目" label-align="center"
        >{{ data.examCount }}个</el-descriptions-item
      >
      <el-descriptions-item label="有效期" label-align="center"
        >一年</el-descriptions-item
      >
    </el-descriptions>
    <div class="detail">
      <fieldset>
        <legend>体检项目明细</legend>
        <div v-if="data.departmentExam_count > 0">
          <h4 class="detail-title">
            科室检查({{ data.departmentExam_count }}项目)
          </h4>
          <table class="detail-table">
            <tr v-for="one in data.departmentExam">
              <th>{{ one.title }}</th>
              <td>{{ one.content }}</td>
            </tr>
          </table>
        </div>
        <div v-if="data.labExam_count > 0">
          <h4 class="detail-title">实验室检查({{ data.labExam_count }}项目)</h4>
          <table class="detail-table">
            <tr v-for="one in data.labExam">
              <th>{{ one.title }}</th>
              <td>{{ one.content }}</td>
            </tr>
          </table>
        </div>
        <div v-if="data.medicalExam_count > 0">
          <h4 class="detail-title">
            医技检查({{ data.medicalExam_count }}项目)
          </h4>
          <table class="detail-table">
            <tr v-for="one in data.medicalExam">
              <th>{{ one.title }}</th>
              <td>{{ one.content }}</td>
            </tr>
          </table>
        </div>
        <div v-if="data.otherExam_count > 0">
          <h4 class="detail-title">其他检查({{ data.otherExam_count }}项目)</h4>
          <table class="detail-table">
            <tr v-for="one in data.otherExam">
              <th>{{ one.title }}</th>
              <td>{{ one.content }}</td>
            </tr>
          </table>
        </div>
      </fieldset>
    </div>
  </div>

  <div class="checkup-appointment">
    <fieldset>
      <legend>预约须知</legend>
      <el-descriptions title="" :column="1" size="large" class="descriptions">
        <el-descriptions-item
          label="预约时间："
          label-align="center"
          style="width: 300px"
        >
          该医院支持提前可约，若要预约当天请在08:30前下单
        </el-descriptions-item>
        <el-descriptions-item label="营业时间：" label-align="center">
          周一至周五08:00-10:30(到院时间为08:00-10:30)
        </el-descriptions-item>
        <el-descriptions-item label="体检地点：" label-align="center">
          天津市南开区向阳路888号美年大健康体检中心
        </el-descriptions-item>
        <el-descriptions-item label="体检凭证：" label-align="center">
          体检当天凭借预约成功短信，现场出示身份证即可体检
        </el-descriptions-item>
        <el-descriptions-item label="优惠信息：" label-align="center">
          会员在线支付时含“立减”字样的套餐，付款时会自动抵扣掉对应的金额
        </el-descriptions-item>
        <el-descriptions-item label="订单退改：" label-align="center">
          如客户预约成功后选择退款，需扣除套餐实付金额的10%作为服务费。最高扣款金额不超过100元。（*个别体检中心执行单独退赔政策*）
        </el-descriptions-item>
        <el-descriptions-item label="注意事项：" label-align="center">
          当您预约套餐时，即表示接受检测的所有项目。如因自身原因放弃体检套餐中的检查项目，网站将不予退款处理
        </el-descriptions-item>
        <el-descriptions-item label="发票申请：" label-align="center">
          请在体检后到“我的订单”中申请，如需了解开票具体流程，可在提交订单后及时联系中康体检网客服，客服热线：4008007580
        </el-descriptions-item>
      </el-descriptions>
    </fieldset>

    <fieldset>
      <legend>线上预约优势</legend>
      <div class="content">
        <div class="advantage">
          <div class="card">
            <div class="left"><span>提前确认到院可检</span></div>
            <div class="right">
              <span
                >提前选择体检时间、体检套餐，避免部分医院预约号紧缺，而导致当天无法体检的状况。</span
              >
            </div>
          </div>
          <div class="card">
            <div class="left"><span>省时方便无需排队</span></div>
            <div class="right">
              <span
                >体检当天携带身份证到院打印体检单，即可开始体检，无需排队缴费。</span
              >
            </div>
          </div>
          <div class="card">
            <div class="left"><span>电话通知灵活改期</span></div>
            <div class="right">
              <span
                >预约成功后如需改期，可提前电话告知客服，灵活安排行程。</span
              >
            </div>
          </div>
          <div class="card">
            <div class="left"><span>享受优惠节省费用</span></div>
            <div class="right">
              <span
                >线上体检套餐，享受团体体检优惠价格，大部分可享医院价的7-9折。</span
              >
            </div>
          </div>
        </div>
        <div class="timeline">
          <ul>
            <li>
              <img
                src="../../assets/front/goods/timeline-icon-1.png"
                class="list-img"
              />
              <div class="list-line"></div>
              <div class="list-tag">
                <img src="../../assets/front/goods/timeline-tag-1.png" />
                <span>1</span>
              </div>
              <div class="list-content">
                <h4>选购体检套餐</h4>
                <p>确定订单无误后，完成线上支付</p>
              </div>
            </li>
            <li>
              <img
                src="../../assets/front/goods/timeline-icon-2.png"
                class="list-img"
              />
              <div class="list-line"></div>
              <div class="list-tag">
                <img src="../../assets/front/goods/timeline-tag-2.png" />
                <span>2</span>
              </div>
              <div class="list-content">
                <h4>完成预约体检</h4>
                <p>填写体检人信息及体检日期</p>
              </div>
            </li>
            <li>
              <img
                src="../../assets/front/goods/timeline-icon-3.png"
                class="list-img"
              />
              <div class="list-line"></div>
              <div class="list-tag">
                <img src="../../assets/front/goods/timeline-tag-3.png" />
                <span>3</span>
              </div>
              <div class="list-content">
                <h4>到院体检</h4>
                <p>到院出示身份证，领取体检单体检</p>
              </div>
            </li>
            <li>
              <img
                src="../../assets/front/goods/timeline-icon-4.png"
                class="list-img"
              />
              <div class="list-line"></div>
              <div class="list-tag">
                <img src="../../assets/front/goods/timeline-tag-4.png" />
                <span>4</span>
              </div>
              <div class="list-content">
                <h4>获取体检报告</h4>
                <p>根据医院情况，到前台登记自取或自费邮寄</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </fieldset>

    <fieldset>
      <legend>体检注意事项</legend>
      <div class="content">
        <ul class="look-list">
          <li class="item">
            <div class="left">体检前</div>
            <div class="right">
              <ul>
                <li>体检前一天请您清淡饮食,勿饮酒、勿劳累。体检当天请空腹。</li>
                <li>
                  体检前一天要注意休息，晚上8点后不再进食。避免剧烈运动和情绪激动，保证充足睡眠，以免影响体检结果。
                </li>
                <li>例假期间不宜做妇科、尿液检查。</li>
              </ul>
            </div>
          </li>
          <li class="item">
            <div class="left">体检中</div>
            <div class="right">
              <ul>
                <li>
                  需空腹检查的项目为抽血、腹部B超、数字胃肠，胃镜及其它标注的体检项目。
                </li>
                <li>
                  做膀胱、子宫、附件B超时请勿排尿，如无尿需饮水至膀胱充盈。做妇科检查前应排空尿。
                </li>
                <li>
                  未婚女性不做妇科检查；怀孕的女性请预先告知医护人员,不安排做放射及其他有影响的检查。
                </li>
                <li>做放射线检查前,请您除去身上佩戴的金银、玉器等饰物。</li>
                <li>
                  核磁共振检查，应禁止佩带首饰、手表、传呼、手机等金属物品，磁卡也不应带入检查室，以防消磁。
                </li>
              </ul>
            </div>
          </li>
          <li class="item">
            <div class="left">体检后</div>
            <div class="right">
              <ul>
                <li>全部项目完毕后请您务必将体检单交到前台。</li>
                <li>请您认真听取医生的建议,及时复查.随诊或进一步检查治疗。</li>
                <li>
                  请您保存好体检结果，以便和下次体检结果作对照，也可作为您就医时的资料。
                </li>
              </ul>
            </div>
          </li>
        </ul>
      </div>
    </fieldset>
  </div>
</template>

<script lang="ts" setup>
import {
  reactive,
  ref,
  type Ref,
  getCurrentInstance,
  onMounted,
  inject,
} from "vue";
import router from "../../router/index";
import axios from "axios";
import { ElMessage } from "element-plus";

const data: any = reactive({
  packageCode: null,
  packageName: null,
  description: null,
  coverImage: null,
  originalPrice: null,
  currentPrice: null,
  ruleName: null,
  packageType: null,
  tags: [],
  departmentExam: [],
  labExam: [],
  medicalExam: [],
  otherExam: [],
  departmentExam_count: null,
  labExam_count: null,
  medicalExam_count: null,
  otherExam_count: null,
  examCount: null,
});

// 页面加载的时候发送ajax请求，提交快照id，展示商品快照信息。
const snapshotId = router.currentRoute.value.params.id;
const mode = router.currentRoute.value.params.mode;

const minioUrl = inject("minioUrl");
const loadSnapshot = async () => {
  try {
    const path =
      mode === "front"
        ? "/api/front/goods/snapshot"
        : "/api/front/goods/mis/snapshot";

    const response = await axios.get(path, {
      params: { snapshotId },
      headers: { satoken: localStorage.getItem("token") },
    });

    if (response.data.code === 200) {
      const result = response.data.data.result;

      data.packageCode = result.packageCode;
      data.packageName = result.packageName;
      data.description = result.description;
      data.coverImage = `${minioUrl}/${result.coverImage}`;
      data.originalPrice = result.originalPrice;
      data.currentPrice = result.currentPrice;
      data.ruleName = result.ruleName;
      data.packageType = result.packageType;
      data.tags = result.tags;
      data.departmentExam = result.departmentExam || [];
      data.labExam = result.labExam || [];
      data.medicalExam = result.medicalExam || [];
      data.otherExam = result.otherExam || [];
      data.departmentExam_count = data.departmentExam.length;
      data.labExam_count = data.labExam.length;
      data.medicalExam_count = data.medicalExam.length;
      data.otherExam_count = data.otherExam.length;
      data.examCount =
        data.departmentExam_count +
        data.labExam_count +
        data.medicalExam_count +
        data.otherExam_count;
    } else {
      console.log(response);
      ElMessage.error(response.data.msg || "加载商品信息失败");
    }
  } catch (e) {
    ElMessage.error("网络异常，稍后再试");
  }
};

// 页面打开时就发送请求。
loadSnapshot();
</script>

<style lang="less" scoped>
@import url("goods_snapshot.less");
</style>
