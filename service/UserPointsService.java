package com.volunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.UserPoints;

import java.util.List;

/**
 * 用户积分记录Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
public interface UserPointsService extends IService<UserPoints> {
    
    /**
     * 获取用户总积分
     */
    Integer getUserTotalPoints(Long userId);
    
    /**
     * 获取用户积分记录列表
     */
    List<UserPoints> getUserPointsRecords(Long userId);
    
    /**
     * 添加积分记录
     */
    boolean addPointsRecord(Long userId, Integer points, Integer pointsType, String source, String description);
    
    /**
     * 批量添加积分记录
     */
    boolean batchAddPointsRecords(List<UserPoints> pointsRecords);
    
    /**
     * 分页查询积分记录（管理后台）
     */
    IPage<UserPoints> getPointsRecordsPage(Integer current, Integer size, String username, String realName, Integer pointsType, String source);
}
