package com.volunteer.controller;

import com.volunteer.service.StatisticsService;
import com.volunteer.vo.Result;
import com.volunteer.vo.VolunteerYearlyReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 志愿者统计控制器（面向志愿者用户）
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Slf4j
@RestController
@RequestMapping("/v1/statistics")
public class VolunteerStatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 获取志愿者年度报告
     */
    @GetMapping("/volunteers/{userId}/yearly-report")
    public Result<VolunteerYearlyReportDTO> getVolunteerYearlyReport(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year) {
        try {
            if (year == null) {
                year = java.time.LocalDate.now().getYear();
            }
            VolunteerYearlyReportDTO report = statisticsService.getVolunteerYearlyReport(userId, year);
            if (report == null) {
                return Result.error("暂无该年度的服务记录");
            }
            return Result.success(report);
        } catch (Exception e) {
            log.error("获取志愿者年度报告失败：{}", e.getMessage(), e);
            return Result.error("获取志愿者年度报告失败：" + e.getMessage());
        }
    }
}


