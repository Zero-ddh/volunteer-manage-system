package com.volunteer.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 志愿者排名DTO
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Data
public class VolunteerRankingDTO {
    
    @ExcelProperty(value = "排名", index = 0)
    private Integer ranking;
    
    @ExcelProperty(value = "用户名", index = 1)
    private String username;
    
    @ExcelProperty(value = "姓名", index = 2)
    private String realName;
    
    @ExcelProperty(value = "手机号", index = 3)
    private String phone;
    
    @ExcelProperty(value = "总服务时长(小时)", index = 4)
    private BigDecimal totalServiceHours;
    
    @ExcelIgnore
    private Long userId;
    
    @ExcelIgnore
    private Integer activityCount;
    
    @ExcelIgnore
    private Long totalPoints;
    
    @ExcelIgnore
    private Integer starLevel;
    
    @ExcelIgnore
    private Integer serviceDays;
}


