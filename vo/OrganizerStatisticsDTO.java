package com.volunteer.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 组织者统计DTO
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Data
public class OrganizerStatisticsDTO {
    
    /**
     * 组织者ID
     */
    private Long organizerId;
    
    /**
     * 组织者名称
     */
    private String organizerName;
    
    /**
     * 创建的活动总数
     */
    private Long totalActivities;
    
    /**
     * 已结束的活动数
     */
    private Long completedActivities;
    
    /**
     * 进行中的活动数
     */
    private Long ongoingActivities;
    
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
     * 参与活动的不重复志愿者数
     */
    private Long uniqueVolunteers;
    
    /**
     * 平均完成率（%）
     */
    private BigDecimal avgCompletionRate;
}


