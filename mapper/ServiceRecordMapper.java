package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ServiceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务记录Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Mapper
public interface ServiceRecordMapper extends BaseMapper<ServiceRecord> {

    /**
     * 分页查询用户服务记录（带活动信息）
     */
    Page<ServiceRecord> selectUserServiceRecordsWithActivity(Page<ServiceRecord> page, @Param("userId") Long userId);

    /**
     * 获取用户服务足迹数据
     */
    List<ServiceRecord> selectUserServiceFootprints(@Param("userId") Long userId);

    /**
     * 统计用户服务时长
     */
    Double selectTotalServiceHours(@Param("userId") Long userId);

    /**
     * 统计用户参与的不同活动数量
     */
    Integer selectDistinctActivityCount(@Param("userId") Long userId);

    /**
     * 获取用户服务地点统计
     */
    List<ServiceRecord> selectServiceLocationStats(@Param("userId") Long userId);
    
    /**
     * 统计用户服务次数
     */
    Integer selectServiceCount(@Param("userId") Long userId);
}
