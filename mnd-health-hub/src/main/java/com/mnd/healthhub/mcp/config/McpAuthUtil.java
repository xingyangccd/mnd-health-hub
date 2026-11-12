package com.mnd.healthhub.mcp.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.util.StrUtil;
import com.mnd.healthhub.config.satoken.StpCustomerUtil;
import com.mnd.healthhub.exception.HisException;

/**
 * MCP 工具鉴权工具类
 * <p>
 * MCP 工具方法不经过 Sa-Token 拦截器，需手动从 Token 解析用户身份。
 * 所有需要登录的 MCP 工具方法都必须调用本类方法获取真实用户ID，
 * 禁止信任外部传入的 customerId，防止身份伪造。
 * <p>
 * 适用范围：业务端客户（StpCustomerUtil，type=customer）
 */
public final class McpAuthUtil {

    private McpAuthUtil() {
    }

    /**
     * 从当前上下文中解析业务端客户ID。
     * @return 客户ID
     */
    public static int requireCustomerId() {
        String token = McpToolContextHolder.getToken();
        if (StrUtil.isBlank(token)) {
            throw new HisException("请先登录：当前用户缺少token", 401);
        }
        try {
            Object loginId = StpCustomerUtil.getLoginIdByToken(token);
            if (loginId == null) {
                throw new HisException("请先登录：Token无效或已过期", 401);
            }
            return Integer.parseInt(loginId.toString());
        } catch (NotLoginException e) {
            throw new HisException("请先登录：Token无效或已过期", 401);
        } catch (NumberFormatException e) {
            throw new HisException("Token解析失败：客户ID格式异常", 401);
        }
    }

    /**
     * 从 Token 解析业务端客户ID
     *
     * @param token 登录令牌（由 /front/customer/login 接口颁发）
     * @return 客户ID
     * @throws HisException 未登录或 Token 无效时抛出
     */
    public static int requireCustomerId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new HisException("请先登录：缺少用户Token", 401);
        }
        try {
            Object loginId = StpCustomerUtil.getLoginIdByToken(token);
            if (loginId == null) {
                throw new HisException("请先登录：Token 无效或已过期", 401);
            }
            return Integer.parseInt(loginId.toString());
        } catch (NotLoginException e) {
            throw new HisException("请先登录：Token 无效或已过期", 401);
        } catch (NumberFormatException e) {
            throw new HisException("Token 解析失败：用户ID格式异常", 401);
        }
    }
}