package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户积分记录Mapper接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {
    
    /**
     * 获取用户总积分
     */
    Integer getUserTotalPoints(@Param("userId") Long userId);
    
    /**
     * 获取用户积分记录列表
     */
    List<UserPoints> getUserPointsRecords(@Param("userId") Long userId);
    
    /**
     * 分页查询积分记录（管理后台）
     */
    IPage<UserPoints> getPointsRecordsPage(Page<UserPoints> page, @Param("username") String username, @Param("realName") String realName, @Param("pointsType") Integer pointsType, @Param("source") String source);
}
