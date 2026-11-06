package com.volunteer.controller;

import com.volunteer.dto.CertificateAuditDTO;
import com.volunteer.dto.UserCertificateDTO;
import com.volunteer.dto.UserSkillDTO;
import com.volunteer.entity.SkillCategory;
import com.volunteer.entity.UserCertificate;
import com.volunteer.entity.UserSkill;
import com.volunteer.service.SkillCategoryService;
import com.volunteer.service.UserCertificateService;
import com.volunteer.service.UserSkillService;
import com.volunteer.vo.PageResult;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 技能管理控制器
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Slf4j
@RestController
@RequestMapping("/v1/skills")
public class SkillController {

    @Resource
    private SkillCategoryService skillCategoryService;
    
    @Resource
    private UserSkillService userSkillService;
    
    @Resource
    private UserCertificateService userCertificateService;

    /**
     * 获取技能分类列表
     */
    @GetMapping("/categories")
    public Result<List<SkillCategory>> getCategories() {
        try {
            List<SkillCategory> categories = skillCategoryService.getEnabledCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取技能分类列表失败", e);
            return Result.error("获取技能分类列表失败");
        }
    }

    /**
     * 获取技能分类树形结构
     */
    @GetMapping("/categories/tree")
    public Result<List<SkillCategory>> getCategoryTree() {
        try {
            List<SkillCategory> tree = skillCategoryService.getCategoryTree();
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取技能分类树失败", e);
            return Result.error("获取技能分类树失败");
        }
    }

    /**
     * 获取用户技能列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<UserSkillDTO>> getUserSkills(@PathVariable Long userId) {
        try {
            List<UserSkillDTO> skills = userSkillService.getUserSkills(userId);
            return Result.success(skills);
        } catch (Exception e) {
            log.error("获取用户技能列表失败", e);
            return Result.error("获取用户技能列表失败");
        }
    }

    /**
     * 添加用户技能
     */
    @PostMapping("/user")
    public Result<Boolean> addUserSkill(@RequestBody UserSkillDTO skillDTO) {
        try {
            UserSkill skill = new UserSkill();
            BeanUtils.copyProperties(skillDTO, skill);
            skill.setVerified(0); // 默认未验证
            boolean result = userSkillService.save(skill);
            return Result.success(result);
        } catch (Exception e) {
            log.error("添加用户技能失败", e);
            return Result.error("添加用户技能失败");
        }
    }

    /**
     * 更新用户技能
     */
    @PutMapping("/user/{id}")
    public Result<Boolean> updateUserSkill(@PathVariable Long id, @RequestBody UserSkillDTO skillDTO) {
        try {
            UserSkill skill = userSkillService.getById(id);
            if (skill == null) {
                return Result.error("技能不存在");
            }
            BeanUtils.copyProperties(skillDTO, skill);
            skill.setId(id);
            boolean result = userSkillService.updateById(skill);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新用户技能失败", e);
            return Result.error("更新用户技能失败");
        }
    }

