package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.SkillCategory;
import com.volunteer.mapper.SkillCategoryMapper;
import com.volunteer.service.SkillCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能分类Service实现类
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Service
public class SkillCategoryServiceImpl extends ServiceImpl<SkillCategoryMapper, SkillCategory> implements SkillCategoryService {
    
    @Override
    public List<SkillCategory> getEnabledCategories() {
        LambdaQueryWrapper<SkillCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillCategory::getStatus, 1)
               .orderByAsc(SkillCategory::getSortOrder);
        return list(wrapper);
    }
    
    @Override
    public List<SkillCategory> getCategoryTree() {
        List<SkillCategory> allCategories = getEnabledCategories();
        
        // 构建树形结构
        List<SkillCategory> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId() == null || category.getParentId() == 0)
                .collect(Collectors.toList());
        
        for (SkillCategory root : rootCategories) {
            buildTree(root, allCategories);
        }
        
        return rootCategories;
    }
    
    @Override
    public List<SkillCategory> getSubCategories(Long parentId) {
        LambdaQueryWrapper<SkillCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillCategory::getParentId, parentId)
               .eq(SkillCategory::getStatus, 1)
               .orderByAsc(SkillCategory::getSortOrder);
        return list(wrapper);
    }
    
    /**
     * 递归构建树形结构
     */
    private void buildTree(SkillCategory parent, List<SkillCategory> allCategories) {
        List<SkillCategory> children = allCategories.stream()
                .filter(category -> parent.getId().equals(category.getParentId()))
                .collect(Collectors.toList());
        
        parent.setChildren(children);
        
        for (SkillCategory child : children) {
            buildTree(child, allCategories);
        }
    }
}
