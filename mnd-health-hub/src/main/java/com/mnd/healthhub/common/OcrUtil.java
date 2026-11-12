package com.mnd.healthhub.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloud.apigateway.sdk.utils.Client;
import com.cloud.apigateway.sdk.utils.Request;
import com.mnd.healthhub.exception.HisException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class OcrUtil {

    @Value("${huawei.appKey}")
    private String appKey;

    @Value("${huawei.appSecret}")
    private String appSecret;

    private static final String OCR_URL = "https://jmexpressbill.apistore.huaweicloud.com/ocr/express-bill";
    private static final int SUCCESS_CODE = 200;

    /**
     * 运单识别
     *
     * @param imgBase64 运单条形码图片
     * @return 运单信息
     */
    public Map<String, String> identifyWaybill(String imgBase64) {
        log.info("开始OCR识别运单");

        if (StrUtil.isBlank(imgBase64)) {
            log.error("OCR识别失败，图片的base64字符串为空");
            throw new HisException("图片数据不能为空");
        }

        try {
            // 构建请求
            Request request = new Request();
            request.setKey(appKey);
            request.setSecret(appSecret);
            request.setMethod("POST");
            request.setUrl(OCR_URL);
            // 模拟表单提交
            request.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            // 设置请求体
            String encodedBase64 = URLUtil.encodeAll(imgBase64);
            request.setBody("base64=" + encodedBase64);

            // 生成签名
            HttpRequestBase signedRequest = Client.sign(request);

            // 发送请求
            try (CloseableHttpClient client = HttpClients.custom().build(); CloseableHttpResponse response = client.execute(signedRequest)) {

                if (response.getStatusLine().getStatusCode() != 200) {
                    log.error("OCR识别失败，状态码: {}", response.getStatusLine().getStatusCode());
                    throw new HisException("OCR识别运单失败");
                }

                // HttpEntity就是具体响应回来的数据封装的对象。
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new HisException("OCR响应体为空");
                }

                String responseBody = EntityUtils.toString(entity, "utf-8");
                log.debug("OCR响应: {}", responseBody);

                // 解析响应
                JSONObject json = JSONUtil.parseObj(responseBody);
                int code = json.getInt("code");
                String msg = json.getStr("msg");

                if (code != SUCCESS_CODE) {
                    log.error("OCR识别失败，code: {}, msg: {}", code, msg);
                    throw new HisException("OCR识别运单失败: " + msg);
                }

                JSONObject data = json.getJSONObject("data");
                if (data == null) {
                    log.error("OCR响应数据为空");
                    throw new HisException("OCR识别结果为空");
                }

                Map<String, String> result = new HashMap<>();
                result.put("recName", data.getStr("recipient_name", ""));
                result.put("recPhone", data.getStr("recipient_phone", ""));
                result.put("expressNo", data.getStr("waybill_number", ""));

                log.info("OCR识别成功，运单号: {}", result.get("expressNo"));
                return result;
            }

        } catch (Exception e) {
            log.error("OCR识别失败", e);
            throw new HisException("OCR识别运单失败：" + e.getMessage());
        }

    }

}
