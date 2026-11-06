package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ActivitySummary;
import com.volunteer.service.ActivitySummaryService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台社群活动总结管理控制器
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Slf4j
@RestController
@RequestMapping("/admin/community-summaries")
public class AdminCommunitySummaryController {

    @Autowired
    private ActivitySummaryService summaryService;

    /**
     * 分页查询活动总结列表
     */
    @PostMapping("/summaries")
    public Result<Page<ActivitySummary>> list(@RequestBody Map<String, Object> params) {
        try {
            int pageNo = (Integer) params.getOrDefault("pageNo", 1);
            int pageSize = (Integer) params.getOrDefault("pageSize", 10);
            String keyword = (String) params.get("keyword");
            Long organizerId = params.get("organizerId") instanceof Integer ? 
                ((Integer) params.get("organizerId")).longValue() : 
                (Long) params.get("organizerId");

            Page<ActivitySummary> result = summaryService.pageList(pageNo, pageSize, keyword, organizerId);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询活动总结列表失败：{}", e.getMessage());
            return Result.error("查询活动总结列表失败：" + e.getMessage());
        }
    }
}
