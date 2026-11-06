package com.volunteer.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 志愿者年度报告DTO
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Data
public class VolunteerYearlyReportDTO {
    
    /**
     * 志愿者ID
     */
    private Long userId;
    
    /**
     * 姓名
     */
    private String realName;
    
    /**
     * 年度
     */
    private Integer year;
    
    /**
     * 全年总服务时长（小时）
     */
    private BigDecimal totalServiceHours;
    
    /**
     * 参与活动次数
     */
    private Integer activityCount;
    
    /**
     * 获得积分
     */
    private Long totalPoints;
    
    /**
     * 服务天数
     */
    private Integer serviceDays;
    
    /**
     * 服务领域分布（按分类统计）
     */
    private List<Map<String, Object>> categoryDistribution;
    
    /**
     * 在志愿者中的排名
     */
    private Integer ranking;
    
    /**
     * 星级
     */
    private Integer starLevel;
    
    /**
     * 每月服务时长统计
     */
    private List<Map<String, Object>> monthlyStatistics;
    
    /**
     * 服务地点统计
     */
    private List<Map<String, Object>> locationStatistics;
}


