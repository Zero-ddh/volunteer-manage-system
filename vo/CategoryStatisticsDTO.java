package com.volunteer.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 活动分类统计DTO
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Data
public class CategoryStatisticsDTO {
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类编码
     */
    private String categoryCode;
    
    /**
     * 该分类下的活动数量
     */
    private Long activityCount;
    
    /**
     * 总报名人数
     */
    private Long totalRegistrations;
    
    /**
     * 已签到人数
     */
    private Long checkinCount;
    
    /**
     * 总服务时长（小时）
     */
    private BigDecimal totalServiceHours;
    
    /**
     * 完成率（%）
     */
    private BigDecimal completionRate;
}


