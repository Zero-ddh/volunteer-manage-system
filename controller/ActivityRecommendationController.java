package com.volunteer.controller;

import com.volunteer.dto.ActivityRecommendationDTO;
import com.volunteer.entity.Activity;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.service.ActivityRecommendationService;
import com.volunteer.service.ActivitySkillRequirementService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 活动推荐Controller
 * 
 * @author volunteer-team
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/v1/activity-recommendation")
public class ActivityRecommendationController {
    
    @Resource
    private ActivityRecommendationService activityRecommendationService;
    
    @Resource
    private ActivitySkillRequirementService activitySkillRequirementService;
    
    @Resource
    private ActivityMapper activityMapper;
    
    /**
     * 获取推荐活动列表
     */
    @GetMapping("/list")
    public Result<List<ActivityRecommendationDTO>> recommendActivities(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<ActivityRecommendationDTO> recommendations = 
                activityRecommendationService.recommendActivities(
                    userId, pageNo, pageSize, categoryId, location
                );
            
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取推荐活动失败", e);
            return Result.error("获取推荐活动失败：" + e.getMessage());
        }
    }
    
    /**
     * 计算活动匹配分数
     */
    @GetMapping("/match-score/{activityId}")
    public Result<Double> calculateMatchScore(
            @PathVariable Long activityId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            double score = activityRecommendationService.calculateComprehensiveMatchScore(userId, activityId);
            return Result.success(score);
        } catch (Exception e) {
            log.error("计算匹配分数失败", e);
            return Result.error("计算匹配分数失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取活动详细匹配信息
     */
    @GetMapping("/match-details/{activityId}")
    public Result<ActivityRecommendationDTO> getMatchDetails(
            @PathVariable Long activityId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取活动信息
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            
            // 计算各项匹配度
            double skillScore = activitySkillRequirementService.calculateSkillMatchScore(userId, activityId);
            double categoryScore = activityRecommendationService.calculateCategoryPreferenceScore(userId, activity.getCategoryId());
            
            // 获取匹配关键词
            List<String> skillKeywords = activitySkillRequirementService.getSkillMatchKeywords(userId, activityId);
            List<String> categoryKeywords = activityRecommendationService.getHistoryCategoryMatchKeywords(userId, activity.getCategoryId());
            
            // 计算综合匹配度（权重：技能70% + 历史活动类型30%）
            double matchScore = skillScore * 0.7 + categoryScore * 0.3;
            
            // 构建返回对象
            ActivityRecommendationDTO dto = new ActivityRecommendationDTO();
            BeanUtils.copyProperties(activity, dto);
            dto.setSkillMatchScore(skillScore);
            dto.setCategoryMatchScore(categoryScore);
            dto.setHistoryMatchScore(0.0); // 历史匹配已被服务类型匹配替代
            dto.setMatchScore(matchScore);
            dto.setSkillMatchKeywords(skillKeywords);
            dto.setCategoryMatchKeywords(categoryKeywords);
            
            return Result.success(dto);
        } catch (Exception e) {
            log.error("获取匹配详情失败", e);
            return Result.error("获取匹配详情失败：" + e.getMessage());
        }
    }
}
