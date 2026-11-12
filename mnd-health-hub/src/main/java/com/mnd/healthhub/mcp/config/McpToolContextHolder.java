package com.mnd.healthhub.mcp.config;

/**
 * MCP工具上下文持有者
 */
public final class McpToolContextHolder { // 不允许继承
    // 不允许创建对象
    private McpToolContextHolder(){}

    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    /**
     * 将Token绑定到当前线程中。
     */
    public static void setToken(String token){
        TOKEN_HOLDER.set(token);
    }

    /**
     * 从当前线程中获取token
     */
    public static String getToken(){
        return TOKEN_HOLDER.get();
    }

    /**
     * 将token从当前线程中删除
     * Tomcat服务器是多线程，并且Tomcat服务器的线程是可复用的。
     * 如果不清楚，会导致用户错乱。
     */
    public static void clear(){
        TOKEN_HOLDER.remove();
    }
}
