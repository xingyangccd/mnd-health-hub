package com.mnd.healthhub.config;

import cn.dev33.satoken.exception.NotLoginException;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.exception.HisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    /**
     * 用户未登录，访问需要登录的接口时抛出
     * 例如：未携带token或token已过期
     */
    @ExceptionHandler(NotLoginException.class)
    public R handleNotLogin(NotLoginException e) {
        log.warn("用户未登录：{}", e.getMessage());
        return R.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    /**
     * 业务异常，手动抛出
     * 例如：用户不存在、余额不足、库存不足等
     */
    @ExceptionHandler(HisException.class)
    public R handleHis(HisException e) {
        log.warn("业务异常：{}", e.getMessage(), e);
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数绑定异常，使用@ModelAttribute接收参数时类型不匹配
     * 例如：前端传入 "abc" 绑定到 Integer 类型字段
     */
    @ExceptionHandler(BindException.class)
    public R handleBind(BindException e) {
        String msg = e.getFieldError() != null ? e.getFieldError().getDefaultMessage() : "参数绑定失败";
        log.warn("参数绑定异常：{}", msg);
        return R.error(HttpStatus.BAD_REQUEST.value(), msg);
    }

    /**
     * 参数校验异常，使用@Valid校验请求体失败时抛出
     * 例如：@NotBlank(message = "用户名不能为空") 但前端传了空字符串
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null ? e.getBindingResult().getFieldError().getDefaultMessage() : "参数校验失败";
        log.warn("参数校验异常：{}", msg);
        return R.error(HttpStatus.BAD_REQUEST.value(), msg);
    }

    /**
     * 请求体解析异常，JSON格式错误或类型不匹配
     * 例如：前端传入 {"age": "abc"} 但后端接收 Integer 类型
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R handleHttpMsgNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析异常：{}", e.getMessage());
        return R.error(HttpStatus.BAD_REQUEST.value(), "请求数据格式错误，请检查参数类型");
    }

    /**
     * 缺少请求文件/字段，使用@RequestPart接收文件但前端未上传
     * 例如：接口接收 MultipartFile 但前端未传文件
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public R handleMissingRequestPart(MissingServletRequestPartException e) {
        log.warn("缺少请求文件/字段：{}", e.getMessage());
        return R.error(HttpStatus.BAD_REQUEST.value(), "缺少必要的请求参数或文件");
    }

    /**
     * 请求方法不支持，使用错误的HTTP方法访问接口
     * 例如：接口是 @PostMapping，但前端用 GET 请求
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R handleHttpMethodNotSupport(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMessage());
        return R.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "不支持的请求方法");
    }

    /**
     * 其他未捕获的异常，兜底处理
     * 例如：NullPointerException、空指针等未知系统异常
     */
    @ExceptionHandler(Exception.class)
    public R handle(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return R.error();
    }
}