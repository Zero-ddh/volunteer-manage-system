package com.volunteer.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 活动统计DTO
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Data
public class ActivityStatisticsDTO {
    
    @ExcelProperty(value = "活动ID", index = 0)
    private Long activityId;
    
    @ExcelProperty(value = "活动标题", index = 1)
    private String activityTitle;
    
    @ExcelProperty(value = "分类", index = 2)
    private String categoryName;
    
    @ExcelProperty(value = "总报名", index = 3)
    private Long totalRegistrations;
    
    @ExcelProperty(value = "已通过", index = 4)
    private Long approvedRegistrations;
    
    @ExcelProperty(value = "已签到", index = 5)
    private Long checkinCount;
    
    @ExcelProperty(value = "完成率(%)", index = 6)
    private BigDecimal completionRate;
    
    @ExcelIgnore
    private String organizerName;
    
    @ExcelIgnore
    private String startTime;
    
    @ExcelIgnore
    private String endTime;
    
    @ExcelIgnore
    private BigDecimal totalServiceHours;
    
    @ExcelIgnore
    private BigDecimal avgServiceHours;
    
    @ExcelIgnore
    private Integer maxParticipants;
    
    @ExcelIgnore
    private Integer status;
    
    @ExcelIgnore
    private BigDecimal registrationRate;
}


