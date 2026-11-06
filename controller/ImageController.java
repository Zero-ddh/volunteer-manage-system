package com.volunteer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * 图片访问控制器
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Slf4j
@RestController
@RequestMapping("/uploads")
public class ImageController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 获取图片文件
     */
    @GetMapping("/images/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        try {
            // 获取请求路径
            String requestPath = request.getRequestURI();
            String imagePath = requestPath.substring("/api/uploads/images/".length());
            
            // 构建文件路径
            String filePath = uploadPath + "/images/" + imagePath;
            File file = new File(filePath);
            
            log.info("请求图片路径: {}", filePath);
            log.info("文件是否存在: {}", file.exists());
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(file.length());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("获取图片失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
