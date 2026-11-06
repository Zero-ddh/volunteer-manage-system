package com.volunteer.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 志愿者用户详情VO
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Data
public class VolunteerUserDetailVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 性别
     */
    private Integer gender;
    
    /**
     * 性别文本
     */
    private String genderText;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 个人简介
     */
    private String introduction;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 状态文本
     */
    private String statusText;
    
    // 等级字段已屏蔽，改为使用星级字段
    // private String level;
    // private Integer levelValue;
    
    /**
     * 总服务时长（小时）
     */
    private Integer totalServiceHours;
    
    /**
     * 参与活动次数
     */
    private Integer activityCount;
    
    /**
     * 总积分
     */
    private Integer totalPoints;
    
    /**
     * 星级
     */
    private Integer starLevel;
    
    /**
     * 评级时间
     */
    private LocalDateTime starRatingTime;
    
    /**
     * 注册时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 用户技能信息列表
     */
    private List<UserSkillInfo> userSkills;

    /**
     * 用户证书信息列表
     */
    private List<UserCertificateInfo> userCertificates;

    /**
     * 紧急联系人信息列表
     */
    private List<EmergencyContactInfo> emergencyContacts;

    /**
     * 用户技能信息内部类
     */
    @Data
    public static class UserSkillInfo {
        private Long id;
        private String skillName;
        private String skillCategoryName;
        private Integer skillLevel;
        private String skillLevelText;
        private String description;
        private Integer verified;
        private String verifiedText;
        private Long certificateId;
        private String certificateName;
    }

    /**
     * 用户证书信息内部类
     */
    @Data
    public static class UserCertificateInfo {
        private Long id;
        private String certificateName;
        private String issuingAuthority;
        private String certificateNumber;
        private String issueDate;
        private String expiryDate;
        private String certificateFile;
        private String originalFileName;
        private String fileType;
        private Integer status;
        private String statusText;
        private String auditRemark;
        private String auditTime;
    }

    /**
     * 紧急联系人信息内部类
     */
    @Data
    public static class EmergencyContactInfo {
        private Long id;
        private String name;
        private String phone;
        private String relation;
        private Integer isPrimary;
        private String isPrimaryText;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
    }
}
