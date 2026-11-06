package com.volunteer.mapper;

import com.volunteer.vo.ActivityStatisticsDTO;
import com.volunteer.vo.CategoryStatisticsDTO;
import com.volunteer.vo.OrganizerStatisticsDTO;
import com.volunteer.vo.VolunteerFieldStatisticsDTO;
import com.volunteer.vo.VolunteerRankingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计数据Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 获取活动统计列表
     */
    List<ActivityStatisticsDTO> selectActivityStatistics(@Param("activityTitle") String activityTitle,
                                                          @Param("year") Integer year);

    /**
     * 获取单个活动统计详情
     */
    ActivityStatisticsDTO selectActivityStatisticsDetail(@Param("activityId") Long activityId);

    /**
     * 获取组织者统计列表
     */
    List<OrganizerStatisticsDTO> selectOrganizerStatistics(@Param("year") Integer year);

    /**
     * 获取活动分类统计
     */
    List<CategoryStatisticsDTO> selectCategoryStatistics(@Param("year") Integer year);

    /**
     * 获取志愿者排名列表
     */
    List<VolunteerRankingDTO> selectVolunteerRanking(@Param("username") String username,
                                                       @Param("realName") String realName,
                                                       @Param("phone") String phone,
                                                       @Param("year") Integer year,
                                                       @Param("limit") Integer limit);

    /**
     * 获取志愿者的分类服务分布
     */
    List<java.util.Map<String, Object>> selectVolunteerCategoryDistribution(@Param("userId") Long userId, 
                                                                              @Param("year") Integer year);

    /**
     * 获取志愿者的月度统计
     */
    List<java.util.Map<String, Object>> selectVolunteerMonthlyStatistics(@Param("userId") Long userId, 
                                                                            @Param("year") Integer year);

    /**
     * 获取志愿者在年度中的排名
     */
    Integer selectVolunteerRankingInYear(@Param("userId") Long userId, 
                                         @Param("year") Integer year);

    /**
     * 获取志愿者的年度服务统计
     */
    java.util.Map<String, Object> selectVolunteerYearlyStatistics(@Param("userId") Long userId, 
                                                                     @Param("year") Integer year);
    
    /**
     * 获取志愿者服务领域统计
     */
    List<VolunteerFieldStatisticsDTO> selectVolunteerFieldStatistics(@Param("username") String username,
                                                                       @Param("realName") String realName,
                                                                       @Param("phone") String phone,
                                                                       @Param("year") Integer year);
}


