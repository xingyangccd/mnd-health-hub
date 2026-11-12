package com.mnd.healthhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("美年大健康系统接口文档")
                        .description("美年大健康系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("美年大健康")
                                .email("mnd-health@tj.com")
                                .url("http://localhost:8080")));
    }
}