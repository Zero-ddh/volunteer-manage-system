package com.volunteer.config;

import com.volunteer.interceptor.JwtAuthenticationInterceptor;
import com.volunteer.utils.JwtUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtUtils jwtUtils;

    public WebMvcConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        JwtAuthenticationInterceptor interceptor = new JwtAuthenticationInterceptor();
        interceptor.setJwtUtils(jwtUtils);
        registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/public/**", "/error", "/uploads/**", "/api/uploads/**", 
                    "/api/v1/activities", "/api/v1/activities/*/qr-code", "/api/v1/activity-categories", 
                    "/api/v1/goods/list", "/api/v1/community/list", "/api/v1/community/*", "/api/v1/community/*/summary",
                    "/api/v1/activity-summaries");
                    // "/api/v1/community/*/posts" - 已屏蔽帖子功能
    }
}


