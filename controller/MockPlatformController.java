package com.volunteer.controller;

import com.volunteer.vo.Result;
import com.volunteer.entity.ServiceRecord;
import com.volunteer.entity.UserCertificate;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.service.CertificateGenerationService;
import com.volunteer.service.MockOfficialPlatformService;
import com.volunteer.service.ServiceRecordService;
import com.volunteer.service.UserCertificateService;
import com.volunteer.service.VolunteerUserService;
import com.volunteer.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 * 模拟平台控制器
 * 提供模拟官方平台对接的API接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/v1/platform")
public class MockPlatformController {
    
    @Autowired
    private ServiceRecordService serviceRecordService;
    
    @Autowired
    private CertificateGenerationService certificateService;
    
    @Autowired
    private MockOfficialPlatformService mockPlatformService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserCertificateService userCertificateService;
    
    @Autowired
    private VolunteerUserService volunteerUserService;
    
    /**
     * 自动计算服务时长
     */
    @PostMapping("/auto-calculate")
    public Result<Map<String, Object>> autoCalculateServiceHours(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            
            // 批量自动计算用户服务时长
            int successCount = serviceRecordService.batchAutoCalculateServiceHours(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("message", "成功计算 " + successCount + " 条服务记录");
            
            log.info("用户 {} 自动计算服务时长完成，成功数量: {}", userId, successCount);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("自动计算服务时长失败", e);
            return Result.error("自动计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成志愿服务证书
     */
    @PostMapping("/generate-certificate")
    public Result<Map<String, Object>> generateCertificate(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            
            // 生成证书并获取详细信息
            Map<String, Object> certificateInfo = certificateService.generateCertificateWithDetails(userId);
            
            if ((Boolean) certificateInfo.get("success")) {
                log.info("用户 {} 生成志愿服务证书成功，证书编号: {}", 
                    userId, certificateInfo.get("certificateNumber"));
                return Result.success(certificateInfo);
            } else {
                return Result.error((String) certificateInfo.get("message"));
            }
            
        } catch (Exception e) {
            log.error("生成志愿服务证书失败", e);
            return Result.error("证书生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟同步到官方平台
     */
    @PostMapping("/sync")
    public Result<Map<String, Object>> syncToPlatform(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            
            // 获取用户最近的服务记录
            List<ServiceRecord> records = serviceRecordService.getRecentRecords(userId);
            
            int syncedCount = 0;
            int totalCount = records.size();
            
            for (ServiceRecord record : records) {
                if (mockPlatformService.syncToOfficialPlatform(userId, record)) {
                    syncedCount++;
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("syncedCount", syncedCount);
            result.put("successRate", totalCount > 0 ? (double) syncedCount / totalCount : 0);
            result.put("message", String.format("成功同步 %d/%d 条记录到官方平台", syncedCount, totalCount));
            
            log.info("用户 {} 同步到官方平台完成，成功数量: {}/{}", userId, syncedCount, totalCount);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("同步到官方平台失败", e);
            return Result.error("同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟时长认证
     */
    @PostMapping("/verify-hours")
    public Result<Map<String, Object>> verifyServiceHours(HttpServletRequest request, 
                                                          @RequestParam Long activityId,
                                                          @RequestParam Double hours) {
        try {
            Long userId = getCurrentUserId(request);
            
            // 获取活动信息用于认证
            String activityName = "活动ID: " + activityId; // 简化处理
            
            // 模拟认证
            boolean verified = mockPlatformService.verifyServiceHours(userId, hours, activityName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("verified", verified);
            result.put("userId", userId);
            result.put("activityId", activityId);
            result.put("hours", hours);
            result.put("message", verified ? "时长认证成功" : "时长认证失败");
            
            log.info("用户 {} 时长认证结果: {}, 活动: {}, 时长: {}小时", 
                userId, verified, activityId, hours);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("时长认证失败", e);
            return Result.error("认证失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取官方平台用户信息
     */
    @GetMapping("/user-info")
    public Result<Map<String, Object>> getOfficialPlatformUserInfo(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            
            // 获取模拟的平台用户信息
            String platformInfo = mockPlatformService.getOfficialPlatformUserInfo(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("platformInfo", platformInfo);
            result.put("syncStatus", "已连接");
            result.put("lastSyncTime", java.time.LocalDateTime.now().toString());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取官方平台用户信息失败", e);
            return Result.error("获取信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证证书有效性
     */
    @PostMapping("/verify-certificate")
    public Result<Map<String, Object>> verifyCertificate(@RequestParam String certificateNumber) {
        try {
            // 模拟证书验证
            boolean valid = mockPlatformService.verifyCertificate(certificateNumber);
            
            Map<String, Object> result = new HashMap<>();
            result.put("certificateNumber", certificateNumber);
            result.put("valid", valid);
            result.put("verifyTime", java.time.LocalDateTime.now().toString());
            result.put("message", valid ? "证书有效" : "证书无效");
            
            log.info("证书验证结果: {} - {}", certificateNumber, valid ? "有效" : "无效");
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("证书验证失败", e);
            return Result.error("验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成官方认证证书
     */
    @PostMapping("/generate-official-certificate")
    public Result<Map<String, Object>> generateOfficialCertificate(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            
            Map<String, Object> result = certificateService.generateOfficialVolunteerCertificate(userId);
            
            if ((Boolean) result.get("success")) {
                log.info("用户 {} 生成官方认证证书成功，证书编号: {}", 
                    userId, result.get("certificateNumber"));
                return Result.success(result);
            } else {
                return Result.error((String) result.get("message"));
            }
            
        } catch (Exception e) {
            log.error("生成官方认证证书失败", e);
            return Result.error("证书生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证官方证书有效性
     */
    @PostMapping("/verify-official-certificate")
    public Result<Map<String, Object>> verifyOfficialCertificate(@RequestBody Map<String, String> request) {
        try {
            String certificateNumber = request.get("certificateNumber");
            
            if (!StringUtils.hasText(certificateNumber)) {
                return Result.error("证书编号不能为空");
            }
            
            // 模拟官方平台验证
            boolean isValid = mockPlatformService.verifyCertificate(certificateNumber);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);
            result.put("certificateNumber", certificateNumber);
            result.put("message", isValid ? "证书验证成功" : "证书验证失败");
            result.put("verifyTime", java.time.LocalDateTime.now().toString());
            
            if (isValid) {
                result.put("verificationUrl", "https://volunteer.official.gov.cn/verify/" + certificateNumber);
            }
            
            log.info("官方证书验证结果: {} - {}", certificateNumber, isValid ? "有效" : "无效");
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("验证官方证书失败", e);
            return Result.error("证书验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取个人档案信息
     */
    @GetMapping("/personal-profile")
    public Result<Map<String, Object>> getPersonalProfile(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            
            // 获取用户基本信息
            Map<String, Object> profile = new HashMap<>();
            profile.put("userId", userId);
            profile.put("lastSyncTime", java.time.LocalDateTime.now().toString());
            profile.put("platformStatus", "已连接");
            
            // 直接从volunteer_user表获取服务时长和星级
            VolunteerUser volunteerUser = volunteerUserService.getById(userId);
            double totalHours = 0.0;
            int starLevel = 0;
            
            if (volunteerUser != null) {
                totalHours = volunteerUser.getTotalServiceHours() != null ? 
                    volunteerUser.getTotalServiceHours().doubleValue() : 0.0;
                starLevel = volunteerUser.getStarLevel() != null ? 
                    volunteerUser.getStarLevel() : 0;
            }
            
            // 获取最近记录用于显示
            List<ServiceRecord> recentRecords = serviceRecordService.getRecentRecords(userId);
            
            profile.put("totalServiceHours", totalHours);
            profile.put("totalRecords", recentRecords.size());
            profile.put("certificateLevel", getCertificateLevel(totalHours));
            profile.put("starLevel", starLevel);
            
            log.info("用户 {} 获取个人档案信息，总时长: {}小时", userId, totalHours);
            return Result.success(profile);
            
        } catch (Exception e) {
            log.error("获取个人档案信息失败", e);
            return Result.error("获取档案信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户证书列表
     */
    @GetMapping("/user-certificates")
    public Result<Map<String, Object>> getUserCertificates(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String issuingAuthority) {
        try {
            Long userId = getCurrentUserId(request);
            
            // 构建查询条件
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCertificate> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCertificate>()
                    .eq(UserCertificate::getUserId, userId)
                    .orderByDesc(UserCertificate::getCreatedTime);
            
            // 如果指定了颁发机构，则添加过滤条件
            if (issuingAuthority != null && !issuingAuthority.trim().isEmpty()) {
                wrapper.eq(UserCertificate::getIssuingAuthority, issuingAuthority);
            }
            
            // 获取用户证书列表
            List<UserCertificate> certificates = userCertificateService.list(wrapper);
            
            // 分页处理
            int total = certificates.size();
            int start = (pageNo - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            
            List<UserCertificate> pagedCertificates = certificates.subList(start, end);
            
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("records", pagedCertificates);
            result.put("total", total);
            result.put("pageNo", pageNo);
            result.put("pageSize", pageSize);
            
            log.info("用户 {} 获取证书列表，总数: {}, 当前页: {}", userId, total, pageNo);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取用户证书列表失败", e);
            return Result.error("获取证书列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取证书等级
     */
    private String getCertificateLevel(Double totalHours) {
        if (totalHours >= 100) return "金牌志愿者";
        if (totalHours >= 50) return "银牌志愿者";
        if (totalHours >= 20) return "铜牌志愿者";
        return "普通志愿者";
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到有效的用户令牌");
    }
}
