package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.service.ActivityCategoryService;
import com.volunteer.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 活动分类控制器
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/v1")
public class ActivityCategoryController {

    @Resource
    private ActivityCategoryService categoryService;

    /**
     * 管理后台分页查询活动分类列表
     */
    @GetMapping("/admin/activity-categories")
    public Result<Page<ActivityCategory>> adminList(@RequestParam(defaultValue = "1") int pageNo,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) String keyword) {
        Page<ActivityCategory> result = categoryService.pageList(pageNo, pageSize, keyword);
        return Result.success(result);
    }

    /**
     * 管理后台获取活动分类详情
     */
    @GetMapping("/admin/activity-categories/{id}")
    public Result<ActivityCategory> adminDetail(@PathVariable Long id) {
        ActivityCategory category = categoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 管理后台创建活动分类
     */
    @PostMapping("/admin/activity-categories")
    public Result<Boolean> adminCreate(@RequestBody ActivityCategory category) {
        boolean result = categoryService.create(category);
        return Result.success(result);
    }

    /**
     * 管理后台更新活动分类
     */
    @PutMapping("/admin/activity-categories/{id}")
    public Result<Boolean> adminUpdate(@PathVariable Long id, @RequestBody ActivityCategory category) {
        category.setId(id);
        boolean result = categoryService.update(category);
        return Result.success(result);
    }

    /**
     * 管理后台删除活动分类
     */
    @DeleteMapping("/admin/activity-categories/{id}")
    public Result<Boolean> adminDelete(@PathVariable Long id) {
        boolean result = categoryService.delete(id);
        return Result.success(result);
    }

    /**
     * 获取所有启用的活动分类（用于下拉选择）
     */
    @GetMapping("/admin/activity-categories/enabled")
    public Result<List<ActivityCategory>> getAllEnabled() {
        List<ActivityCategory> result = categoryService.getAllEnabled();
        return Result.success(result);
    }

    /**
     * C端获取所有启用的活动分类
     */
    @GetMapping("/activity-categories")
    public Result<List<ActivityCategory>> list() {
        List<ActivityCategory> result = categoryService.getAllEnabled();
        return Result.success(result);
    }
}
