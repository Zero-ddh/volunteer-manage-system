package com.volunteer.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 志愿者用户列表VO
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Data
public class VolunteerUserListVO {
    
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
     * 性别文本
     */
    private String genderText;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 状态文本
     */
    private String statusText;
    
    /**
     * 状态值
     */
    private Integer status;
    
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
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}
