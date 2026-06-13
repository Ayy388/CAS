package com.cas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI casOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CAS 高校选课管理系统 API")
                        .description("Course Administration System - Backend API 文档")
                        .version("1.0.0")
                        .contact(new Contact().name("CAS Team"))
                        .license(new License().name("MIT")));
    }
}