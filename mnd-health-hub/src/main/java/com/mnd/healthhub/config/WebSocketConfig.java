package com.mnd.healthhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration // 配置websocket的一个配置类。
public class WebSocketConfig {

    // 配置了一个Bean：ServerEndpointExporter（服务器 端点 注册器）
    // ServerEndpointExporter 这个Bean的作用：
    // 1. 扫描注解：查找所有被 @ServerEndpoint 标注的类，@ServerEndpoint 标注的类称为：websocket服务器端点。
    //    关于websocket服务器端点：一个业务一般对应一个端点。
    //    一个端点支持多个socket连接。（我们这个项目端点类只需要一个即可：一个端点支持多个socket连接。）
    // 2. 注册端点：将这些端点类注册到 WebSocket 服务器中，创建对应的访问地址
    // Tomcat实现了Servlet、JSP、WebSocket等规范。
    // Tomcat服务器本身就是一个Websocket服务器。
    // Tomcat服务器和WebSocket服务器共享同一个端口：8080
    // Websocket访问协议格式： ws://
    // Websocket访问安全协议格式： wss://
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}