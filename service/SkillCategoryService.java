package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.entity.SkillCategory;

import java.util.List;

/**
 * 技能分类Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
public interface SkillCategoryService extends IService<SkillCategory> {
    
    /**
     * 获取所有启用的技能分类
     */
    List<SkillCategory> getEnabledCategories();
    
    /**
     * 获取技能分类树形结构
     */
    List<SkillCategory> getCategoryTree();
    
    /**
     * 根据父分类ID获取子分类
     */
    List<SkillCategory> getSubCategories(Long parentId);
}
