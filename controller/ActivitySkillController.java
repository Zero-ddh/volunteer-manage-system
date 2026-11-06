package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.ActivitySkillRequirementDTO;
import com.volunteer.dto.VolunteerMatchDTO;
import com.volunteer.service.ActivitySkillRequirementService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 活动技能匹配控制器
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Slf4j
@RestController
@RequestMapping("/v1/activities")
public class ActivitySkillController {

    @Resource
    private ActivitySkillRequirementService activitySkillRequirementService;

    /**
     * 获取活动技能要求
     */
    @GetMapping("/{activityId}/skill-requirements")
    public Result<List<ActivitySkillRequirementDTO>> getSkillRequirements(@PathVariable Long activityId) {
        try {
            List<ActivitySkillRequirementDTO> requirements = activitySkillRequirementService.getActivitySkillRequirements(activityId);
            return Result.success(requirements);
        } catch (Exception e) {
            log.error("获取活动技能要求失败", e);
            return Result.error("获取活动技能要求失败");
        }
    }

    /**
     * 设置活动技能要求
     */
    @PostMapping("/{activityId}/skill-requirements")
    public Result<Boolean> setSkillRequirements(@PathVariable Long activityId, 
                                               @RequestBody List<ActivitySkillRequirementDTO> requirements) {
        try {
            log.info("设置活动技能要求 - activityId: {}, requirements: {}", activityId, requirements);
            boolean result = activitySkillRequirementService.setActivitySkillRequirements(activityId, requirements);
            log.info("设置活动技能要求成功 - activityId: {}, result: {}", activityId, result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("设置活动技能要求失败 - activityId: {}, requirements: {}", activityId, requirements, e);
            return Result.error("设置活动技能要求失败: " + e.getMessage());
        }
    }

    /**
     * 按技能筛选志愿者
     */
    @GetMapping("/{activityId}/volunteers/by-skill")
    public Result<List<VolunteerMatchDTO>> getVolunteersBySkill(@PathVariable Long activityId,
                                                               @RequestParam(required = false) Long skillCategoryId,
                                                               @RequestParam(required = false) String skillName) {
        try {
            List<VolunteerMatchDTO> volunteers = activitySkillRequirementService.getVolunteersBySkill(activityId, skillCategoryId, skillName);
            return Result.success(volunteers);
        } catch (Exception e) {
            log.error("按技能筛选志愿者失败", e);
            return Result.error("按技能筛选志愿者失败");
        }
    }

    /**
     * 计算志愿者与活动的技能匹配度
     */
    @GetMapping("/{activityId}/volunteers/{userId}/match-score")
    public Result<Double> getVolunteerMatchScore(@PathVariable Long activityId, @PathVariable Long userId) {
        try {
            double matchScore = activitySkillRequirementService.calculateSkillMatchScore(userId, activityId);
            return Result.success(matchScore);
        } catch (Exception e) {
            log.error("计算志愿者匹配度失败", e);
            return Result.error("计算志愿者匹配度失败");
        }
    }

    /**
     * 获取志愿者技能匹配详情
     */
    @GetMapping("/{activityId}/volunteers/{userId}/match-details")
    public Result<List<VolunteerMatchDTO>> getVolunteerMatchDetails(@PathVariable Long activityId, @PathVariable Long userId) {
        try {
            List<VolunteerMatchDTO> matchDetails = activitySkillRequirementService.getVolunteerSkillMatchDetails(userId, activityId);
            return Result.success(matchDetails);
        } catch (Exception e) {
            log.error("获取志愿者匹配详情失败", e);
            return Result.error("获取志愿者匹配详情失败");
        }
    }

    /**
     * 获取活动推荐志愿者（按匹配度排序）
     */
    @GetMapping("/{activityId}/recommended-volunteers")
    public Result<List<VolunteerMatchDTO>> getRecommendedVolunteers(@PathVariable Long activityId,
                                                                   @RequestParam(defaultValue = "10") int limit) {
        try {
            // 获取所有匹配的志愿者
            List<VolunteerMatchDTO> volunteers = activitySkillRequirementService.getVolunteersBySkill(activityId, null, null);
            
            // 计算每个志愿者的总体匹配度
            for (VolunteerMatchDTO volunteer : volunteers) {
                double overallScore = activitySkillRequirementService.calculateSkillMatchScore(volunteer.getUserId(), activityId);
                volunteer.setMatchScore(overallScore);
            }
            
            // 按匹配度排序并限制数量
            volunteers.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
            
            if (volunteers.size() > limit) {
                volunteers = volunteers.subList(0, limit);
            }
            
            return Result.success(volunteers);
        } catch (Exception e) {
            log.error("获取推荐志愿者失败", e);
            return Result.error("获取推荐志愿者失败");
        }
    }
}
