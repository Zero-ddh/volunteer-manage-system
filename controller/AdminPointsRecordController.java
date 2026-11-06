package com.volunteer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.volunteer.entity.UserPoints;
import com.volunteer.service.UserPointsService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 积分记录管理控制器（管理后台）
 * 
 * @author volunteer-team
 * @since 2025-01-23
 */
@Slf4j
@RestController
@RequestMapping("/admin/points-record")
@CrossOrigin(origins = "*")
public class AdminPointsRecordController {

    @Autowired
    private UserPointsService userPointsService;

    /**
     * 分页查询积分记录
     */
    @GetMapping("/page")
    public Result<IPage<UserPoints>> getPointsRecordsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer pointsType,
            @RequestParam(required = false) String source) {
        
        try {
            log.info("分页查询积分记录: current={}, size={}, username={}, realName={}, pointsType={}, source={}", 
                current, size, username, realName, pointsType, source);
            
            IPage<UserPoints> page = userPointsService.getPointsRecordsPage(current, size, username, realName, pointsType, source);
            
            log.info("分页查询积分记录成功: 总记录数={}, 当前页={}, 每页大小={}", 
                page.getTotal(), page.getCurrent(), page.getSize());
            
            return Result.success(page);
            
        } catch (Exception e) {
            log.error("分页查询积分记录失败", e);
            return Result.error("查询积分记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取积分记录详情
     */
    @GetMapping("/{id}")
    public Result<UserPoints> getPointsRecordById(@PathVariable Long id) {
        try {
            log.info("获取积分记录详情: id={}", id);
            
            UserPoints pointsRecord = userPointsService.getById(id);
            if (pointsRecord == null) {
                return Result.error("积分记录不存在");
            }
            
            log.info("获取积分记录详情成功: id={}", id);
            return Result.success(pointsRecord);
            
        } catch (Exception e) {
            log.error("获取积分记录详情失败: id={}", id, e);
            return Result.error("获取积分记录详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取积分类型选项
     */
    @GetMapping("/points-types")
    public Result<Object> getPointsTypes() {
        try {
            Map<String, Object> earnType = new HashMap<>();
            earnType.put("value", 1);
            earnType.put("label", "获得积分");
            
            Map<String, Object> spendType = new HashMap<>();
            spendType.put("value", 2);
            spendType.put("label", "消费积分");
            
            return Result.success(new Object[]{earnType, spendType});
        } catch (Exception e) {
            log.error("获取积分类型选项失败", e);
            return Result.error("获取积分类型选项失败: " + e.getMessage());
        }
    }
}
