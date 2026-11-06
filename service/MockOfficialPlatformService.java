package com.volunteer.service;

import com.volunteer.entity.ServiceRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 模拟官方平台服务
 * 用于模拟与官方志愿平台的对接功能
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Service
public class MockOfficialPlatformService {
    
    private static final Random random = new Random();
    
    /**
     * 模拟时长认证
     * @param userId 用户ID
     * @param hours 服务时长
     * @param activityName 活动名称
     * @return 认证是否成功
     */
    public boolean verifyServiceHours(Long userId, Double hours, String activityName) {
        // 模拟认证逻辑：100%成功率
        try {
            // 模拟网络延迟
            Thread.sleep(500 + random.nextInt(1000));
            
            // 模拟认证结果 - 100%成功率
            boolean success = true;
            
            System.out.println("官方平台认证成功 - 用户ID: " + userId + 
                ", 时长: " + hours + "小时, 活动: " + activityName);
            
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 模拟证书生成
     * @param userId 用户ID
     * @param userName 用户姓名
     * @param totalHours 总服务时长
     * @return 证书编号
     */
    public String generateCertificate(Long userId, String userName, Double totalHours) {
        // 生成模拟证书编号
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String certificateNumber = "CERT_" + timestamp + "_" + userId;
        
        System.out.println("生成志愿服务证书 - 用户: " + userName + 
            ", 证书编号: " + certificateNumber + 
            ", 总时长: " + totalHours + "小时");
        
        return certificateNumber;
    }
    
    /**
     * 模拟平台同步
     * @param userId 用户ID
     * @param record 服务记录
     * @return 同步是否成功
     */
    public boolean syncToOfficialPlatform(Long userId, ServiceRecord record) {
        try {
            // 模拟网络延迟
            Thread.sleep(300 + random.nextInt(500));
            
            // 模拟同步结果 - 100%成功率
            boolean success = true;
            
            System.out.println("同步到官方平台成功 - 用户ID: " + userId + 
                ", 记录ID: " + record.getId() + 
                ", 时长: " + record.getServiceHours() + "小时");
            
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 模拟获取官方平台用户信息
     * @param userId 用户ID
     * @return 模拟的平台用户信息
     */
    public String getOfficialPlatformUserInfo(Long userId) {
        return "官方平台用户ID: OFFICIAL_" + userId + 
               ", 认证状态: 已认证, 平台等级: " + (random.nextInt(5) + 1) + "星";
    }
    
    /**
     * 模拟验证证书有效性
     * @param certificateNumber 证书编号
     * @return 验证结果
     */
    public boolean verifyCertificate(String certificateNumber) {
        try {
            // 模拟网络延迟
            Thread.sleep(200 + random.nextInt(300));
            
            // 模拟验证结果 - 100%有效
            boolean valid = true;
            
            System.out.println("证书验证结果: " + certificateNumber + " - 有效");
            
            return valid;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 模拟官方平台时长认证并生成证书
     * @param userId 用户ID
     * @param totalHours 总服务时长
     * @return 认证结果
     */
    public Map<String, Object> authenticateAndGenerateCertificate(Long userId, Double totalHours) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 模拟时长认证
            boolean authSuccess = verifyServiceHours(userId, totalHours, "志愿服务时长认证");
            
            if (authSuccess) {
                // 2. 生成官方平台证书编号
                String officialCertNumber = generateOfficialCertificateNumber(userId, totalHours);
                
                // 3. 模拟证书生成
                Map<String, Object> certInfo = new HashMap<>();
                certInfo.put("certificateNumber", officialCertNumber);
                certInfo.put("officialPlatformId", "OFFICIAL_" + userId);
                certInfo.put("verificationUrl", "https://volunteer.official.gov.cn/verify/" + officialCertNumber);
                certInfo.put("issueDate", LocalDate.now());
                certInfo.put("validUntil", LocalDate.now().plusYears(2));
                
                result.put("success", true);
                result.put("message", "官方平台认证成功，证书已生成");
                result.put("certificateInfo", certInfo);
            } else {
                result.put("success", false);
                result.put("message", "官方平台认证失败，请检查服务记录");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "认证过程出现异常: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 生成官方平台证书编号
     * @param userId 用户ID
     * @param totalHours 总服务时长
     * @return 证书编号
     */
    private String generateOfficialCertificateNumber(Long userId, Double totalHours) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String level = totalHours >= 100 ? "GOLD" : totalHours >= 50 ? "SILVER" : "BRONZE";
        return "OFFICIAL_" + level + "_" + timestamp + "_" + userId;
    }
}
