package com.mnd.healthhub.config.xss;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class XSSFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 将 ServletRequest 转换成 HttpServletRequest
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 把 HttpServletRequest 对象包装成 XSSHttpServletRequestWrapper对象。
        XSSHttpServletRequestWrapper requestWrapper = new XSSHttpServletRequestWrapper(request);
        // 往下传递的时候不要传递 servletRequest，传递 requestWrapper
        filterChain.doFilter(requestWrapper, servletResponse);
    }
}
