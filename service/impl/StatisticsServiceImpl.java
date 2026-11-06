package com.volunteer.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.volunteer.mapper.StatisticsMapper;
import com.volunteer.service.StatisticsService;
import com.volunteer.vo.ActivityStatisticsDTO;
import com.volunteer.vo.CategoryStatisticsDTO;
import com.volunteer.vo.OrganizerStatisticsDTO;
import com.volunteer.vo.PageResult;
import com.volunteer.vo.Result;
import com.volunteer.vo.VolunteerFieldStatisticsDTO;
import com.volunteer.vo.VolunteerRankingDTO;
import com.volunteer.vo.VolunteerYearlyReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分析服务实现类
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private StatisticsMapper statisticsMapper;

    @Override
    public List<ActivityStatisticsDTO> getActivityStatistics(String activityTitle, Integer year) {
        return statisticsMapper.selectActivityStatistics(activityTitle, year);
    }

    @Override
    public ActivityStatisticsDTO getActivityStatisticsDetail(Long activityId) {
        return statisticsMapper.selectActivityStatisticsDetail(activityId);
    }

    @Override
    public List<OrganizerStatisticsDTO> getOrganizerStatistics(Integer year) {
        return statisticsMapper.selectOrganizerStatistics(year);
    }

    @Override
    public List<CategoryStatisticsDTO> getCategoryStatistics(Integer year) {
        return statisticsMapper.selectCategoryStatistics(year);
    }

    @Override
    public Result getVolunteerRanking(String username, String realName, String phone, Integer year, int pageNo, int pageSize) {
        // 获取所有志愿者排名数据
        List<VolunteerRankingDTO> allRankings = statisticsMapper.selectVolunteerRanking(username, realName, phone, year, null);
        
        // 添加排名
        for (int i = 0; i < allRankings.size(); i++) {
            allRankings.get(i).setRanking(i + 1);
        }
        
        // 前端分页处理
        int start = (pageNo - 1) * pageSize;
        int end = Math.min(start + pageSize, allRankings.size());
        List<VolunteerRankingDTO> paginatedRankings = allRankings.subList(start, end);
        
        // 构建分页结果
        PageResult<VolunteerRankingDTO> pageResult = new PageResult<>();
        pageResult.setRecords(paginatedRankings);
        pageResult.setTotal((long) allRankings.size());
        pageResult.setCurrent((long) pageNo);
        pageResult.setSize((long) pageSize);
        pageResult.setPages((long) Math.ceil((double) allRankings.size() / pageSize));
        
        return Result.success(pageResult);
    }

    @Override
    public VolunteerYearlyReportDTO getVolunteerYearlyReport(Long userId, Integer year) {
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }

        // 获取基础统计
        Map<String, Object> statistics = statisticsMapper.selectVolunteerYearlyStatistics(userId, year);
        
        if (statistics == null || statistics.isEmpty()) {
            return null;
        }

        VolunteerYearlyReportDTO report = new VolunteerYearlyReportDTO();
        report.setUserId((Long) statistics.get("userId"));
        report.setRealName((String) statistics.get("realName"));
        report.setYear(year);
        report.setTotalServiceHours((java.math.BigDecimal) statistics.get("totalServiceHours"));
        report.setActivityCount(((Number) statistics.get("activityCount")).intValue());
        report.setTotalPoints(((Number) statistics.get("totalPoints")).longValue());
        report.setServiceDays(((Number) statistics.get("serviceDays")).intValue());
        report.setStarLevel(((Number) statistics.get("starLevel")).intValue());

        // 获取分类分布
        List<Map<String, Object>> categoryDistribution = statisticsMapper.selectVolunteerCategoryDistribution(userId, year);
        report.setCategoryDistribution(categoryDistribution);

        // 获取月度统计
        List<Map<String, Object>> monthlyStatistics = statisticsMapper.selectVolunteerMonthlyStatistics(userId, year);
        report.setMonthlyStatistics(monthlyStatistics);

        // 获取排名
        Integer ranking = statisticsMapper.selectVolunteerRankingInYear(userId, year);
        report.setRanking(ranking);

        // 获取服务地点统计（简化版）
        List<Map<String, Object>> locationStatistics = new ArrayList<>();
        report.setLocationStatistics(locationStatistics);

        return report;
    }

    @Override
    public byte[] exportActivityStatistics(String activityTitle, Integer year) {
        ByteArrayOutputStream outputStream = null;
        ExcelWriter excelWriter = null;
        
        try {
            // 查询数据
            List<ActivityStatisticsDTO> data = statisticsMapper.selectActivityStatistics(activityTitle, year);
            
            // 创建Excel
            outputStream = new ByteArrayOutputStream();
            excelWriter = EasyExcel.write(outputStream, ActivityStatisticsDTO.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("活动统计").build();
            
            // 写入数据
            excelWriter.write(data, writeSheet);
            excelWriter.finish();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出活动统计数据失败：{}", e.getMessage(), e);
            throw new RuntimeException("导出失败：" + e.getMessage(), e);
        } finally {
            if (excelWriter != null) {
                try {
                    excelWriter.finish();
                } catch (Exception e) {
                    log.error("关闭ExcelWriter失败：{}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public byte[] exportVolunteerRanking(String username, String realName, String phone, Integer year) {
        ByteArrayOutputStream outputStream = null;
        ExcelWriter excelWriter = null;
        
        try {
            // 查询数据（不分页，获取全部）
            List<VolunteerRankingDTO> data = statisticsMapper.selectVolunteerRanking(username, realName, phone, year, null);
            
            // 添加排名
            for (int i = 0; i < data.size(); i++) {
                data.get(i).setRanking(i + 1);
            }
            
            // 创建Excel
            outputStream = new ByteArrayOutputStream();
            excelWriter = EasyExcel.write(outputStream, VolunteerRankingDTO.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("志愿者排名").build();
            
            // 写入数据
            excelWriter.write(data, writeSheet);
            excelWriter.finish();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出志愿者排名数据失败：{}", e.getMessage(), e);
            throw new RuntimeException("导出失败：" + e.getMessage(), e);
        } finally {
            if (excelWriter != null) {
                try {
                    excelWriter.finish();
                } catch (Exception e) {
                    log.error("关闭ExcelWriter失败：{}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public byte[] exportOrganizerStatistics(Integer year) {
        // TODO: 实现Excel导出功能
        return new byte[0];
    }

    @Override
    public byte[] exportCategoryStatistics(Integer year) {
        // TODO: 实现Excel导出功能
        return new byte[0];
    }

    @Override
    public List<VolunteerFieldStatisticsDTO> getVolunteerFieldStatistics(String username, String realName, String phone, Integer year) {
        return statisticsMapper.selectVolunteerFieldStatistics(username, realName, phone, year);
    }

    @Override
    public byte[] exportVolunteerFieldStatistics(String username, String realName, String phone, Integer year) {
        ByteArrayOutputStream outputStream = null;
        ExcelWriter excelWriter = null;
        
        try {
            // 查询数据
            List<VolunteerFieldStatisticsDTO> data = statisticsMapper.selectVolunteerFieldStatistics(username, realName, phone, year);
            
            // 使用ByteArrayOutputStream在内存中创建Excel
            outputStream = new ByteArrayOutputStream();
            
            // 创建ExcelWriter
            excelWriter = EasyExcel.write(outputStream, VolunteerFieldStatisticsDTO.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("志愿者服务领域统计").build();
            
            // 写入数据
            excelWriter.write(data, writeSheet);
            
            // 关闭流，释放资源
            excelWriter.finish();
            
            // 返回byte数组
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出志愿者服务领域统计失败：{}", e.getMessage(), e);
            throw new RuntimeException("导出失败：" + e.getMessage(), e);
        } finally {
            // 确保资源被释放
            if (excelWriter != null) {
                try {
                    excelWriter.finish();
                } catch (Exception e) {
                    log.error("关闭ExcelWriter失败：{}", e.getMessage(), e);
                }
            }
        }
    }
}


