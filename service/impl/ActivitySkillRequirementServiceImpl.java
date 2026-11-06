package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.dto.ActivitySkillRequirementDTO;
import com.volunteer.dto.UserSkillDTO;
import com.volunteer.dto.VolunteerMatchDTO;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivitySkillRequirement;
import com.volunteer.entity.SkillCategory;
import com.volunteer.entity.UserSkill;
import com.volunteer.mapper.ActivitySkillRequirementMapper;
import com.volunteer.service.ActivityService;
import com.volunteer.service.ActivitySkillRequirementService;
import com.volunteer.service.SkillCategoryService;
import com.volunteer.service.UserSkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动技能要求Service实现类
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Slf4j
@Service
public class ActivitySkillRequirementServiceImpl extends ServiceImpl<ActivitySkillRequirementMapper, ActivitySkillRequirement> implements ActivitySkillRequirementService {
    
    @Resource
    private SkillCategoryService skillCategoryService;
    
    @Resource
    private UserSkillService userSkillService;
    
    @Resource
    private ActivityService activityService;
    
    @Override
    public List<ActivitySkillRequirementDTO> getActivitySkillRequirements(Long activityId) {
        LambdaQueryWrapper<ActivitySkillRequirement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivitySkillRequirement::getActivityId, activityId)
               .orderByAsc(ActivitySkillRequirement::getId);
        
        List<ActivitySkillRequirement> requirements = list(wrapper);
        
