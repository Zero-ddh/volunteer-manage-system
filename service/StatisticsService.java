package com.volunteer.service;

import com.volunteer.vo.ActivityStatisticsDTO;
import com.volunteer.vo.CategoryStatisticsDTO;
import com.volunteer.vo.OrganizerStatisticsDTO;
import com.volunteer.vo.Result;
import com.volunteer.vo.VolunteerFieldStatisticsDTO;
import com.volunteer.vo.VolunteerRankingDTO;
import com.volunteer.vo.VolunteerYearlyReportDTO;

import java.util.List;
import java.util.Map;

/**
 * 统计分析服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
public interface StatisticsService {

    /**
     * 获取活动统计列表
     */
    List<ActivityStatisticsDTO> getActivityStatistics(String activityTitle, Integer year);

    /**
     * 获取单个活动统计详情
     */
    ActivityStatisticsDTO getActivityStatisticsDetail(Long activityId);

    /**
     * 获取组织者统计列表
     */
    List<OrganizerStatisticsDTO> getOrganizerStatistics(Integer year);

    /**
     * 获取活动分类统计
     */
    List<CategoryStatisticsDTO> getCategoryStatistics(Integer year);

    /**
     * 获取志愿者排名列表
     */
    Result getVolunteerRanking(String username, String realName, String phone, Integer year, int pageNo, int pageSize);

    /**
     * 获取志愿者年度报告
     */
    VolunteerYearlyReportDTO getVolunteerYearlyReport(Long userId, Integer year);

    /**
     * 导出活动统计数据（Excel）
     */
    byte[] exportActivityStatistics(String activityTitle, Integer year);
    
    /**
     * 导出志愿者排名数据（Excel）
     */
    byte[] exportVolunteerRanking(String username, String realName, String phone, Integer year);

    /**
     * 导出组织者统计（Excel）
     */
    byte[] exportOrganizerStatistics(Integer year);

    /**
     * 导出分类统计（Excel）
     */
    byte[] exportCategoryStatistics(Integer year);
    
    /**
     * 获取志愿者服务领域统计
     */
    List<VolunteerFieldStatisticsDTO> getVolunteerFieldStatistics(String username, String realName, String phone, Integer year);
    
    /**
     * 导出志愿者服务领域统计（Excel）
     */
    byte[] exportVolunteerFieldStatistics(String username, String realName, String phone, Integer year);
}


