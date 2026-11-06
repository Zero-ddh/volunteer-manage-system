package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ServiceRecord;
import com.volunteer.service.ServiceRecordService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 服务记录Controller
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/v1/service-record")
public class ServiceRecordController {

    @Resource
    private ServiceRecordService serviceRecordService;

    /**
     * 分页查询用户服务记录
     */
    @GetMapping("/user/list")
    public Result<Page<ServiceRecord>> getUserServiceRecords(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<ServiceRecord> page = serviceRecordService.getUserServiceRecords(userId, pageNo, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取用户服务记录失败", e);
            return Result.error("获取用户服务记录失败");
        }
    }

    /**
     * 获取服务记录详情
     */
    @GetMapping("/{id}")
    public Result<ServiceRecord> getServiceRecordDetail(@PathVariable Long id) {
        try {
            ServiceRecord serviceRecord = serviceRecordService.getServiceRecordDetail(id);
            if (serviceRecord == null) {
                return Result.error("服务记录不存在");
            }
            return Result.success(serviceRecord);
        } catch (Exception e) {
            log.error("获取服务记录详情失败", e);
            return Result.error("获取服务记录详情失败");
        }
    }

    /**
     * 创建服务记录
     */
    @PostMapping
    public Result<Boolean> createServiceRecord(@RequestBody ServiceRecord serviceRecord, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            serviceRecord.setUserId(userId);
            
            // 设置默认值
            if (serviceRecord.getIsTypicalCase() == null) {
                serviceRecord.setIsTypicalCase(0);
            }
            if (serviceRecord.getIsFeaturedCase() == null) {
                serviceRecord.setIsFeaturedCase(0);
            }
            
            boolean result = serviceRecordService.createServiceRecord(serviceRecord);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建服务记录失败", e);
            return Result.error("创建服务记录失败");
        }
    }

    /**
     * 更新服务记录
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateServiceRecord(@PathVariable Long id, @RequestBody ServiceRecord serviceRecord) {
        try {
            serviceRecord.setId(id);
            boolean result = serviceRecordService.updateServiceRecord(serviceRecord);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新服务记录失败", e);
            return Result.error("更新服务记录失败");
        }
    }

    /**
     * 更新服务心得
     */
    @PutMapping("/{id}/experience")
    public Result<Boolean> updateServiceExperience(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String experience = request.get("experience");
            boolean result = serviceRecordService.updateServiceExperience(id, experience);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新服务心得失败", e);
            return Result.error("更新服务心得失败");
        }
    }

    /**
     * 上传服务照片
     */
    @PostMapping("/{id}/photos")
    public Result<Boolean> uploadServicePhotos(@PathVariable Long id, @RequestBody Map<String, List<String>> request) {
        try {
            List<String> photoUrls = request.get("photoUrls");
            boolean result = serviceRecordService.uploadServicePhotos(id, photoUrls);
            return Result.success(result);
        } catch (Exception e) {
            log.error("上传服务照片失败", e);
            return Result.error("上传服务照片失败");
        }
    }

    /**
     * 获取用户服务足迹数据
     */
    @GetMapping("/user/footprints")
    public Result<List<ServiceRecord>> getUserServiceFootprints(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<ServiceRecord> footprints = serviceRecordService.getUserServiceFootprints(userId);
            return Result.success(footprints);
        } catch (Exception e) {
            log.error("获取用户服务足迹失败", e);
            return Result.error("获取用户服务足迹失败");
        }
    }

    /**
     * 获取用户服务统计
     */
    @GetMapping("/user/stats")
    public Result<Map<String, Object>> getUserServiceStats(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> stats = serviceRecordService.getUserServiceStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户服务统计失败", e);
            return Result.error("获取用户服务统计失败");
        }
    }

    /**
     * 获取服务地点统计
     */
    @GetMapping("/user/location-stats")
    public Result<List<Map<String, Object>>> getServiceLocationStats(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Map<String, Object>> stats = serviceRecordService.getServiceLocationStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取服务地点统计失败", e);
            return Result.error("获取服务地点统计失败");
        }
    }

    /**
     * 获取用户积分记录（支持分页和搜索）
     */
    @GetMapping("/user/points")
    public Result<Page<Map<String, Object>>> getUserPointsRecords(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer pointsType,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<Map<String, Object>> pointsRecords = serviceRecordService.getUserPointsRecords(userId, pageNo, pageSize, pointsType);
            return Result.success(pointsRecords);
        } catch (Exception e) {
            log.error("获取用户积分记录失败", e);
            return Result.error("获取用户积分记录失败");
        }
    }

    /**
     * 管理后台分页查询服务记录
     */
    @GetMapping("/admin/list")
    public Result<Page<ServiceRecord>> adminPageList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Integer status) {
        try {
            Page<ServiceRecord> page = serviceRecordService.adminPageList(pageNo, pageSize, keyword, userId, activityId, status);
            return Result.success(page);
        } catch (Exception e) {
            log.error("管理后台查询服务记录失败", e);
            return Result.error("查询服务记录失败");
        }
    }

    // ========== 典型案例相关API ==========

    /**
     * 分页查询用户典型案例
     */
    @GetMapping("/user/typical-cases")
    public Result<Page<ServiceRecord>> getUserTypicalCases(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer caseType,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<ServiceRecord> page = serviceRecordService.getUserTypicalCases(userId, pageNo, pageSize, caseType);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取用户典型案例失败", e);
            return Result.error("获取用户典型案例失败");
        }
    }

    /**
     * 获取典型案例详情
     */
    @GetMapping("/typical-case/{id}")
    public Result<ServiceRecord> getTypicalCaseDetail(@PathVariable Long id) {
        try {
            ServiceRecord serviceRecord = serviceRecordService.getTypicalCaseDetail(id);
            if (serviceRecord == null) {
                return Result.error("典型案例不存在");
            }
            return Result.success(serviceRecord);
        } catch (Exception e) {
            log.error("获取典型案例详情失败", e);
            return Result.error("获取典型案例详情失败");
        }
    }

    /**
     * 创建典型案例
     */
    @PostMapping("/typical-case")
    public Result<Boolean> createTypicalCase(@RequestBody ServiceRecord serviceRecord, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            serviceRecord.setUserId(userId);
            serviceRecord.setIsTypicalCase(1); // 设置为典型案例
            boolean result = serviceRecordService.createTypicalCase(serviceRecord);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建典型案例失败", e);
            return Result.error("创建典型案例失败");
        }
    }

    /**
     * 更新典型案例
     */
    @PutMapping("/typical-case/{id}")
    public Result<Boolean> updateTypicalCase(@PathVariable Long id, @RequestBody ServiceRecord serviceRecord) {
        try {
            serviceRecord.setId(id);
            boolean result = serviceRecordService.updateTypicalCase(serviceRecord);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新典型案例失败", e);
            return Result.error("更新典型案例失败");
        }
    }

    /**
     * 设置典型案例精选状态
     */
    @PutMapping("/typical-case/{id}/featured")
    public Result<Boolean> setFeaturedCase(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            boolean isFeatured = request.get("isFeatured");
            boolean result = serviceRecordService.setFeaturedCase(id, isFeatured);
            return Result.success(result);
        } catch (Exception e) {
            log.error("设置典型案例精选状态失败", e);
            return Result.error("设置典型案例精选状态失败");
        }
    }

    /**
     * 增加案例浏览次数
     */
    @PostMapping("/typical-case/{id}/view")
    public Result<Boolean> incrementCaseViewCount(@PathVariable Long id) {
        try {
            boolean result = serviceRecordService.incrementCaseViewCount(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("增加案例浏览次数失败", e);
            return Result.error("增加案例浏览次数失败");
        }
    }

    /**
     * 增加案例点赞次数
     */
    @PostMapping("/typical-case/{id}/like")
    public Result<Boolean> incrementCaseLikeCount(@PathVariable Long id) {
        try {
            boolean result = serviceRecordService.incrementCaseLikeCount(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("增加案例点赞次数失败", e);
            return Result.error("增加案例点赞次数失败");
        }
    }

    /**
     * 获取典型案例统计
     */
    @GetMapping("/user/typical-case-stats")
    public Result<Map<String, Object>> getTypicalCaseStats(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> stats = serviceRecordService.getTypicalCaseStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取典型案例统计失败", e);
            return Result.error("获取典型案例统计失败");
        }
    }

    /**
     * 管理后台分页查询典型案例
     */
    @GetMapping("/admin/typical-cases")
    public Result<Page<ServiceRecord>> adminPageTypicalCases(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer caseType,
            @RequestParam(required = false) Integer isFeatured) {
        try {
            Page<ServiceRecord> page = serviceRecordService.adminPageTypicalCases(pageNo, pageSize, keyword, userId, caseType, isFeatured);
            return Result.success(page);
        } catch (Exception e) {
            log.error("管理后台查询典型案例失败", e);
            return Result.error("查询典型案例失败");
        }
    }
}
