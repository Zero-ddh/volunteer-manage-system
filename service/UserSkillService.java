package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.dto.UserSkillDTO;
import com.volunteer.entity.UserSkill;

import java.util.List;

/**
 * 用户技能Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
public interface UserSkillService extends IService<UserSkill> {
    
    /**
     * 获取用户技能列表
     */
    List<UserSkillDTO> getUserSkills(Long userId);
    
    /**
     * 根据技能分类获取用户技能
     */
    List<UserSkillDTO> getUserSkillsByCategory(Long userId, Long categoryId);
    
    /**
     * 更新技能验证状态
     */
    boolean updateSkillVerified(Long skillId, Integer verified);
}