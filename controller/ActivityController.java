package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Activity;
import com.volunteer.service.ActivityService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    /**
     * C端活动列表查询
     */
    @GetMapping("/activities")
    public Result<Page<Activity>> list(@RequestParam(defaultValue = "1") int pageNo,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) String location,
                                      @RequestParam(required = false) String startTimeBegin,
                                      @RequestParam(required = false) String startTimeEnd) {
        log.info("活动列表查询 - pageNo: {}, pageSize: {}, keyword: {}, categoryId: {}, status: {}, location: {}, startTimeBegin: {}, startTimeEnd: {}", 
            pageNo, pageSize, keyword, categoryId, status, location, startTimeBegin, startTimeEnd);
        
        Page<Activity> result = activityService.pageList(pageNo, pageSize, keyword, categoryId, status, 
            location, startTimeBegin, startTimeEnd);
        
        log.info("活动列表查询结果 - 总数: {}", result.getTotal());
        return Result.success(result);
    }

    /**
     * C端活动详情
     */
    @GetMapping("/activities/{id}")
    public Result<Activity> detail(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        return Result.success(activity);
    }

    /**
     * 获取用户参与的活动列表（用于服务记录选择）
     */
    @GetMapping("/user/activities")
    public Result<List<Activity>> getUserParticipatedActivities(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            List<Activity> activities = activityService.getUserParticipatedActivities(userId);
            return Result.success(activities);
        } catch (Exception e) {
            log.error("获取用户参与活动失败", e);
            return Result.error("获取用户参与活动失败");
        }
    }

    /**
     * C端创建活动
     */
    @PostMapping("/activities")
    public Result<Boolean> create(@RequestBody Activity activity) {
        boolean result = activityService.create(activity);
        return Result.success(result);
    }

    /**
     * C端更新活动
     */
    @PutMapping("/activities/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        boolean result = activityService.update(activity);
        return Result.success(result);
    }

    /**
     * C端删除活动
     */
    @DeleteMapping("/activities/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean result = activityService.delete(id);
        return Result.success(result);
    }

    /**
     * 管理后台活动列表查询
     */
    @GetMapping("/admin/activities")
    public Result<Page<Activity>> adminList(@RequestParam(defaultValue = "1") int pageNo,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Long organizerId) {
        Page<Activity> result = activityService.adminPageList(pageNo, pageSize, keyword, categoryId, status, organizerId);
        return Result.success(result);
    }

    /**
     * 管理后台活动详情
     */
    @GetMapping("/admin/activities/{id}")
    public Result<Activity> adminDetail(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        return Result.success(activity);
    }

    /**
     * 管理后台创建活动
     */
    @PostMapping("/admin/activities")
    public Result<Boolean> adminCreate(@RequestBody Activity activity) {
        boolean result = activityService.create(activity);
        return Result.success(result);
    }

    /**
     * 管理后台更新活动
     */
    @PutMapping("/admin/activities/{id}")
    public Result<Boolean> adminUpdate(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        boolean result = activityService.update(activity);
        return Result.success(result);
    }

    /**
     * 管理后台删除活动
     */
    @DeleteMapping("/admin/activities/{id}")
    public Result<String> adminDelete(@PathVariable Long id) {
        String result = activityService.deleteWithCheck(id);
        if ("删除成功".equals(result)) {
            return Result.success(result);
        } else {
            return Result.error(result);
        }
    }

    /**
     * 管理后台批量删除活动
     */
    @DeleteMapping("/admin/activities/batch")
    public Result<Boolean> adminBatchDelete(@RequestBody List<Long> ids) {
        boolean result = activityService.batchDelete(ids);
        return Result.success(result);
    }

    /**
     * 管理后台更新活动状态
     */
    @PutMapping("/admin/activities/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody Activity activity) {
        boolean result = activityService.updateStatus(id, activity.getStatus());
        return Result.success(result);
    }

    /**
     * 管理后台批量更新活动状态
     */
    @PutMapping("/admin/activities/batch/status")
    public Result<Boolean> batchUpdateStatus(@RequestBody List<Long> ids, @RequestParam Integer status) {
        boolean result = activityService.batchUpdateStatus(ids, status);
        return Result.success(result);
    }

    /**
     * 管理后台导出活动数据
     */
    @GetMapping("/admin/activities/export")
    public Result<String> exportActivities(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) Integer status) {
        String exportUrl = activityService.exportActivities(keyword, categoryId, status);
        return Result.success(exportUrl);
    }
}


