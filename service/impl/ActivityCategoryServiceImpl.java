package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.mapper.ActivityCategoryMapper;
import com.volunteer.service.ActivityCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 活动分类服务实现类
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Slf4j
@Service
public class ActivityCategoryServiceImpl implements ActivityCategoryService {

    @Resource
    private ActivityCategoryMapper categoryMapper;

    @Override
    public boolean create(ActivityCategory category) {
        try {
            // 检查分类编码是否已存在
            ActivityCategory existCategory = getByCategoryCode(category.getCategoryCode());
            if (existCategory != null) {
                throw new RuntimeException("分类编码已存在");
            }
            
            // 设置默认状态
            if (category.getStatus() == null) {
                category.setStatus(1);
            }
            
            return categoryMapper.insert(category) > 0;
        } catch (Exception e) {
            log.error("创建活动分类失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean update(ActivityCategory category) {
        try {
            // 检查分类编码是否与其他记录冲突
            ActivityCategory existCategory = getByCategoryCode(category.getCategoryCode());
            if (existCategory != null && !existCategory.getId().equals(category.getId())) {
                throw new RuntimeException("分类编码已存在");
            }
            
            return categoryMapper.updateById(category) > 0;
        } catch (Exception e) {
            log.error("更新活动分类失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            return categoryMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除活动分类失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ActivityCategory getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public ActivityCategory getByCategoryCode(String categoryCode) {
        LambdaQueryWrapper<ActivityCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityCategory::getCategoryCode, categoryCode);
        return categoryMapper.selectOne(queryWrapper);
    }

    @Override
    public Page<ActivityCategory> pageList(int pageNo, int pageSize, String keyword) {
        LambdaQueryWrapper<ActivityCategory> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(ActivityCategory::getCategoryName, keyword)
                .or()
                .like(ActivityCategory::getCategoryCode, keyword)
                .or()
                .like(ActivityCategory::getDescription, keyword)
            );
        }
        
        // 按排序字段和创建时间排序
        queryWrapper.orderByAsc(ActivityCategory::getSortOrder)
                   .orderByDesc(ActivityCategory::getCreatedTime);
        
        Page<ActivityCategory> page = new Page<>(pageNo, pageSize);
        return categoryMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<ActivityCategory> getAllEnabled() {
        LambdaQueryWrapper<ActivityCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityCategory::getStatus, 1)
                   .orderByAsc(ActivityCategory::getSortOrder)
                   .orderByDesc(ActivityCategory::getCreatedTime);
        return categoryMapper.selectList(queryWrapper);
    }
}
