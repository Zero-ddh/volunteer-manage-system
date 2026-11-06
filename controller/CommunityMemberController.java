package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Community;
import com.volunteer.service.CommunityMemberService;
import com.volunteer.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/community")
public class CommunityMemberController {

    @Resource
    private CommunityMemberService communityMemberService;

    @PostMapping("/{id}/join")
    public Result<Boolean> join(@PathVariable("id") Long communityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean result = communityMemberService.join(communityId, userId);
        return Result.success(result);
    }

    @PostMapping("/{id}/quit")
    public Result<Boolean> quit(@PathVariable("id") Long communityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean result = communityMemberService.quit(communityId, userId);
        return Result.success(result);
    }

    @GetMapping("/{id}/is-member")
    public Result<Boolean> isMember(@PathVariable("id") Long communityId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean result = communityMemberService.isMember(communityId, userId);
        return Result.success(result);
    }
    
    /**
     * 获取用户加入的社群列表
     */
    @GetMapping("/my-communities")
    public Result<Page<Community>> getMyCommunities(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Community> result = communityMemberService.getUserCommunities(userId, pageNo, pageSize, keyword, category);
        return Result.success(result);
    }
}


