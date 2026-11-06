package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.ServiceRecord;

import java.util.List;
import java.util.Map;

/**
 * 服务记录Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
public interface ServiceRecordService extends IService<ServiceRecord> {

    /**
     * 分页查询用户服务记录
     */
    Page<ServiceRecord> getUserServiceRecords(Long userId, int pageNo, int pageSize);

    /**
     * 获取服务记录详情
     */
    ServiceRecord getServiceRecordDetail(Long id);

    /**
     * 创建服务记录
     */
    boolean createServiceRecord(ServiceRecord serviceRecord);

    /**
     * 更新服务记录
     */
    boolean updateServiceRecord(ServiceRecord serviceRecord);

    /**
     * 更新服务心得
     */
    boolean updateServiceExperience(Long id, String experience);

    /**
     * 上传服务照片
     */
    boolean uploadServicePhotos(Long id, List<String> photoUrls);

    /**
     * 获取用户服务足迹数据
     */
    List<ServiceRecord> getUserServiceFootprints(Long userId);

    /**
     * 获取用户服务统计
     */
    Map<String, Object> getUserServiceStats(Long userId);

    /**
     * 获取服务地点统计
     */
    List<Map<String, Object>> getServiceLocationStats(Long userId);

    /**
     * 获取用户积分记录
     */
    /**
     * 获取用户积分记录（支持分页和搜索）
     */
    Page<Map<String, Object>> getUserPointsRecords(Long userId, int pageNo, int pageSize, Integer pointsType);
    
    /**
     * 获取用户积分记录（兼容旧版本）
     */
    List<Map<String, Object>> getUserPointsRecords(Long userId);

    /**
     * 管理后台分页查询服务记录
     */
    Page<ServiceRecord> adminPageList(int pageNo, int pageSize, String keyword, Long userId, Long activityId, Integer status);

    // ========== 典型案例相关方法 ==========

    /**
     * 分页查询用户典型案例
     */
    Page<ServiceRecord> getUserTypicalCases(Long userId, int pageNo, int pageSize, Integer caseType);

    /**
     * 获取典型案例详情
     */
    ServiceRecord getTypicalCaseDetail(Long id);

    /**
     * 创建典型案例
     */
    boolean createTypicalCase(ServiceRecord serviceRecord);

    /**
     * 更新典型案例
     */
    boolean updateTypicalCase(ServiceRecord serviceRecord);

    /**
     * 设置典型案例精选状态
     */
    boolean setFeaturedCase(Long id, boolean isFeatured);

    /**
     * 增加案例浏览次数
     */
    boolean incrementCaseViewCount(Long id);

    /**
     * 增加案例点赞次数
     */
    boolean incrementCaseLikeCount(Long id);

    /**
     * 获取典型案例统计
     */
    Map<String, Object> getTypicalCaseStats(Long userId);

    /**
     * 管理后台分页查询典型案例
     */
    Page<ServiceRecord> adminPageTypicalCases(int pageNo, int pageSize, String keyword, Long userId, Integer caseType, Integer isFeatured);

    // ========== 自动时长计算相关方法 ==========

    /**
     * 自动计算服务时长
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 是否计算成功
     */
    boolean autoCalculateServiceHours(Long userId, Long activityId);

    /**
     * 批量自动计算用户服务时长
     * @param userId 用户ID
     * @return 计算成功的记录数
     */
    int batchAutoCalculateServiceHours(Long userId);

    /**
     * 获取用户最近的服务记录（用于自动计算）
     * @param userId 用户ID
     * @return 最近的服务记录列表
     */
    List<ServiceRecord> getRecentRecords(Long userId);
    
    /**
     * 获取用户所有服务记录（用于统计总时长）
     * @param userId 用户ID
     * @return 所有服务记录列表
     */
    List<ServiceRecord> getAllRecordsByUserId(Long userId);
}
