package com.volunteer.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 志愿者服务领域统计DTO
 * 
 * @author volunteer-team
 * @since 2025-01-27
 */
@Data
public class VolunteerFieldStatisticsDTO {
    
    @ExcelProperty(value = "用户名", index = 0)
    private String username;
    
    @ExcelProperty(value = "姓名", index = 1)
    private String realName;
    
    @ExcelProperty(value = "手机号", index = 2)
    private String phone;
    
    @ExcelProperty(value = "服务领域分类", index = 3)
    private String categoryName;
    
    @ExcelProperty(value = "活动数", index = 4)
    private Long activityCount;
    
    /**
     * 志愿者ID（不导出到Excel）
     */
    private Long userId;
}

