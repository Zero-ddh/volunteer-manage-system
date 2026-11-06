package com.volunteer.controller;

import com.volunteer.entity.PointsRuleConfig;
import com.volunteer.service.PointsRuleConfigService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 积分规则配置控制器
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin/points-rules")
public class PointsRuleConfigController {
    
    @Autowired
    private PointsRuleConfigService pointsRuleConfigService;
    
    /**
     * 获取积分规则列表
     */
    @GetMapping("/list")
    public Result<List<PointsRuleConfig>> getRulesList() {
        try {
            List<PointsRuleConfig> rules = pointsRuleConfigService.list();
            return Result.success(rules);
        } catch (Exception e) {
            log.error("获取积分规则列表失败", e);
            return Result.error("获取积分规则列表失败");
        }
    }
    
    /**
     * 获取积分规则详情
     */
    @GetMapping("/{id}")
    public Result<PointsRuleConfig> getRuleDetail(@PathVariable Long id) {
        try {
            PointsRuleConfig rule = pointsRuleConfigService.getById(id);
            return Result.success(rule);
        } catch (Exception e) {
            log.error("获取积分规则详情失败", e);
            return Result.error("获取积分规则详情失败");
        }
    }
    
    /**
     * 更新积分规则
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateRule(@PathVariable Long id, @RequestBody PointsRuleConfig rule) {
        try {
            rule.setId(id);
            boolean success = pointsRuleConfigService.updateRule(rule);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新积分规则失败", e);
            return Result.error("更新积分规则失败");
        }
    }
    
    /**
     * 启用/禁用积分规则
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> toggleRuleStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = pointsRuleConfigService.toggleRuleStatus(id, status);
            return success ? Result.success(true) : Result.error("操作失败");
        } catch (Exception e) {
            log.error("切换积分规则状态失败", e);
            return Result.error("切换积分规则状态失败");
        }
    }
    
    /**
     * 手动生成积分数据
     */
    @PostMapping("/generate-points")
    public Result<Map<String, Object>> generatePointsData() {
        try {
            Map<String, Object> result = pointsRuleConfigService.generatePointsData();
            return Result.success(result);
        } catch (Exception e) {
            log.error("手动生成积分数据失败", e);
            return Result.error("生成积分数据失败: " + e.getMessage());
        }
    }
}
