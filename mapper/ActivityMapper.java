package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 根据ID获取活动详情（包含分类名称和组织者名称）
     */
    @Select("SELECT a.*, " +
            "COALESCE(ac.category_name, '未分类') as category_name, " +
            "COALESCE(u.username, '未知') as organizer_name " +
            "FROM activity a " +
            "LEFT JOIN activity_category ac ON a.category_id = ac.id " +
            "LEFT JOIN sys_user u ON a.organizer_id = u.id " +
            "WHERE a.id = #{id} AND a.deleted = 0")
    Activity selectActivityDetailById(Long id);

    /**
     * 根据ID获取活动详情（包含分类名称、组织者名称和实际报名人数）
     */
    Activity selectActivityDetailWithParticipantsById(Long id);

    /**
     * 分页查询活动列表（包含实际报名人数）
     */
    Page<Activity> selectActivityPageWithParticipants(Page<Activity> page, 
                                                     @Param("status") Integer status,
                                                     @Param("categoryId") Long categoryId,
                                                     @Param("keyword") String keyword,
                                                     @Param("location") String location,
                                                     @Param("startTimeBegin") String startTimeBegin,
                                                     @Param("startTimeEnd") String startTimeEnd);

    /**
     * 管理后台分页查询活动列表（包含实际报名人数）
     */
    Page<Activity> selectAdminActivityPageWithParticipants(Page<Activity> page,
                                                           @Param("status") Integer status,
                                                           @Param("categoryId") Long categoryId,
                                                           @Param("organizerId") Long organizerId,
                                                           @Param("keyword") String keyword);

    /**
     * 获取用户参与的活动列表（通过报名记录查询）
     */
    @Select("SELECT DISTINCT a.* " +
            "FROM activity a " +
            "INNER JOIN activity_registration r ON a.id = r.activity_id " +
            "WHERE r.user_id = #{userId} " +
            "AND r.status IN (1, 2) " +  // 1: 已报名, 2: 已审核通过
            "AND a.deleted = 0 " +
            "ORDER BY a.created_time DESC")
    List<Activity> getUserParticipatedActivities(@Param("userId") Long userId);
}
