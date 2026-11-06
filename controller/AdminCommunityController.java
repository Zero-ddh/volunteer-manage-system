package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Community;
import com.volunteer.service.CommunityService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台社群管理控制器
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Slf4j
@RestController
@RequestMapping("/admin/community")
public class AdminCommunityController {

    @Autowired
    private CommunityService communityService;

    /**
     * 分页查询社群列表
     */
    @PostMapping("/list")
    public Result<Page<Community>> list(@RequestBody Map<String, Object> params) {
        try {
            int pageNo = (Integer) params.getOrDefault("pageNo", 1);
            int pageSize = (Integer) params.getOrDefault("pageSize", 10);
            String keyword = (String) params.get("keyword");
            String category = (String) params.get("category");

            Page<Community> result = communityService.pageCommunities(pageNo, pageSize, keyword, category);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询社群列表失败：{}", e.getMessage());
            return Result.error("查询社群列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取社群详情
     */
    @GetMapping("/{id}")
    public Result<Community> getCommunity(@PathVariable Long id) {
        try {
            Community community = communityService.getCommunity(id);
            if (community == null) {
                return Result.error("社群不存在");
            }
            return Result.success(community);
        } catch (Exception e) {
            log.error("获取社群详情失败：{}", e.getMessage());
            return Result.error("获取社群详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建社群
     */
    @PostMapping("/create")
    public Result<String> createCommunity(@RequestBody Community community) {
        try {
            boolean success = communityService.createCommunity(community);
            if (success) {
                return Result.success("创建成功");
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建社群失败：{}", e.getMessage());
            return Result.error("创建社群失败：" + e.getMessage());
        }
    }

    /**
     * 更新社群
     */
    @PutMapping("/update")
    public Result<String> updateCommunity(@RequestBody Community community) {
        try {
            boolean success = communityService.updateCommunity(community);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新社群失败：{}", e.getMessage());
            return Result.error("更新社群失败：" + e.getMessage());
        }
    }

    /**
     * 删除社群
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteCommunity(@PathVariable Long id) {
        try {
            boolean success = communityService.deleteCommunity(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除社群失败：{}", e.getMessage());
            return Result.error("删除社群失败：" + e.getMessage());
        }
    }

    /**
     * 更新社群状态
     */
    @PutMapping("/status/{id}")
    public Result<String> updateCommunityStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            if (status == null) {
                return Result.error("状态参数不能为空");
            }
            
            boolean success = communityService.updateCommunityStatus(id, status);
            if (success) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新社群状态失败：{}", e.getMessage());
            return Result.error("更新社群状态失败：" + e.getMessage());
        }
    }
}
