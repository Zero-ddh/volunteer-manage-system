package com.volunteer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置类
 * 配置静态资源访问
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        String projectRoot = System.getProperty("user.dir");
        System.out.println("=== 静态资源配置调试信息 ===");
        System.out.println("项目根目录 (user.dir): " + projectRoot);
        System.out.println("配置的上传路径: " + uploadPath);
        
        String uploadDir;
        
        // 检查项目根目录下是否有uploads文件夹
        File projectUploads = new File(projectRoot, uploadPath);
        System.out.println("检查路径: " + projectUploads.getAbsolutePath());
        System.out.println("路径是否存在: " + projectUploads.exists());
        
        if (projectUploads.exists()) {
            uploadDir = projectUploads.getAbsolutePath();
            System.out.println("✓ 使用项目根目录下的uploads: " + uploadDir);
        } else {
            // 如果项目根目录没有，则使用相对路径（相对于jar包位置）
            uploadDir = new File(uploadPath).getAbsolutePath();
            System.out.println("⚠ 使用相对路径uploads: " + uploadDir);
            System.out.println("注意：请确保Spring Boot应用从正确的目录启动！");
        }
        
        // 确保路径以/结尾
        String absolutePath = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath)
                .setCachePeriod(3600);
        
        // 映射带context-path的路径
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + absolutePath)
                .setCachePeriod(3600);
        
        // 配置静态资源访问
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        System.out.println("静态资源映射: /uploads/** -> file:" + absolutePath);
        System.out.println("静态资源映射: /api/uploads/** -> file:" + absolutePath);
        System.out.println("=== 静态资源配置完成 ===");
    }
}
