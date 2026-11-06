package com.volunteer.controller;

import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/v1/upload")
public class FileUploadController {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Value("${file.upload.base-url:http://localhost:8080/api/uploads}")
    private String baseUrl;

    /**
     * 上传图片
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 验证文件大小 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 创建上传目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, datePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 生成访问URL - 使用完整的baseUrl
            String fileUrl = baseUrl + "/" + datePath + "/" + filename;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", filename);
            result.put("originalName", originalFilename);
            result.put("relativeUrl", urlPrefix + "/" + datePath + "/" + filename); // 相对URL用于数据库存储
            result.put("size", String.valueOf(file.getSize()));

            log.info("图片上传成功: {}", fileUrl);
            return Result.success(result);

        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败");
        }
    }

    /**
     * 上传文件（通用）
     */
    @PostMapping("/file")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证文件大小 (50MB)
            if (file.getSize() > 50 * 1024 * 1024) {
                return Result.error("文件大小不能超过50MB");
            }

            // 创建上传目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, datePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 生成访问URL - 使用完整的baseUrl
            String fileUrl = baseUrl + "/" + datePath + "/" + filename;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", filename);
            result.put("originalName", originalFilename);
            result.put("relativeUrl", urlPrefix + "/" + datePath + "/" + filename); // 相对URL用于数据库存储
            result.put("size", String.valueOf(file.getSize()));

            log.info("文件上传成功: {}", fileUrl);
            return Result.success(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    /**
     * 上传证书文件（仅支持PDF格式）
     */
    @PostMapping("/certificate")
    public Result<Map<String, Object>> uploadCertificate(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            
            // 验证是否为PDF格式
            boolean isValidFileType = false;
            if (originalFilename != null) {
                String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
                isValidFileType = extension.equals("pdf");
            }

            if (!isValidFileType) {
                return Result.error("仅支持PDF格式的证书文件");
            }

            // 验证文件大小 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 创建上传目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, "certificate", datePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 生成访问URL
            String fileUrl = baseUrl + "/certificate/" + datePath + "/" + filename;

            // 确定文件类型
            String fileType = "image";
            if (contentType != null && contentType.equals("application/pdf")) {
                fileType = "pdf";
            }

            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileType", fileType);
            result.put("size", file.getSize());
            result.put("originalName", originalFilename);

            log.info("证书文件上传成功: {}", fileUrl);
            return Result.success(result);

        } catch (IOException e) {
            log.error("证书文件上传失败", e);
            return Result.error("证书文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("证书文件上传失败", e);
            return Result.error("证书文件上传失败");
        }
    }
}