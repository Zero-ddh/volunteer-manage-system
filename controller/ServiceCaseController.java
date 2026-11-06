package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ServiceCase;
import com.volunteer.service.ServiceCaseService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 服务典型案例Controller
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/v1/service-case")
public class ServiceCaseController {

    @Resource
    private ServiceCaseService serviceCaseService;

    /**
     * 分页查询用户典型案例
     */
    @GetMapping("/user/list")
    public Result<Page<ServiceCase>> getUserServiceCases(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer caseType,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<ServiceCase> page = serviceCaseService.getUserServiceCases(userId, pageNo, pageSize, caseType);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取用户典型案例失败", e);
            return Result.error("获取用户典型案例失败");
        }
    }

    /**
     * 分页查询精选案例
     */
    @GetMapping("/featured/list")
    public Result<Page<ServiceCase>> getFeaturedCases(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer caseType) {
        try {
            Page<ServiceCase> page = serviceCaseService.getFeaturedCases(pageNo, pageSize, caseType);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取精选案例失败", e);
            return Result.error("获取精选案例失败");
        }
    }

    /**
     * 分页查询所有案例（管理后台）
     */
    @GetMapping("/admin/list")
    public Result<Page<ServiceCase>> getAllCases(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer caseType,
            @RequestParam(required = false) Integer status) {
        try {
            Page<ServiceCase> page = serviceCaseService.getAllCases(pageNo, pageSize, keyword, caseType, status);
            return Result.success(page);
        } catch (Exception e) {
            log.error("管理后台查询典型案例失败", e);
            return Result.error("查询典型案例失败");
        }
    }

    /**
     * 创建典型案例
     */
    @PostMapping
    public Result<Boolean> createServiceCase(@RequestBody ServiceCase serviceCase, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            serviceCase.setUserId(userId);
            boolean result = serviceCaseService.createServiceCase(serviceCase);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建典型案例失败", e);
            return Result.error("创建典型案例失败");
        }
    }

    /**
     * 更新典型案例
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateServiceCase(@PathVariable Long id, @RequestBody ServiceCase serviceCase) {
        try {
            serviceCase.setId(id);
            boolean result = serviceCaseService.updateServiceCase(serviceCase);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新典型案例失败", e);
            return Result.error("更新典型案例失败");
        }
    }

    /**
     * 获取案例详情
     */
    @GetMapping("/{id}")
    public Result<ServiceCase> getCaseDetail(@PathVariable Long id) {
        try {
            ServiceCase serviceCase = serviceCaseService.getCaseDetail(id);
            if (serviceCase == null) {
                return Result.error("案例不存在");
            }
            return Result.success(serviceCase);
        } catch (Exception e) {
            log.error("获取案例详情失败", e);
            return Result.error("获取案例详情失败");
        }
    }

    /**
     * 增加点赞次数
     */
    @PostMapping("/{id}/like")
    public Result<Boolean> incrementLikeCount(@PathVariable Long id) {
        try {
            boolean result = serviceCaseService.incrementLikeCount(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("点赞失败", e);
            return Result.error("点赞失败");
        }
    }

    /**
     * 设置精选状态（管理后台）
     */
    @PutMapping("/{id}/featured")
    public Result<Boolean> setFeatured(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            boolean featured = request.get("featured");
            boolean result = serviceCaseService.setFeatured(id, featured);
            return Result.success(result);
        } catch (Exception e) {
            log.error("设置精选状态失败", e);
            return Result.error("设置精选状态失败");
        }
    }

    /**
     * 审核案例（管理后台）
     */
    @PutMapping("/{id}/audit")
    public Result<Boolean> auditCase(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Integer status = (Integer) request.get("status");
            String remark = (String) request.get("remark");
            boolean result = serviceCaseService.auditCase(id, status, remark);
            return Result.success(result);
        } catch (Exception e) {
            log.error("审核案例失败", e);
            return Result.error("审核案例失败");
        }
    }

    /**
     * 获取案例统计信息
     */
    @GetMapping("/user/stats")
    public Result<Map<String, Object>> getCaseStats(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> stats = serviceCaseService.getCaseStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取案例统计失败", e);
            return Result.error("获取案例统计失败");
        }
    }
}
