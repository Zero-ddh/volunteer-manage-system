package com.volunteer.service;

import com.volunteer.entity.UserCertificate;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.ServiceRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 证书自动生成服务
 * 用于基于服务时长自动生成志愿服务证书
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Service
public class CertificateGenerationService {
    
    @Autowired
    private MockOfficialPlatformService mockPlatformService;
    
    @Autowired
    private UserCertificateService userCertificateService;
    
    @Autowired
    private VolunteerUserService volunteerUserService;
    
    @Autowired
    private ServiceRecordMapper serviceRecordMapper;
    
    /**
     * 生成志愿服务证书
     * @param userId 用户ID
     * @return 证书编号
     */
    @Transactional
    public String generateVolunteerCertificate(Long userId) {
        try {
            // 1. 获取用户信息
            VolunteerUser user = volunteerUserService.getById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 2. 获取用户总服务时长
            Double totalHours = serviceRecordMapper.selectTotalServiceHours(userId);
            if (totalHours == null || totalHours <= 0) {
                throw new RuntimeException("服务时长不足，无法生成证书");
            }
            
            // 3. 检查是否已有证书
            if (hasExistingCertificate(userId)) {
                throw new RuntimeException("该用户已有志愿服务证书");
            }
            
            // 4. 生成证书编号
            String certificateNumber = mockPlatformService.generateCertificate(
                userId, user.getRealName(), totalHours);
            
            // 5. 创建证书记录
            UserCertificate certificate = new UserCertificate();
            certificate.setUserId(userId);
            certificate.setSkillId(null); // 志愿服务证书不需要关联特定技能，设置为null
            certificate.setCertificateName("志愿服务证书");
            certificate.setCertificateNumber(certificateNumber);
            certificate.setIssuingAuthority("志愿服务平台");
            certificate.setIssueDate(LocalDate.now());
            certificate.setStatus(2); // 已通过状态
            certificate.setAuditRemark("系统自动生成");
            
            // 6. 保存证书
            boolean saved = userCertificateService.save(certificate);
            if (!saved) {
                throw new RuntimeException("证书保存失败");
            }
            
            return certificateNumber;
            
        } catch (Exception e) {
            throw new RuntimeException("证书生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否已有志愿服务证书
     * @param userId 用户ID
     * @return 是否已有证书
     */
    private boolean hasExistingCertificate(Long userId) {
        // 查询用户是否已有志愿服务证书
        return userCertificateService.getUserCertificates(userId).stream()
            .anyMatch(cert -> "志愿服务证书".equals(cert.getCertificateName()));
    }
    
    /**
     * 生成证书并返回详细信息
     * @param userId 用户ID
     * @return 证书信息
     */
    @Transactional
    public Map<String, Object> generateCertificateWithDetails(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 生成证书
            String certificateNumber = generateVolunteerCertificate(userId);
            
            // 获取用户信息
            VolunteerUser user = volunteerUserService.getById(userId);
            Double totalHours = serviceRecordMapper.selectTotalServiceHours(userId);
            
            // 构建返回信息
            result.put("success", true);
            result.put("certificateNumber", certificateNumber);
            result.put("userName", user.getRealName());
            result.put("totalHours", totalHours);
            result.put("issueDate", LocalDate.now().toString());
            result.put("message", "志愿服务证书生成成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 批量生成证书（管理员功能）
     * @param userIds 用户ID列表
     * @return 生成结果统计
     */
    @Transactional
    public Map<String, Object> batchGenerateCertificates(java.util.List<Long> userIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        java.util.List<String> failedUsers = new java.util.ArrayList<>();
        
        for (Long userId : userIds) {
            try {
                generateVolunteerCertificate(userId);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failedUsers.add("用户ID: " + userId + " - " + e.getMessage());
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failedUsers", failedUsers);
        result.put("message", String.format("批量生成完成，成功: %d, 失败: %d", successCount, failCount));
        
        return result;
    }
    
    /**
     * 生成官方认证的志愿服务证书
     * @param userId 用户ID
     * @return 证书生成结果
     */
    @Transactional
    public Map<String, Object> generateOfficialVolunteerCertificate(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取用户信息和总时长
            VolunteerUser user = volunteerUserService.getById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            Double totalHours = user.getTotalServiceHours() != null ? 
                user.getTotalServiceHours().doubleValue() : 0.0;
            if (totalHours == null || totalHours < 10) {
                throw new RuntimeException("服务时长不足10小时，无法生成官方认证证书");
            }
            
            // 2. 允许重新生成官方认证证书（服务时长会不断增加）
            // 移除限制：用户可以基于新的服务时长重新生成证书
            
            // 3. 调用官方平台认证
            Map<String, Object> authResult = mockPlatformService.authenticateAndGenerateCertificate(userId, totalHours);
            
            if (!(Boolean) authResult.get("success")) {
                throw new RuntimeException((String) authResult.get("message"));
            }
            
            // 4. 创建官方认证证书
            @SuppressWarnings("unchecked")
            Map<String, Object> certInfo = (Map<String, Object>) authResult.get("certificateInfo");
            UserCertificate certificate = new UserCertificate();
            certificate.setUserId(userId);
            certificate.setSkillId(null); // 官方认证志愿服务证书不需要关联特定技能，设置为null
            certificate.setCertificateName("官方认证志愿服务证书");
            certificate.setCertificateNumber((String) certInfo.get("certificateNumber"));
            certificate.setIssuingAuthority("官方志愿服务平台");
            certificate.setIssueDate(LocalDate.now());
            certificate.setExpiryDate((LocalDate) certInfo.get("validUntil"));
            certificate.setStatus(2); // 已通过状态
            certificate.setAuditRemark("官方平台自动认证通过");
            
            // 5. 保存证书
            boolean saved = userCertificateService.save(certificate);
            if (!saved) {
                throw new RuntimeException("证书保存失败");
            }
            
            // 6. 构建返回结果
            result.put("success", true);
            result.put("message", "官方认证证书生成成功");
            result.put("certificateNumber", certificate.getCertificateNumber());
            result.put("verificationUrl", certInfo.get("verificationUrl"));
            result.put("totalHours", totalHours);
            result.put("certificateLevel", getCertificateLevel(totalHours));
            result.put("userName", user.getRealName());
            result.put("issueDate", LocalDate.now().toString());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取证书等级
     * @param totalHours 总服务时长
     * @return 证书等级
     */
    private String getCertificateLevel(Double totalHours) {
        if (totalHours >= 100) return "金牌志愿者";
        if (totalHours >= 50) return "银牌志愿者";
        if (totalHours >= 20) return "铜牌志愿者";
        return "普通志愿者";
    }
}
