package com.mnd.healthhub.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应结果封装类
 * 提供链式操作，用于规范所有Controller方法的返回格式
 *
 * <p>使用示例：
 * <pre>
 * // 1. 成功返回，无数据
 * return R.ok();
 *
 * // 2. 成功返回，自定义消息
 * return R.ok("保存成功");
 *
 * // 3. 成功返回，带数据
 * return R.ok().put("data", user);
 *
 * // 4. 成功返回，批量添加数据
 * Map&lt;String, Object&gt; map = new HashMap&lt;&gt;();
 * map.put("list", userList);
 * map.put("total", 100);
 * return R.ok(map);
 *
 * // 5. 错误返回，自定义错误码和消息
 * return R.error(400, "用户名不能为空");
 *
 * // 6. 错误返回，使用默认错误码500
 * return R.error("系统异常");
 *
 * // 7. 错误返回，使用默认错误码和消息
 * return R.error();
 * </pre>
 */
@Data
@Schema(description = "统一API响应结果")
public class R {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "success")
    private String msg;

    @Schema(description = "响应数据（动态字段）")
    private Map<String, Object> data = new HashMap<>();

    /**
     * 默认构造器：初始化一个成功的响应
     */
    public R() {
        this.code = HttpStatus.SC_OK;
        this.msg = "success";
    }

    /**
     * 添加数据，支持链式调用
     */
    public R put(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 批量添加数据
     */
    public R putAll(Map<String, Object> map) {
        if (map != null) {
            this.data.putAll(map);
        }
        return this;
    }

    /**
     * 创建一个表示【成功】的响应对象（无附加数据）
     */
    public static R ok() {
        return new R();
    }

    /**
     * 创建一个表示【成功】的响应对象，并自定义成功消息
     */
    public static R ok(String msg) {
        R r = new R();
        r.setMsg(msg);
        return r;
    }

    /**
     * 创建一个表示【成功】的响应对象，并批量添加数据
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    /**
     * 创建一个表示【错误】的响应对象，并指定错误码和错误信息
     */
    public static R error(int code, String msg) {
        R r = new R();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    /**
     * 创建一个表示【错误】的响应对象，使用默认错误码（500）
     */
    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    /**
     * 创建一个表示【错误】的响应对象，使用默认错误码和错误信息
     */
    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }
}