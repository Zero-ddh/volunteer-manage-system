package com.volunteer.controller;

import com.volunteer.service.StatisticsService;
import com.volunteer.vo.ActivityStatisticsDTO;
import com.volunteer.vo.CategoryStatisticsDTO;
import com.volunteer.vo.OrganizerStatisticsDTO;
import com.volunteer.vo.VolunteerFieldStatisticsDTO;
import com.volunteer.vo.VolunteerRankingDTO;
import com.volunteer.vo.VolunteerYearlyReportDTO;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 统计分析控制器
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 获取活动统计列表
     */
    @GetMapping("/activities")
    public Result<List<ActivityStatisticsDTO>> getActivityStatistics(
            @RequestParam(required = false) String activityTitle,
            @RequestParam(required = false, defaultValue = "2025") Integer year) {
        try {
            List<ActivityStatisticsDTO> statistics = statisticsService.getActivityStatistics(activityTitle, year);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取活动统计失败：{}", e.getMessage(), e);
            return Result.error("获取活动统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取单个活动统计详情
     */
    @GetMapping("/activities/{activityId}")
    public Result<ActivityStatisticsDTO> getActivityStatisticsDetail(@PathVariable Long activityId) {
        try {
            ActivityStatisticsDTO statistics = statisticsService.getActivityStatisticsDetail(activityId);
            if (statistics == null) {
                return Result.error("活动统计不存在");
            }
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取活动统计详情失败：{}", e.getMessage(), e);
            return Result.error("获取活动统计详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取组织者统计列表
     */
    @GetMapping("/organizers")
    public Result<List<OrganizerStatisticsDTO>> getOrganizerStatistics(
            @RequestParam(required = false, defaultValue = "2025") Integer year) {
        try {
            List<OrganizerStatisticsDTO> statistics = statisticsService.getOrganizerStatistics(year);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取组织者统计失败：{}", e.getMessage(), e);
            return Result.error("获取组织者统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取活动分类统计
     */
    @GetMapping("/categories")
    public Result<List<CategoryStatisticsDTO>> getCategoryStatistics(
            @RequestParam(required = false, defaultValue = "2025") Integer year) {
        try {
            List<CategoryStatisticsDTO> statistics = statisticsService.getCategoryStatistics(year);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取分类统计失败：{}", e.getMessage(), e);
            return Result.error("获取分类统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取志愿者排名列表
     */
    @GetMapping("/volunteers/ranking")
    public Result getVolunteerRanking(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            return statisticsService.getVolunteerRanking(username, realName, phone, year, pageNo, pageSize);
        } catch (Exception e) {
            log.error("获取志愿者排名失败：{}", e.getMessage(), e);
            return Result.error("获取志愿者排名失败：" + e.getMessage());
        }
    }

    /**
     * 导出活动统计数据（Excel）
     */
    @GetMapping("/activities/export")
    public void exportActivityStatistics(
            @RequestParam(required = false) String activityTitle,
            @RequestParam(required = false, defaultValue = "2025") Integer year,
            HttpServletResponse response) {
        try {
            byte[] data = statisticsService.exportActivityStatistics(activityTitle, year);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", String.format("attachment; filename=活动统计_%s.xlsx", year));
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出活动统计数据失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 导出志愿者排名数据（Excel）
     */
    @GetMapping("/volunteers/ranking/export")
    public void exportVolunteerRanking(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, defaultValue = "2025") Integer year,
            HttpServletResponse response) {
        try {
            byte[] data = statisticsService.exportVolunteerRanking(username, realName, phone, year);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", String.format("attachment; filename=志愿者排名_%s.xlsx", year));
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出志愿者排名数据失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 导出组织者统计数据（Excel）
     */
    @GetMapping("/organizers/export")
    public void exportOrganizerStatistics(
            @RequestParam(required = false, defaultValue = "2025") Integer year,
            HttpServletResponse response) {
        try {
            byte[] data = statisticsService.exportOrganizerStatistics(year);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=organizer_statistics.xlsx");
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出组织者统计数据失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 导出分类统计数据（Excel）
     */
    @GetMapping("/categories/export")
    public void exportCategoryStatistics(
            @RequestParam(required = false, defaultValue = "2025") Integer year,
            HttpServletResponse response) {
        try {
            byte[] data = statisticsService.exportCategoryStatistics(year);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=category_statistics.xlsx");
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出分类统计数据失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 获取志愿者服务领域统计
     */
    @GetMapping("/volunteers/field-statistics")
    public Result<List<VolunteerFieldStatisticsDTO>> getVolunteerFieldStatistics(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, defaultValue = "2025") Integer year) {
        try {
            List<VolunteerFieldStatisticsDTO> statistics = statisticsService.getVolunteerFieldStatistics(username, realName, phone, year);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取志愿者服务领域统计失败：{}", e.getMessage(), e);
            return Result.error("获取志愿者服务领域统计失败：" + e.getMessage());
        }
    }

    /**
     * 导出志愿者服务领域统计数据（Excel）
     */
    @GetMapping("/volunteers/field-statistics/export")
    public void exportVolunteerFieldStatistics(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, defaultValue = "2025") Integer year,
            HttpServletResponse response) {
        try {
            byte[] data = statisticsService.exportVolunteerFieldStatistics(username, realName, phone, year);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", String.format("attachment; filename=志愿者服务领域统计_%s.xlsx", year));
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出志愿者服务领域统计数据失败：{}", e.getMessage(), e);
        }
    }
}


