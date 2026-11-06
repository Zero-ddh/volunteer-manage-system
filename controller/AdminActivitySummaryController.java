package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ActivitySummary;
import com.volunteer.service.ActivitySummaryService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理后台活动总结管理控制器
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminActivitySummaryController {

    @Resource
    private ActivitySummaryService summaryService;

    /**
     * 分页查询活动总结列表
     */
    @PostMapping("/activity-summaries/list")
    public Result<Page<ActivitySummary>> list(@RequestBody java.util.Map<String, Object> params) {
        try {
            int pageNo = (Integer) params.getOrDefault("pageNo", 1);
            int pageSize = (Integer) params.getOrDefault("pageSize", 10);
            String keyword = (String) params.get("keyword");
            Long communityId = params.get("communityId") != null ? 
                Long.valueOf(params.get("communityId").toString()) : null;
            Integer status = params.get("status") != null ? 
                Integer.valueOf(params.get("status").toString()) : null;
            
            Page<ActivitySummary> result = summaryService.adminPageList(pageNo, pageSize, keyword, communityId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询活动总结列表失败：{}", e.getMessage());
            return Result.error("查询活动总结列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取活动总结详情
     */
    @GetMapping("/activity-summaries/{id}")
    public Result<ActivitySummary> detail(@PathVariable Long id) {
        try {
            ActivitySummary result = summaryService.getById(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取活动总结详情失败：{}", e.getMessage());
            return Result.error("获取活动总结详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建活动总结
     */
    @PostMapping("/activity-summaries")
    public Result<Boolean> create(@RequestBody ActivitySummary summary) {
        try {
            boolean result = summaryService.create(summary);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建活动总结失败：{}", e.getMessage());
            return Result.error("创建活动总结失败：" + e.getMessage());
        }
    }

    /**
     * 更新活动总结
     */
    @PutMapping("/activity-summaries/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody ActivitySummary summary) {
        try {
            summary.setId(id);
            boolean result = summaryService.update(summary);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新活动总结失败：{}", e.getMessage());
            return Result.error("更新活动总结失败：" + e.getMessage());
        }
    }

    /**
     * 更新活动总结状态
     */
    @PutMapping("/activity-summaries/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            if (status == null) {
                return Result.error("状态参数不能为空");
            }
            
            ActivitySummary summary = new ActivitySummary();
            summary.setId(id);
            summary.setStatus(status);
            
            boolean result = summaryService.update(summary);
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("更新状态失败");
            }
        } catch (Exception e) {
            log.error("更新活动总结状态失败：{}", e.getMessage());
            return Result.error("更新活动总结状态失败：" + e.getMessage());
        }
    }

    /**
     * 删除活动总结
     */
    @DeleteMapping("/activity-summaries/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            boolean result = summaryService.delete(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除活动总结失败：{}", e.getMessage());
            return Result.error("删除活动总结失败：" + e.getMessage());
        }
    }
}
