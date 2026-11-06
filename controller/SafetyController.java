package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.EmergencyContact;
import com.volunteer.entity.EmergencyReport;
import com.volunteer.service.SafetyService;
import com.volunteer.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/v1/safety")
public class SafetyController {

    @Resource
    private SafetyService safetyService;

    private Long getCurrentUserId(HttpServletRequest request) {
        // 从JWT拦截器设置的属性中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }

    // 紧急联系人
    @GetMapping("/emergency-contact")
    public Result<List<EmergencyContact>> listContacts(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            return Result.success(safetyService.listContacts(userId));
        } catch (Exception e) {
            return Result.error("获取紧急联系人列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/emergency-contact")
    public Result<Boolean> createContact(@RequestBody EmergencyContact contact, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            contact.setUserId(userId);
            return Result.success(safetyService.createContact(contact));
        } catch (Exception e) {
            return Result.error("创建紧急联系人失败: " + e.getMessage());
        }
    }

    @PutMapping("/emergency-contact/{id}")
    public Result<Boolean> updateContact(@PathVariable Long id, @RequestBody EmergencyContact contact, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            contact.setId(id);
            contact.setUserId(userId);
            return Result.success(safetyService.updateContact(contact));
        } catch (Exception e) {
            return Result.error("更新紧急联系人失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/emergency-contact/{id}")
    public Result<Boolean> deleteContact(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            return Result.success(safetyService.deleteContact(id, userId));
        } catch (Exception e) {
            return Result.error("删除紧急联系人失败: " + e.getMessage());
        }
    }

    @PutMapping("/emergency-contact/{id}/primary")
    public Result<Boolean> setPrimary(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            return Result.success(safetyService.setPrimary(id, userId));
        } catch (Exception e) {
            return Result.error("设置主联系人失败: " + e.getMessage());
        }
    }

    // 紧急上报
    @PostMapping("/emergency-report")
    public Result<Boolean> createReport(@RequestBody EmergencyReport report, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            report.setReporterUserId(userId);
            return Result.success(safetyService.createReport(report));
        } catch (Exception e) {
            return Result.error("创建紧急上报失败: " + e.getMessage());
        }
    }

    @GetMapping("/emergency-report/{id}")
    public Result<EmergencyReport> getReport(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            return Result.success(safetyService.getReport(id, userId));
        } catch (Exception e) {
            return Result.error("获取紧急上报失败: " + e.getMessage());
        }
    }

    @GetMapping("/emergency-report/list")
    public Result<Page<EmergencyReport>> listMyReports(@RequestParam int pageNo, @RequestParam int pageSize, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            return Result.success(safetyService.listMyReports(userId, pageNo, pageSize));
        } catch (Exception e) {
            return Result.error("获取紧急上报列表失败: " + e.getMessage());
        }
    }

    // 后台处理：更新上报状态
    @PutMapping("/emergency-report/{id}/status")
    public Result<Boolean> updateReportStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            return Result.success(safetyService.updateReportStatus(id, status));
        } catch (Exception e) {
            return Result.error("更新上报状态失败: " + e.getMessage());
        }
    }
}


