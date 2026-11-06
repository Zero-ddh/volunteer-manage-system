package com.volunteer.controller;

import com.volunteer.service.VolunteerUserService;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 星级志愿者控制器
 * 
 * @author volunteer-team
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/v1/star-volunteers")
public class StarVolunteersController {
    
    @Autowired
    private VolunteerUserService volunteerUserService;
    
    /**
     * 获取星级志愿者列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getStarVolunteers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer starLevel,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> result = volunteerUserService.getStarVolunteers(name, starLevel, page, size);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取星级志愿者列表失败", e);
            return Result.error("获取星级志愿者列表失败");
        }
    }
    
    /**
     * 获取积分排行榜
     */
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getPointsRanking() {
        try {
            List<Map<String, Object>> ranking = volunteerUserService.getPointsRanking();
            return Result.success(ranking);
        } catch (Exception e) {
            log.error("获取积分排行榜失败", e);
            return Result.error("获取积分排行榜失败");
        }
    }
}
