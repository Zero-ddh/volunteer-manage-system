package com.volunteer.service;

import com.volunteer.dto.ActivityRecommendationDTO;

import java.util.List;

/**
 * 活动推荐服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-27
 */
public interface ActivityRecommendationService {
    
    /**
     * 为用户推荐匹配的活动
     * 
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param categoryId 分类ID（可选）
     * @param location 用户位置（可选）
     * @return 推荐活动列表
     */
    List<ActivityRecommendationDTO> recommendActivities(Long userId,
                                                        Integer pageNo,
                                                        Integer pageSize,
                                                        Long categoryId,
                                                        String location);
    
    /**
     * 计算综合匹配分数
     * 
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 综合匹配分数 (0-100)
     */
    double calculateComprehensiveMatchScore(Long userId, Long activityId);
    
    /**
     * 计算地理位置匹配分数（基于文本地址）
     * 
     * @param userAddress 用户地址
     * @param activityLocation 活动地点
     * @return 位置匹配分数 (0-100)
     */
    double calculateLocationMatchScore(String userAddress, String activityLocation);
    
    /**
     * 计算分类偏好匹配分数
     * 
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @return 分类匹配分数 (0-100)
     */
    double calculateCategoryPreferenceScore(Long userId, Long categoryId);
    
    /**
     * 计算历史活动相似度分数
     * 
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 历史匹配分数 (0-100)
     */
    double calculateHistorySimilarityScore(Long userId, Long activityId);
    
    /**
     * 获取历史活动类型匹配的关键词
     * 
     * @param userId 用户ID
     * @param categoryId 分类ID
     * @return 匹配的活动分类名称列表
     */
    List<String> getHistoryCategoryMatchKeywords(Long userId, Long categoryId);
}
