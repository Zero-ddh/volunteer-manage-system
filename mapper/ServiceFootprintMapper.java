package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.ServiceFootprint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务足迹Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Mapper
public interface ServiceFootprintMapper extends BaseMapper<ServiceFootprint> {

    /**
     * 获取用户服务足迹数据
     */
    List<ServiceFootprint> selectUserFootprints(@Param("userId") Long userId);

    /**
     * 根据服务记录ID获取足迹
     */
    ServiceFootprint selectByServiceRecordId(@Param("serviceRecordId") Long serviceRecordId);

    /**
     * 获取用户服务地点统计
     */
    List<ServiceFootprint> selectLocationStats(@Param("userId") Long userId);
}