        return requirements.stream().map(requirement -> {
            ActivitySkillRequirementDTO dto = new ActivitySkillRequirementDTO();
            BeanUtils.copyProperties(requirement, dto);
            
            // 设置技能分类名称
            if (requirement.getSkillCategoryId() != null) {
                SkillCategory category = skillCategoryService.getById(requirement.getSkillCategoryId());
                if (category != null) {
                    dto.setSkillCategoryName(category.getCategoryName());
                }
            }
            
            // 设置等级文本
            dto.setRequiredLevelText(getLevelText(requirement.getRequiredLevel()));
            
            // 设置是否必需文本
            dto.setIsRequiredText(requirement.getIsRequired() == 1 ? "必需" : "可选");
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean setActivitySkillRequirements(Long activityId, List<ActivitySkillRequirementDTO> requirements) {
        try {
            log.info("开始设置活动技能要求 - activityId: {}, requirements count: {}", activityId, 
                requirements != null ? requirements.size() : 0);
            
            // 删除现有要求
            LambdaQueryWrapper<ActivitySkillRequirement> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ActivitySkillRequirement::getActivityId, activityId);
            remove(deleteWrapper);
            log.info("已删除活动 {} 的现有技能要求", activityId);
            
            // 添加新要求
            if (requirements != null && !requirements.isEmpty()) {
                List<ActivitySkillRequirement> newRequirements = requirements.stream().map(dto -> {
                    ActivitySkillRequirement requirement = new ActivitySkillRequirement();
                    
                    // 只复制必需的字段
                    requirement.setActivityId(activityId);
                    requirement.setSkillName(dto.getSkillName());
                    requirement.setRequiredLevel(dto.getRequiredLevel());
                    requirement.setIsRequired(dto.getIsRequired());
                    
                    // skill_category_id 字段可能为null，设置默认值为0
                    requirement.setSkillCategoryId(dto.getSkillCategoryId() != null ? dto.getSkillCategoryId() : 0L);
                    
                    // 描述字段可选
                    if (dto.getDescription() != null) {
                        requirement.setDescription(dto.getDescription());
                    }
                    
                    log.debug("创建技能要求 - skillName: {}, requiredLevel: {}, isRequired: {}", 
                        requirement.getSkillName(), requirement.getRequiredLevel(), requirement.getIsRequired());
                    
                    return requirement;
                }).collect(Collectors.toList());
                
                boolean result = saveBatch(newRequirements);
                log.info("活动技能要求保存结果 - activityId: {}, result: {}", activityId, result);
                return result;
            }
            
            log.info("活动 {} 没有技能要求，已清空", activityId);
            return true;
        } catch (Exception e) {
            log.error("设置活动技能要求失败 - activityId: {}, requirements: {}", activityId, requirements, e);
            throw e;
        }
    }
    
    @Override
    public List<VolunteerMatchDTO> getVolunteersBySkill(Long activityId, Long skillCategoryId, String skillName) {
        // 获取活动技能要求
        List<ActivitySkillRequirementDTO> requirements = getActivitySkillRequirements(activityId);
        
        // 如果没有技能要求，返回空列表
        if (requirements.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 构建查询条件
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getStatus, 1); // 只查询正常状态的技能
        
        if (skillCategoryId != null) {
            wrapper.eq(UserSkill::getSkillCategoryId, skillCategoryId);
        }
        
        if (skillName != null && !skillName.isEmpty()) {
            wrapper.like(UserSkill::getSkillName, skillName);
        }
        
        // 获取匹配的技能
        List<UserSkill> skills = userSkillService.list(wrapper);
        
        // 转换为匹配DTO
        return skills.stream().map(skill -> {
            VolunteerMatchDTO dto = new VolunteerMatchDTO();
            dto.setUserId(skill.getUserId());
            dto.setSkillId(skill.getId());
            dto.setSkillName(skill.getSkillName());
            dto.setSkillLevel(skill.getSkillLevel());
            dto.setSkillLevelText(getLevelText(skill.getSkillLevel()));
            dto.setVerified(skill.getVerified());
            
            // 计算匹配度
            ActivitySkillRequirementDTO matchedRequirement = findMatchingRequirement(skill, requirements);
            if (matchedRequirement != null) {
                dto.setRequiredLevel(matchedRequirement.getRequiredLevel());
                dto.setRequiredLevelText(matchedRequirement.getRequiredLevelText());
                dto.setMatchScore(calculateMatchScore(skill.getSkillLevel(), matchedRequirement.getRequiredLevel()));
                dto.setIsMatched(skill.getSkillLevel() >= matchedRequirement.getRequiredLevel());
            } else {
                dto.setMatchScore(0.0);
                dto.setIsMatched(false);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public java.util.List<String> getSkillMatchKeywords(Long userId, Long activityId) {
        try {
            // 获取用户技能
            List<UserSkillDTO> userSkills = userSkillService.getUserSkillsByCategory(userId, null);
            
            if (userSkills == null || userSkills.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 获取活动信息（包含requirements字段）
            Activity activity = activityService.getById(activityId);
            if (activity == null) {
                log.warn("活动不存在 - activityId: {}", activityId);
                return new ArrayList<>();
            }
            
            String requirementsText = activity.getRequirements();
            
            // 如果requirements为空，返回空列表
            if (requirementsText == null || requirementsText.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            // 在文本中查找匹配的技能
            List<String> matchedKeywords = new ArrayList<>();
            String lowerText = requirementsText.toLowerCase();
            
            for (UserSkillDTO skill : userSkills) {
                String skillName = skill.getSkillName().toLowerCase();
                if (lowerText.contains(skillName)) {
                    matchedKeywords.add(skillName);
                }
            }
            
            return matchedKeywords;
        } catch (Exception e) {
            log.error("获取技能匹配关键词失败 - userId: {}, activityId: {}", userId, activityId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public double calculateSkillMatchScore(Long userId, Long activityId) {
        try {
            log.info("开始计算技能匹配度 - userId: {}, activityId: {}", userId, activityId);
            
            // 获取用户技能
            List<UserSkillDTO> userSkills = userSkillService.getUserSkillsByCategory(userId, null);
            log.info("用户技能列表 - userId: {}, skills: {}", userId, 
                userSkills.stream().map(UserSkillDTO::getSkillName).collect(Collectors.toList()));
            
            // 获取活动信息（包含requirements字段）
            Activity activity = activityService.getById(activityId);
            if (activity == null) {
                log.warn("活动不存在 - activityId: {}", activityId);
                return 0.0;
            }
            
            String requirementsText = activity.getRequirements();
            log.info("活动技能要求文本 - activityId: {}, requirements: {}", activityId, requirementsText);
            
            // 如果requirements为空，返回100%匹配度（所有用户都可以参与）
            if (requirementsText == null || requirementsText.trim().isEmpty()) {
                log.debug("活动无技能要求 - activityId: {}", activityId);
                return 100.0;
            }
            
            // 直接在文本中搜索用户技能关键词
            double matchScore = calculateSimpleTextMatch(userSkills, requirementsText);
            log.info("技能匹配度计算结果 - userId: {}, activityId: {}, score: {}", 
                userId, activityId, matchScore);
            
            return matchScore;
        } catch (Exception e) {
            log.error("计算技能匹配度失败 - userId: {}, activityId: {}", userId, activityId, e);
            return 0.0;
        }
    }
    
    @Override
    public List<VolunteerMatchDTO> getVolunteerSkillMatchDetails(Long userId, Long activityId) {
        // 获取用户技能
        List<UserSkillDTO> userSkills = userSkillService.getUserSkillsByCategory(userId, null);
        
        // 获取活动技能要求
        List<ActivitySkillRequirementDTO> requirements = getActivitySkillRequirements(activityId);
        
        List<VolunteerMatchDTO> matchDetails = new ArrayList<>();
        
        for (ActivitySkillRequirementDTO requirement : requirements) {
            UserSkillDTO matchedSkill = findMatchingUserSkill(requirement, userSkills);
            
            VolunteerMatchDTO dto = new VolunteerMatchDTO();
            dto.setUserId(userId);
            dto.setSkillName(requirement.getSkillName());
            dto.setSkillCategoryName(requirement.getSkillCategoryName());
            dto.setRequiredLevel(requirement.getRequiredLevel());
            dto.setRequiredLevelText(requirement.getRequiredLevelText());
            dto.setIsMatched(matchedSkill != null);
            
            if (matchedSkill != null) {
                dto.setSkillId(matchedSkill.getId());
                dto.setSkillLevel(matchedSkill.getSkillLevel());
                dto.setSkillLevelText(getLevelText(matchedSkill.getSkillLevel()));
                dto.setMatchScore(calculateMatchScore(matchedSkill.getSkillLevel(), requirement.getRequiredLevel()));
                dto.setVerified(matchedSkill.getVerified());
            } else {
                dto.setMatchScore(0.0);
            }
            
            matchDetails.add(dto);
        }
        
        return matchDetails;
    }
    
    /**
     * 查找匹配的技能要求
     */
    private ActivitySkillRequirementDTO findMatchingRequirement(UserSkill skill, List<ActivitySkillRequirementDTO> requirements) {
        return requirements.stream()
                .filter(req -> req.getSkillName().equals(skill.getSkillName()) || 
                              (req.getSkillCategoryId() != null && req.getSkillCategoryId().equals(skill.getSkillCategoryId())))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 查找匹配的用户技能
     */
    private UserSkillDTO findMatchingUserSkill(ActivitySkillRequirementDTO requirement, List<UserSkillDTO> userSkills) {
        return userSkills.stream()
                .filter(skill -> skill.getSkillName().equals(requirement.getSkillName()) || 
                               (requirement.getSkillCategoryId() != null && requirement.getSkillCategoryId().equals(skill.getSkillCategoryId())))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 计算匹配分数
     */
    private double calculateMatchScore(Integer userLevel, Integer requiredLevel) {
        if (userLevel == null || requiredLevel == null) {
            return 0.0;
        }
        
        if (userLevel >= requiredLevel) {
            // 用户技能等级满足要求
            double baseScore = 80.0; // 基础分数
            double bonusScore = (userLevel - requiredLevel) * 5.0; // 超出部分加分
            return Math.min(100.0, baseScore + bonusScore);
        } else {
            // 用户技能等级不满足要求
            double penaltyScore = (requiredLevel - userLevel) * 15.0; // 不足部分扣分
            return Math.max(0.0, 60.0 - penaltyScore);
        }
    }
    
    /**
     * 计算简单文本匹配度（直接在文本中搜索技能关键词）
     */
    private double calculateSimpleTextMatch(List<UserSkillDTO> userSkills, String requirementsText) {
        if (userSkills == null || userSkills.isEmpty()) {
            log.warn("用户技能为空，返回0分");
            return 0.0;
        }
        
        // 收集用户技能名称（转换为小写）
        List<String> userSkillNames = userSkills.stream()
            .map(skill -> skill.getSkillName().toLowerCase())
            .collect(Collectors.toList());
        
        log.info("用户技能列表: {}", userSkillNames);
        log.info("活动要求文本: {}", requirementsText);
        
        // 从活动要求文本中提取技能要求列表（按逗号、顿号、换行分隔）
        List<String> requiredSkills = new ArrayList<>();
        String[] separators = {",", "，", "、", "\n", " "};
        String trimmedText = requirementsText.trim();
        
        if (!trimmedText.isEmpty()) {
            List<String> tempList = new ArrayList<>();
            tempList.add(trimmedText);
            
            for (String separator : separators) {
                List<String> newList = new ArrayList<>();
                for (String item : tempList) {
                    String[] parts = item.split(separator);
                    for (String part : parts) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) {
                            newList.add(trimmed);
                        }
                    }
                }
                tempList = newList;
            }
            requiredSkills = tempList;
        }
        
        log.info("活动要求技能列表: {}", requiredSkills);
        
        // 统计匹配的技能数（注意：这里是统计用户满足了多少个活动要求）
        int matchedCount = 0;
        List<String> matchedKeywords = new ArrayList<>();
        
        // 对每个活动要求，检查用户是否有对应的技能
        for (String requiredSkill : requiredSkills) {
            String lowerRequired = requiredSkill.toLowerCase();
            boolean matched = false;
            
            // 检查用户技能列表中是否有匹配的
            for (String userSkill : userSkillNames) {
                if (lowerRequired.contains(userSkill) || userSkill.contains(lowerRequired)) {
                    matched = true;
                    matchedKeywords.add(requiredSkill);
                    log.info("技能匹配成功 - 活动要求: '{}', 用户技能: '{}'", requiredSkill, userSkill);
                    break;
                }
            }
            
            if (matched) {
                matchedCount++;
            }
        }
        
        // 计算匹配度：匹配率 = 用户满足的要求数 / 活动要求总数
        if (requiredSkills.isEmpty()) {
            return 100.0; // 没有要求，默认100%
        }
        
        double matchRatio = (double) matchedCount / requiredSkills.size();
        double matchScore = matchRatio * 100.0;
        
        log.info("技能匹配统计 - 活动要求数: {}, 用户满足数: {}, 匹配关键词: {}, 匹配率: {}, 最终分数: {}", 
            requiredSkills.size(), matchedCount, matchedKeywords, matchRatio, matchScore);
        
        return Math.min(100.0, matchScore);
    }
    
    /**
     * 获取等级文本
     */
    private String getLevelText(Integer level) {
        if (level == null) return "未知";
        
        switch (level) {
            case 1: return "初级";
            case 2: return "中级";
            case 3: return "高级";
            case 4: return "专家级";
            default: return "未知";
        }
    }
}


