package com.volunteer.vo;

import lombok.Data;

/**
 * 志愿者统计信息VO
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Data
public class VolunteerStatisticsVO {
    
    /**
     * 总志愿者数
     */
    private Long totalVolunteers;
    
    /**
     * 正常状态志愿者数
     */
    private Long activeVolunteers;
    
    /**
     * 禁用状态志愿者数
     */
    private Long inactiveVolunteers;
    
    /**
     * 待审核志愿者数
     */
    private Long pendingVolunteers;
    
    /**
     * 总服务时长（小时）
     */
    private Long totalServiceHours;
    
    /**
     * 平均服务时长（小时）
     */
    private Double avgServiceHours;
    
    /**
     * 本月新增志愿者数
     */
    private Long monthlyNewVolunteers;
    
    /**
     * 活跃志愿者数（近30天有活动）
     */
    private Long activeVolunteers30Days;
}
