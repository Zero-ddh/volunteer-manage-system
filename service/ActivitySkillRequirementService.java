package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.dto.ActivitySkillRequirementDTO;
import com.volunteer.dto.VolunteerMatchDTO;
import com.volunteer.entity.ActivitySkillRequirement;

import java.util.List;

/**
 * 活动技能要求Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
public interface ActivitySkillRequirementService extends IService<ActivitySkillRequirement> {
    
    /**
     * 获取活动技能要求
     */
    List<ActivitySkillRequirementDTO> getActivitySkillRequirements(Long activityId);
    
    /**
     * 设置活动技能要求
     */
    boolean setActivitySkillRequirements(Long activityId, List<ActivitySkillRequirementDTO> requirements);
    
    /**
     * 按技能筛选志愿者
     */
    List<VolunteerMatchDTO> getVolunteersBySkill(Long activityId, Long skillCategoryId, String skillName);
    
    /**
     * 计算志愿者与活动的技能匹配度
     */
    double calculateSkillMatchScore(Long userId, Long activityId);
    
    /**
     * 获取技能匹配的关键词列表
     */
    List<String> getSkillMatchKeywords(Long userId, Long activityId);
    
    /**
     * 获取志愿者技能匹配详情
     */
    List<VolunteerMatchDTO> getVolunteerSkillMatchDetails(Long userId, Long activityId);
}
