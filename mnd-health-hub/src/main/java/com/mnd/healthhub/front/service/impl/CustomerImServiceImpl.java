package com.mnd.healthhub.front.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mnd.healthhub.db.mapper.CrmCustomerImMapper;
import com.mnd.healthhub.db.mapper.CrmCustomerMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.CustomerImService;
import com.tencentyun.TLSSigAPIv2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.service.RequestBodyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service("frontCustomerImService")
@Slf4j
@RequiredArgsConstructor
public class CustomerImServiceImpl implements CustomerImService {

    private final CrmCustomerImMapper crmCustomerImMapper;

    @Value("${tencent.im.sdkAppId}")
    private Long sdkAppId;

    @Value("${tencent.im.secretKey}")
    private String secretKey;

    @Value("${tencent.im.managerId}")
    private String managerId;

    @Value("${tencent.im.customerServiceId}")
    private String customerServiceId;

    private final CrmCustomerMapper crmCustomerMapper;

    // 调用IM RESTFul接口的基础URL。
    private static final String BASE_URL = "https://console.tim.qq.com/";

    // 安全签名的有效期：半年
    private static final int USER_SIG_EXPIRE = 180 * 86400; // 180天

    // 常量字符串：IM账号已存在 Imported，IM账号不存在 NotImported
    private static final String ACCOUNT_IMPORTED = "Imported";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createAccount(Integer customerId) {
        // 1. 从大健康系统当中查询客户的信息
        Map<String, Object> customer = crmCustomerMapper.selectById(customerId);
        String phone = MapUtil.getStr(customer, "phone"); // 客户的手机号
        String photoUrl = MapUtil.getStr(customer, "photoUrl"); // 客户头像

        // 2. 生成客户的IM账号名，格式：customer_客户ID
        // 生成客户的昵称，客户昵称是在聊天窗口展示的：customer_phone
        String account = "customer_" + customerId;
        String nickname = "客户_" + phone;

        // 3. 初始化腾讯IM签名生成器
        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);

        // 4. 生成客户的账号签名（客户的安全签名），有效期180天
        String userSigForCustomer = api.genUserSig(account, USER_SIG_EXPIRE);

        // 5. 封装要返回的数据
        Map<String, Object> result = new HashMap<>();
        result.put("sdkAppId", sdkAppId);
        result.put("account", account);
        result.put("userSig", userSigForCustomer);

        // 6. 给管理员生成一个安全签名，因为他要调用IM的API接口。
        String adminSig = api.genUserSig(managerId, USER_SIG_EXPIRE);

        // 7. 查询客户的IM账号是否已存在
        // 在这个方法当中给腾讯云的IM服务发送请求，调用接口，查询IM账号是否存在。
        boolean accountExists = checkAccountExists(account, adminSig);

        // 8. 如果客户的IM账号不存在，创建账号
        boolean isNewAccount = false;
        if (!accountExists) {
            // 在这个方法当中给腾讯云IM服务发送请求，调用接口，创建IM账户。
            createImAccount(account, nickname, photoUrl, adminSig);
            // 只要执行到这里，说明这个账号一定是第一次创建。是一个新账号。（新账号要和客服专员建立好友关系，才能发送消息。）
            isNewAccount = true;
        }

        // 给新的客户IM账号添加好友（客服专员加为好友）
        if (isNewAccount) {
            // 添加好友
            addFriend(account, adminSig);
        }

        // 记录客户的登录时间
        int rows = crmCustomerImMapper.insertOrUpdate(customerId);
        if (rows == 0) {
            log.error("创建客户IM账号登录记录失败，客户账号ID：{}", customerId);
            throw new HisException("创建客户IM账号登录记录失败，客户账号ID：" + customerId);
        }

        // 给客户发送欢迎语
        sendWelcomeMessage(account);

