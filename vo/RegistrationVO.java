package com.volunteer.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报名信息VO
 * 
 * @author volunteer-team
 * @since 2025-01-21
 */
@Data
public class RegistrationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报名ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动描述
     */
    private String activityDescription;

    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityStartTime;

    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityEndTime;

    /**
     * 活动地点
     */
    private String activityLocation;

    /**
     * 活动分类ID
     */
    private Long activityCategoryId;

    /**
     * 活动分类名称
     */
    private String activityCategoryName;

    /**
     * 活动要求
     */
    private String activityRequirements;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 报名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationTime;

    /**
     * 状态：1-待审核，2-已通过，3-已拒绝，4-已取消
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 用户技能信息列表
     */
    private List<UserSkillInfo> userSkills;

    /**
     * 用户证书信息列表
     */
    private List<UserCertificateInfo> userCertificates;

    /**
     * 技能匹配度分数（0-100）
     */
    private Double skillMatchScore;

    /**
     * 活动技能要求列表
     */
    private List<ActivitySkillRequirementInfo> activitySkillRequirements;

    /**
     * 用户技能信息内部类
     */
    @Data
    public static class UserSkillInfo implements Serializable {
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
    public static class UserCertificateInfo implements Serializable {
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
     * 活动技能要求信息内部类
     */
    @Data
    public static class ActivitySkillRequirementInfo implements Serializable {
        private Long id;
        private String skillName;
        private String skillCategoryName;
        private Integer requiredLevel;
        private String requiredLevelText;
        private Integer isRequired;
        private String isRequiredText;
        private String description;
        private Boolean isMatched;
        private String matchedSkillLevel;
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待审核";
            case 2:
                return "已通过";
            case 3:
                return "已拒绝";
            case 4:
                return "已取消";
            default:
                return "未知";
        }
    }
}
