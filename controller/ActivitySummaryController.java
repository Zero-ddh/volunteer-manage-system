package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ActivitySummary;
import com.volunteer.service.ActivitySummaryService;
import com.volunteer.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 活动总结控制器
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/v1")
public class ActivitySummaryController {

    @Resource
    private ActivitySummaryService summaryService;

    /**
     * 创建活动总结
     */
    @PostMapping("/activity-summaries")
    public Result<Boolean> create(@RequestBody ActivitySummary summary) {
        boolean result = summaryService.create(summary);
        return Result.success(result);
    }

    /**
     * 更新活动总结
     */
    @PutMapping("/activity-summaries/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody ActivitySummary summary) {
        summary.setId(id);
        boolean result = summaryService.update(summary);
        return Result.success(result);
    }

    /**
     * 删除活动总结
     */
    @DeleteMapping("/activity-summaries/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean result = summaryService.delete(id);
        return Result.success(result);
    }

    /**
     * 获取活动总结详情
     */
    @GetMapping("/activity-summaries/{id}")
    public Result<ActivitySummary> detail(@PathVariable Long id) {
        ActivitySummary summary = summaryService.getById(id);
        return Result.success(summary);
    }

    /**
     * 根据社群ID获取活动总结
     */
    @GetMapping("/communities/{communityId}/summary")
    public Result<ActivitySummary> getByCommunityId(@PathVariable Long communityId) {
        ActivitySummary summary = summaryService.getByCommunityId(communityId);
        return Result.success(summary);
    }

    /**
     * 分页查询活动总结列表
     */
    @GetMapping("/activity-summaries")
    public Result<Page<ActivitySummary>> list(@RequestParam(defaultValue = "1") int pageNo,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long communityId,
                                            @RequestParam(required = false) Integer status) {
        Page<ActivitySummary> result = summaryService.pageList(pageNo, pageSize, keyword, communityId, status);
        return Result.success(result);
    }

    /**
     * 管理后台分页查询活动总结列表
     */
    @GetMapping("/admin/activity-summaries")
    public Result<Page<ActivitySummary>> adminList(@RequestParam(defaultValue = "1") int pageNo,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Long communityId) {
        Page<ActivitySummary> result = summaryService.pageList(pageNo, pageSize, keyword, communityId);
        return Result.success(result);
    }

    /**
     * 管理后台创建活动总结
     */
    @PostMapping("/admin/activity-summaries")
    public Result<Boolean> adminCreate(@RequestBody ActivitySummary summary) {
        boolean result = summaryService.create(summary);
        return Result.success(result);
    }

    /**
     * 管理后台更新活动总结
     */
    @PutMapping("/admin/activity-summaries/{id}")
    public Result<Boolean> adminUpdate(@PathVariable Long id, @RequestBody ActivitySummary summary) {
        summary.setId(id);
        boolean result = summaryService.update(summary);
        return Result.success(result);
    }

    /**
     * 管理后台删除活动总结
     */
    @DeleteMapping("/admin/activity-summaries/{id}")
    public Result<Boolean> adminDelete(@PathVariable Long id) {
        boolean result = summaryService.delete(id);
        return Result.success(result);
    }
}
