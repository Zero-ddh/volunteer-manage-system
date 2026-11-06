package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ActivityCategory;

/**
 * 活动分类服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
public interface ActivityCategoryService {

    /**
     * 创建活动分类
     */
    boolean create(ActivityCategory category);

    /**
     * 更新活动分类
     */
    boolean update(ActivityCategory category);

    /**
     * 删除活动分类
     */
    boolean delete(Long id);

    /**
     * 根据ID获取活动分类
     */
    ActivityCategory getById(Long id);

    /**
     * 根据分类编码获取活动分类
     */
    ActivityCategory getByCategoryCode(String categoryCode);

    /**
     * 分页查询活动分类列表
     */
    Page<ActivityCategory> pageList(int pageNo, int pageSize, String keyword);

    /**
     * 获取所有启用的活动分类
     */
    java.util.List<ActivityCategory> getAllEnabled();
}
