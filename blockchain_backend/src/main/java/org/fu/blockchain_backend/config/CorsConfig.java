package org.fu.blockchain_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有的路径
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8888", "http://127.0.0.1:8888")
                // 允许所有的请求方法 (GET, POST, PUT, DELETE等)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有的请求头
                .allowedHeaders("*")
                // 允许携带 Cookie (如果以后用到登录状态)
                .allowCredentials(true)
                // 跨域检查的有效期（秒）
                .maxAge(3600);
    }
}