        // 结果一定要返回，这是登录凭证。
        return result;
    }

    /**
     * 发送欢迎语
     *
     * @param account 消息发送给该账号
     */
    private void sendWelcomeMessage(String account) {
        // 生成客服账号签名
        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);
        String customerServiceSig = api.genUserSig(customerServiceId, USER_SIG_EXPIRE);

        // 构建URL
        String url = buildUrlForService(customerServiceSig);

        // 构建请求体
        JSONObject request = new JSONObject();
        request.set("SyncOtherMachine", 2);
        request.set("To_Account", account);
        request.set("MsgLifeTime", 120);
        request.set("MsgRandom", RandomUtil.randomInt(1, 99999999));

        // 构建消息体
        JSONObject msgContent = new JSONObject();
        msgContent.set("Text", "您好，有什么可以帮您吗？");

        JSONObject msgBodyItem = new JSONObject();
        msgBodyItem.set("MsgType", "TIMTextElem");
        msgBodyItem.set("MsgContent", msgContent);

        request.set("MsgBody", Collections.singletonList(msgBodyItem));

        // 发送请求
        String response = HttpUtil.post(url, request.toString());
        JSONObject result = JSONUtil.parseObj(response);

        Integer errorCode = result.getInt("ErrorCode");
        String errorInfo = result.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("发送欢迎语失败：{}", errorInfo);
            throw new HisException("客服系统异常");
        }
    }

    /**
     * 专门为service定义一个构建URL的方法。
     *
     * @param userSig 安全签名
     * @return url
     */
    private String buildUrlForService(String userSig) {
        return BASE_URL + "v4/openim/sendmsg?sdkappid=" + sdkAppId
                + "&identifier=" + customerServiceId
                + "&usersig=" + userSig
                + "&random=" + RandomUtil.randomInt(99999999)
                + "&contenttype=json";
    }

    /**
     * 让客户的IM账号和客服专员的IM账号建立好友关系。
     *
     * @param account  客户的IM账号
     * @param adminSig 管理员的安全签名
     */
    private void addFriend(String account, String adminSig) {

        String url = buildUrl("v4/sns/friend_add", adminSig);

        // 封装请求体
        JSONObject friendItem = new JSONObject();
        // 要和哪个IM账号建立好友关系，通过 To_Account 来指定
        friendItem.set("To_Account", customerServiceId);
        // 添加源：通过WEB添加的
        friendItem.set("AddSource", "AddSource_Type_WEB");

        JSONObject request = new JSONObject();
        // account 要加 customerServiceId 为好友。
        request.set("From_Account", account);
        request.set("AddFriendItem", Collections.singletonList(friendItem));

        // 发送请求
        String response = HttpUtil.post(url, request.toString());
        JSONObject result = JSONUtil.parseObj(response);

        // 解析这个结果
        Integer errorCode = result.getInt("ErrorCode");
        String errorInfo = result.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("添加客服IM好友失败：{}", errorInfo);
            throw new HisException("客服系统异常");
        }

        JSONArray resultItem = result.getJSONArray("ResultItem");
        JSONObject item = resultItem.getJSONObject(0);

        Integer resultCode = item.getInt("ResultCode");
        String resultInfo = item.getStr("ResultInfo");

        if (resultCode != 0) {
            log.error("添加客服IM好友失败：{}", resultInfo);
            throw new HisException("客服系统异常");
        }

    }

    /**
     * 调用腾讯云IM服务，创建IM账号。
     *
     * @param account  IM账号
     * @param nickname 昵称
     * @param photoUrl 头像
     * @param adminSig 管理员的安全签名
     */
    private void createImAccount(String account, String nickname, String photoUrl, String adminSig) {
        // URL
        String url = buildUrl("v4/im_open_login_svc/account_import", adminSig);

        // 封装请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("UserID", account);
        requestBody.set("Nick", nickname);
        requestBody.set("FaceUrl", photoUrl);

        // 发送请求
        String response = HttpUtil.post(url, requestBody.toString());

        // 将响应结果字符串转换成对象，便于操作
        JSONObject result = JSONUtil.parseObj(response);
        Integer errorCode = result.getInt("ErrorCode");
        String errorInfo = result.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("创建客户IM账号失败：{}", errorInfo);
            throw new HisException("客服系统异常");
        }
    }

    /**
     * 调用腾讯云的IM服务接口，验证IM账号是否存在。
     *
     * @param account  账号
     * @param adminSig 管理员安全签名（API调用者的签名，腾讯云需要这个签名来验证这是一个合法的调用）
     * @return true表示已存在，false表示不存在
     */
    private boolean checkAccountExists(String account, String adminSig) {

        // 构建URL
        String url = buildUrl("v4/im_open_login_svc/account_check", adminSig);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        // 新的内容：Collections.singletonList() 作用：创建一个List集合，集合中只存储一个元素。
        // Collections.singletonMap() 作用：创建一个Map集合，集合中只存储一个元素。
        requestBody.set("CheckItem", Collections.singletonList(Collections.singletonMap("UserID", account)));

        // 发送请求，请求IM服务器。
        String response = HttpUtil.post(url, requestBody.toString());
        // 将响应回来的字符串再转换成JSONObject，这样便于我们的操作。
        JSONObject result = JSONUtil.parseObj(response);
        Integer errorCode = result.getInt("ErrorCode");
        String errorInfo = result.getStr("ErrorInfo");
        if (errorCode != 0) {
            log.error("查询客户IM账号失败：{}", errorInfo);
            throw new HisException("客服系统异常");
        }

        // 获取ResultItem
        JSONArray resultItem = result.getJSONArray("ResultItem");
        JSONObject item = resultItem.getJSONObject(0);
        String accountStatus = item.getStr("AccountStatus");

        return ACCOUNT_IMPORTED.equals(accountStatus);
    }

    /**
     * 构建请求的URL路径
     *
     * @param apiPath  调用API的路径
     * @param adminSig 管理员的安全签名
     * @return 构建之后的URL。
     */
    private String buildUrl(String apiPath, String adminSig) {
        return BASE_URL + apiPath
                + "?sdkappid=" + sdkAppId
                + "&identifier=" + managerId
                + "&usersig=" + adminSig
                + "&random=" + RandomUtil.randomInt(99999999)
                + "&contenttype=json";
    }
}
