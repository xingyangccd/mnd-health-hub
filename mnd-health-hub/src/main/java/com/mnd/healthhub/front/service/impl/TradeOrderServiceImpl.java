package com.mnd.healthhub.front.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.GoodsSnapshotEntity;
import com.mnd.healthhub.db.entity.TradeOrderEntity;
import com.mnd.healthhub.db.mapper.GoodsSnapshotDao;
import com.mnd.healthhub.db.mapper.MedExamPackageMapper;
import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.PaymentService;
import com.mnd.healthhub.front.service.TradeOrderService;
import com.mnd.healthhub.socket.MessagePushEndpoint;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeOrderServiceImpl implements TradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final MedExamPackageMapper medExamPackageMapper;
    private final PaymentService paymentService;
    private final StringRedisTemplate redisTemplate;
    private final GoodsSnapshotDao goodsSnapshotDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(int customerId, Integer goodsId, Integer quantity) {
        log.info("开始创建支付订单：用户：{}，商品：{}，数量：{}", customerId, goodsId, quantity);

        // 1.风控
        log.debug("检查用户[{}]是否触发风控限制", customerId);
        if (tradeOrderMapper.isCustomerReachedDailyLimit(customerId)) {
            log.warn("用户[{}]触发风控限制，拒绝创建订单", customerId);
            return null;
        }

        // 1.查找商品详情
        Map<String, Object> map = medExamPackageMapper.selectPackageWithPromotionForOrder(goodsId);
        if (map == null || map.isEmpty()) {
            log.warn("套餐[{}]不存在", goodsId);
            throw new HisException("套餐不存在", 404);
        }
        log.debug("查到的套餐信息：{}", map);

        // 将商品的基础信息都取出来，方便后面的使用
        String packageCode = MapUtil.getStr(map, "packageCode");
        String packageName = MapUtil.getStr(map, "packageName");
        String description = MapUtil.getStr(map, "description");
        String coverImage = MapUtil.getStr(map, "coverImage");
        String originalPrice = MapUtil.getStr(map, "originalPrice");
        String currentPrice = MapUtil.getStr(map, "currentPrice");
        String packageType = MapUtil.getStr(map, "packageType");
        String md5Hash = MapUtil.getStr(map, "md5Hash");
        String ruleContent = MapUtil.getStr(map, "ruleContent");
        String ruleName = MapUtil.getStr(map, "ruleName");

        // 从数据库当中返回的是JSON字符串。不能把JSON字符串直接传递给mongodb
        // 如果直接传递给mongodb字符串，mongodb中存储的就不是文档对象了，是纯字符串了。
        String temp = MapUtil.getStr(map, "departmentExam");
        List<Map> departmentExam = temp != null ? JSONUtil.toList(temp, Map.class) : null;

        temp = MapUtil.getStr(map, "labExam");
        List<Map> labExam = temp != null ? JSONUtil.toList(temp, Map.class) : null;

        temp = MapUtil.getStr(map, "medicalExam");
        List<Map> medicalExam = temp != null ? JSONUtil.toList(temp, Map.class) : null;

        temp = MapUtil.getStr(map, "otherExam");
        List<Map> otherExam = temp != null ? JSONUtil.toList(temp, Map.class) : null;

        temp = MapUtil.getStr(map, "examItems");
        List<Map> examItems = temp != null ? JSONUtil.toList(temp, Map.class) : null;

        temp = MapUtil.getStr(map, "tags");
        List<String> tags = temp != null ? JSONUtil.toList(temp, String.class) : null;

        log.info("套餐信息解析完成，用户：{}，套餐：{}，套餐名称：{}", customerId, goodsId, packageName);

        // 2.计算订单金额
        BigDecimal totalAmount = null;
        if (ruleContent == null) {
            // 商品没有促销规则，直接计算总价：总价 = 单价 × 数量
            totalAmount = new BigDecimal(currentPrice).multiply(BigDecimal.valueOf(quantity));
        } else {
            // 商品存在促销规则，按照促销规则计算总价。
            try {
                ExpressRunner runner = new ExpressRunner();
                DefaultContext<String, Object> context = new DefaultContext<>();
                context.put("price", currentPrice);
                context.put("number", quantity);
                Object result = runner.execute(ruleContent, context, null, true, false);
                totalAmount = new BigDecimal(result.toString());
                log.info("促销规则{}执行完成，输入：数量={}，单价={}，结果={}", ruleName, quantity, currentPrice, totalAmount);
            } catch (Exception e) {
                log.error("促销规则执行异常，规则：{}，数量：{}，单价：{}", ruleName, quantity, currentPrice, e);
                throw new HisException("执行规则引擎失败", e);
            }
        }

        // 3.创建微信支付单
        String outTradeNo = null;
        int totalAmountInfFen = 0;
        String codeUrl = null;
        try {
            // 商家生成订单流水号
            outTradeNo = IdUtil.simpleUUID().toUpperCase();

            // 将总金额转换为分（传递给微信支付平台）
            totalAmountInfFen = NumberUtil.round(NumberUtil.mul(totalAmount, BigDecimal.valueOf(100)), 0).intValue();
            totalAmountInfFen = 1;

            // 订单支付的有效期（基于当前系统时间 + 20分钟：订单的支付有效期是20分钟）
            String timeExpire = DateUtil.offsetMinute(new Date(), 20).toInstant().toString();

            // 创建微信支付单
            ObjectNode payResponse = paymentService.unifiedOrder(outTradeNo, totalAmountInfFen, "购买体检套餐", timeExpire);

            // 获取code_url
            codeUrl = payResponse.get("code_url").textValue();
            log.info("微信支付单创建成功：订单流水号：{}，二维码链接：{}", outTradeNo, codeUrl);

        } catch (Exception e) {
            log.error("创建微信支付单失败，交易流水号：{}，金额：{}分", outTradeNo, totalAmountInfFen, e);
            throw new HisException("创建微信支付单失败，请稍后再试");
        }

        // 4.创建支付缓存（设置缓存的过期时间）
        // key的命名规则：codeUrl_customerId_outTradeNo
        // 这个规则需要记住，后面代码中需要使用这key中的 customerId 和 outTradeNo
        String key = "codeUrl_" + customerId + "_" + outTradeNo;
        // 设置这个支付单缓存的有效期是：20分钟
        redisTemplate.opsForValue().set(key, codeUrl, 20, TimeUnit.MINUTES);

        // 5.创建商品快照（如果不存在的话）
        String snapshotId = null;
        try {
            snapshotId = goodsSnapshotDao.hasGoodsSnapshot(md5Hash);
            if (snapshotId == null) {
                // 商品快照不存在，需要保存，创建商品快照对象，执行保存操作。
                GoodsSnapshotEntity goodsSnapshot = new GoodsSnapshotEntity();
                goodsSnapshot.setId(goodsId);
                goodsSnapshot.setPackageCode(packageCode);
                goodsSnapshot.setPackageName(packageName);
                goodsSnapshot.setDescription(description);
                goodsSnapshot.setCoverImage(coverImage);
                goodsSnapshot.setOriginalPrice(new BigDecimal(originalPrice));
                goodsSnapshot.setCurrentPrice(new BigDecimal(currentPrice));
                goodsSnapshot.setPackageType(packageType);
                goodsSnapshot.setMd5Hash(md5Hash);
                goodsSnapshot.setRuleContent(ruleContent);
                goodsSnapshot.setRuleName(ruleName);
                goodsSnapshot.setDepartmentExam(departmentExam);
                goodsSnapshot.setLabExam(labExam);
                goodsSnapshot.setMedicalExam(medicalExam);
                goodsSnapshot.setOtherExam(otherExam);
                goodsSnapshot.setExamItems(examItems);
                goodsSnapshot.setTags(tags);

                // 保存商品快照。返回快照的主键值。
                snapshotId = goodsSnapshotDao.insert(goodsSnapshot);
                log.info("商品快照创建成功：快照ID：{}，套餐：{}", snapshotId, packageName);
            } else {
                // 商品快照已存在
                log.debug("商品快照已存在，快照ID：{}，套餐：{}", snapshotId, packageName);
            }
        } catch (Exception e) {
            log.error("保存商品快照失败，套餐ID：{}，套餐名称：{}", goodsId, packageName, e);
            throw new HisException("保存商品快照失败");
        }

        // 6.保存订单记录
        TradeOrderEntity order = new TradeOrderEntity();
        order.setOutTradeNo(outTradeNo);
        order.setCustomerId(customerId);
        order.setGoodsId(goodsId);
        order.setGoodsTitle(packageName);
        order.setGoodsPrice(new BigDecimal(currentPrice));
        order.setGoodsImage(coverImage);
        order.setGoodsDescription(description);
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount); // 这里的总价不是分为单位的，以元为单位。
        order.setSnapshotId(snapshotId);
        int rows = tradeOrderMapper.insert(order);
        if (rows != 1) {
            log.error("订单保存失败，交易流水号：{}，用户：{}", outTradeNo, customerId);
            throw new HisException("订单保存失败", 500);
        }
        log.info("订单保存成功，交易流水号：{}，用户：{}，金额：{}元", outTradeNo, customerId, totalAmount);

        // 7.更新商品的销量（先注意一下：这里并没有等用户完全支付成功之后，再更新销量。而是创建订单时就更新了销量。）
        rows = medExamPackageMapper.updateSalesVolume(goodsId, quantity);
        if (rows != 1) {
            log.error("套餐销量更新失败，套餐ID：{}，当前销量：{}", goodsId, quantity);
            throw new HisException("套餐销量更新失败");
        }
        log.info("更新商品销量成功，商品ID：{}，购买数量：{}", goodsId, quantity);

        // 8.返回支付的二维码（将二维码图片转换成base64的字符串，响应给前端系统。前端系统使用src属性直接接收base64字符串，显示二维码）
        String qrCodeBase64 = null;
        try {
            // 编写生成二维码图片的配置信息
            QrConfig qrConfig = new QrConfig();
            qrConfig.setWidth(230);
            qrConfig.setHeight(230);
            qrConfig.setMargin(2);
            // 将code_url链接转换成二维码图片，并且将二维码图片以base64编码，最终生成一个base64的字符串。
            qrCodeBase64 = QrCodeUtil.generateAsBase64(codeUrl, qrConfig, "jpg");
            log.info("二维码生成成功，交易流水号：{}", outTradeNo);
        } catch (Exception e) {
            log.error("二维码生成失败，交易流水号：{}，二维码链接：{}", outTradeNo, codeUrl, e);
            throw new HisException("生成支付二维码失败，请稍后重试");
        }

        // 封装最终要返回的数据
        Map<String, Object> result = new HashMap<>();
        result.put("qrCodeBase64", qrCodeBase64);
        result.put("outTradeNo", outTradeNo); // 后面业务中要使用。
        log.info("订单创建成功，交易流水号：{}，用户：{}，金额：{}元", outTradeNo, customerId, totalAmount);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async("taskExecutor")
    public void handlePayNotify(Transaction transaction) {
        // 订单交易流水号
        String outTradeNo = transaction.getOutTradeNo();
        // 支付状态
        Transaction.TradeStateEnum tradeState = transaction.getTradeState();

        log.info("处理支付通知：订单号：{}，状态：{}", outTradeNo, tradeState);

        // 查询订单是否存在.
        TradeOrderEntity order = tradeOrderMapper.selectByOutTradeNo(outTradeNo);
        if (order == null) {
            log.error("订单不存在，订单号：{}", outTradeNo);
            throw new HisException("订单不存在", 404);
        }

        // 幂等性的处理：如果订单已支付，不再重复处理
        if (order.getOrderStatus() != null && order.getOrderStatus() == 3) {
            log.info("订单已支付，无需重复处理，订单号：{}", outTradeNo);
            return;
        }

        // 判断支付结果，更新订单状态
        if (Transaction.TradeStateEnum.SUCCESS.equals(tradeState)) {
            // 支付成功
            // 更新订单状态为已付款
            int rows = tradeOrderMapper.updatePayment(outTradeNo, transaction.getTransactionId(), 3);
            if (rows != 1) {
                log.error("更新订单状态失败，订单号：{}", outTradeNo);
                throw new HisException("更新订单状态失败", 500);
            }

            // 推送消息给前端系统。（需要让我们的服务器和前端系统之间创建一个长连接【WebSocket：服务器主动推送消息给客户端】）
            Integer customerId = tradeOrderMapper.findCustomerIdByOutTradeNo(outTradeNo);
            if (customerId == null) {
                throw new HisException("客户不存在", 404);
            }
            JSONObject json = new JSONObject();
            json.set("result", true);
            MessagePushEndpoint.sendInfo(json.toString(), "customer_" + customerId);

            // 删除redis中的key（支付缓存清空）
            // 这里的key被删除，并不会触发关单操作，因为在执行这个代码时，order_status 的状态已经修改为 3 了。底层不会关单。只是删除缓存。
            String key = "codeUrl_" + order.getCustomerId() + "_" + outTradeNo;
            redisTemplate.delete(key);

            log.info("支付通知处理成功，订单号：{}，交易号：{}，支付金额：{}分",
                    outTradeNo, transaction.getTransactionId(), transaction.getAmount().getTotal());

        } else if (Transaction.TradeStateEnum.CLOSED.equals(tradeState)) {
            // 订单已关闭
            log.info("订单已关闭，订单号：{}", outTradeNo);
            int rows = tradeOrderMapper.updatePayment(outTradeNo, transaction.getTransactionId(), 2);
            if (rows != 1) {
                log.error("更新订单状态失败，订单号：{}", outTradeNo);
                throw new HisException("更新订单状态失败", 500);
            }
        } else {
            // 其他支付状态的处理。
            log.warn("支付通知状态：{}，订单号：{}", tradeState, outTradeNo);
            // 可以根据需要记录日志或者做其他处理....
        }


    }

    @Override
    public boolean getPaymentResult(String outTradeNo) {
        log.info("开始查询支付结果，订单号：{}", outTradeNo);
        // 1. 查询订单是否存在
        TradeOrderEntity order = tradeOrderMapper.selectByOutTradeNo(outTradeNo);
        if (order == null) {
            log.warn("订单不存在，订单号：{}", outTradeNo);
            return false;
        }
        // 2.为了幂等性，判断一下，订单是否已支付，如果已支付，直接返回null
        // 幂等性：用户多次操作，只影响一次（关闭电梯：疯狂按，电梯门也只关一次）
        if (order.getOrderStatus() != null && order.getOrderStatus() == 3) {
            log.info("订单已支付，订单号：{}", outTradeNo);
            return true;
        }

        // 3.调用微信支付接口，查看支付结果
        String transactionId = paymentService.getPaymentResult(outTradeNo);
        if (StrUtil.isBlank(transactionId)) {
            log.info("订单{}尚未支付", outTradeNo);
            return false;
        }

        // 4.支付成功，更新订单状态
        log.info("订单{}支付成功，微信支付单ID：{}，更新订单状态", outTradeNo, transactionId);
        int rows = tradeOrderMapper.updatePayment(outTradeNo, transactionId, 3);
        if (rows != 1) {
            log.error("更新订单状态失败，订单号：{}", outTradeNo);
            // 发送警告信息给商家，用户支付成功了，但是更新数据库失败了
            return false;
        }

        // 5. 删除支付二维码缓存
        String key = "codeUrl_" + order.getCustomerId() + "_" + outTradeNo;
        redisTemplate.delete(key);
        log.info("订单{}支付处理完成", outTradeNo);

        return true;
    }

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> params) {
        long total = tradeOrderMapper.selectPageCount(params);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = tradeOrderMapper.selectPageList(params);
        }
        return PageResult.of(records, total);
    }

    @Override
    public void refund(Integer customerId, Integer orderId) {
        // 1. 查询客户是不是重复发起退款（保证幂等性）
        String existingRefundNo = tradeOrderMapper.findOutRefundNoByOrderId(orderId);
        if (existingRefundNo != null) {
            log.warn("退款已发起，订单ID：{}，退款单号：{}，请勿重复操作", orderId, existingRefundNo);
            throw new HisException("退款已发起，请勿重复操作");
        }

        // 2. 查询交易信息
        Map<String, Object> map = tradeOrderMapper.findTransactionInfoByOrderIdAndCustomerId(orderId, customerId);
        if(map == null || map.isEmpty()) {
            log.error("未查询到交易信息，订单ID：{}，客户ID：{}", orderId, customerId);
            throw new HisException("为查询到交易信息");
        }

        String transactionId = MapUtil.getStr(map, "transactionId");
        String totalAmount = MapUtil.getStr(map, "totalAmount");
        if(StrUtil.isBlank(transactionId)) {
            log.error("微信支付支付单ID为空，订单ID：{}，客户ID：{}", orderId, customerId);
            throw new HisException("微信支付单ID为空");
        }

        // 3. 金额转换为分
        //int total = NumberUtil.mul(totalAmount, "100").intValue();
        int total = 1; // 总金额
        int refund = total; // 退款金额

        log.info("发起退款，订单ID：{}，客户ID：{}，退款金额：{}分", orderId, customerId, refund);

        // 4. 调用退款的方法进行退款
        String outRefundNo = paymentService.refund(transactionId, refund, total);
        if(StrUtil.isBlank(outRefundNo)) {
            log.error("微信退款失败，订单ID：{}，微信支付单号：{}", orderId, transactionId);
            throw new HisException("微信退款失败");
        }

        // 5.更新退款单号，退款日期，退款时间
        int rows = tradeOrderMapper.updateOutRefundNoByOrderIdAndOutRefundNo(orderId, outRefundNo);
        if(rows != 1){
            log.error("更新退款单号失败，订单ID:{},退款流水号：{}", orderId, outRefundNo);
            throw new HisException("更新退款单号失败");
        }

        log.info("退款正在进行中，订单ID：{}，退款流水号：{}", orderId, outRefundNo);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundNotify(RefundNotification refundNotification) {
        String outRefundNo = refundNotification.getOutRefundNo();
        String refundStatus = refundNotification.getRefundStatus().toString();
        String outTradeNo = refundNotification.getOutTradeNo();

        log.info("处理退款回调，退款单号：{}，状态：{}，订单号：{}", outRefundNo, refundStatus, outTradeNo);

        if ("SUCCESS".equals(refundStatus)) {
            int rows = tradeOrderMapper.updateStatusByOutTradeNo(outTradeNo);
            if (rows == 0) {
                log.warn("退款回调更新订单状态失败，订单号：{}", outTradeNo);
                throw new HisException("更新订单退款状态失败");
            }
            log.info("退款成功，退款单号：{}，订单号：{}", outRefundNo, outTradeNo);

        } else if ("ABNORMAL".equals(refundStatus)) {
            log.error("退款异常：用户银行卡冻结或作废，退款单号：{}，订单号：{}", outRefundNo, outTradeNo);
            // TODO: 发送告警通知给客服（给客服发短信），需要人工处理。
        } else {
            log.info("退款状态：{}，退款单号：{}，暂不处理", refundStatus, outRefundNo);
        }
    }

    @Override
    public void checkOrderOwnership(Integer orderId, Integer customerId) {
        boolean exists = tradeOrderMapper.existsByOrderIdAndCustomerId(orderId, customerId);
        if(!exists) {
            throw new HisException("该订单不属于您");
        }
    }
}

