    /**
     * 删除用户技能
     */
    @DeleteMapping("/user/{id}")
    public Result<Boolean> deleteUserSkill(@PathVariable Long id) {
        try {
            boolean result = userSkillService.removeById(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除用户技能失败", e);
            return Result.error("删除用户技能失败");
        }
    }

    /**
     * 上传技能证书
     */
    @PostMapping("/certificate/upload")
    public Result<Map<String, Object>> uploadCertificate(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            
            // 允许的文件类型
            if (!isAllowedFileType(contentType, fileExtension)) {
                return Result.error("只支持PDF、JPG、PNG格式的证书文件");
            }

            // 验证文件大小 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 重定向到文件上传控制器
            return Result.error("请使用 /v1/upload/certificate 接口上传证书文件");

        } catch (Exception e) {
            log.error("证书文件上传失败", e);
            return Result.error("证书文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 添加技能证书
     */
    @PostMapping("/certificate")
    public Result<Boolean> addCertificate(@RequestBody UserCertificate certificate) {
        try {
            log.info("接收证书数据: {}", certificate);
            certificate.setStatus(1); // 待审核状态
            boolean result = userCertificateService.addCertificate(certificate);
            log.info("证书保存结果: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("添加技能证书失败", e);
            return Result.error("添加技能证书失败: " + e.getMessage());
        }
    }

    /**
     * 更新技能证书
     */
    @PutMapping("/certificate/{id}")
    public Result<Boolean> updateCertificate(@PathVariable Long id, @RequestBody UserCertificate certificate) {
        try {
            log.info("更新证书数据: ID={}, 数据={}", id, certificate);
            certificate.setId(id);
            certificate.setStatus(1); // 更新后重新设为待审核状态
            boolean result = userCertificateService.updateById(certificate);
            log.info("证书更新结果: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新技能证书失败", e);
            return Result.error("更新技能证书失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户证书列表
     */
    @GetMapping("/certificate/user/{userId}")
    public Result<List<UserCertificateDTO>> getUserCertificates(@PathVariable Long userId) {
        try {
            List<UserCertificateDTO> certificates = userCertificateService.getUserCertificates(userId);
            return Result.success(certificates);
        } catch (Exception e) {
            log.error("获取用户证书列表失败", e);
            return Result.error("获取用户证书列表失败");
        }
    }

    /**
     * 审核技能证书
     */
    @PutMapping("/certificate/{id}/audit")
    public Result<Boolean> auditCertificate(@PathVariable Long id, @RequestBody CertificateAuditDTO auditDTO, HttpServletRequest request) {
        try {
            auditDTO.setCertificateId(id);
            // 从JWT中获取当前登录用户的ID
            Long currentUserId = (Long) request.getAttribute("userId");
            auditDTO.setAuditorId(currentUserId);
            boolean result = userCertificateService.auditCertificate(auditDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("审核技能证书失败", e);
            return Result.error("审核技能证书失败");
        }
    }

    /**
     * 删除技能证书
     */
    @DeleteMapping("/certificate/{id}")
    public Result<Boolean> deleteCertificate(@PathVariable Long id) {
        try {
            log.info("删除证书: ID={}", id);
            boolean result = userCertificateService.removeById(id);
            log.info("证书删除结果: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除技能证书失败", e);
            return Result.error("删除技能证书失败: " + e.getMessage());
        }
    }

    /**
     * 获取待审核证书列表（支持分页和搜索）
     */
    @GetMapping("/certificate/pending")
    public Result<PageResult<UserCertificateDTO>> getPendingCertificates(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String certificateName,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long skillCategoryId,
            @RequestParam(required = false) Integer status) {
        try {
            PageResult<UserCertificateDTO> result = userCertificateService.getPendingCertificatesWithPage(
                pageNo, pageSize, certificateName, userName, skillCategoryId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取待审核证书列表失败", e);
            return Result.error("获取待审核证书列表失败");
        }
    }

    /**
     * 获取证书详情
     */
    @GetMapping("/certificate/{id}")
    public Result<UserCertificateDTO> getCertificateDetail(@PathVariable Long id) {
        try {
            UserCertificateDTO certificate = userCertificateService.getCertificateDetail(id);
            if (certificate == null) {
                return Result.error("证书不存在");
            }
            return Result.success(certificate);
        } catch (Exception e) {
            log.error("获取证书详情失败", e);
            return Result.error("获取证书详情失败");
        }
    }

    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedFileType(String contentType, String fileExtension) {
        // 允许的MIME类型
        if (contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png")
        )) {
            return true;
        }
        
        // 允许的文件扩展名
        return fileExtension.equals("pdf") || 
               fileExtension.equals("jpg") || 
               fileExtension.equals("jpeg") || 
               fileExtension.equals("png");
    }
}
