package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Community;
import com.volunteer.entity.CommunityPost;
import com.volunteer.service.CommunityService;
import com.volunteer.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/community")
public class CommunityController {

    @Resource
    private CommunityService communityService;

    @GetMapping("/list")
    public Result<Page<Community>> list(@RequestParam(defaultValue = "1") int pageNo,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category) {
        Page<Community> page = communityService.pageCommunities(pageNo, pageSize, keyword, category);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Community> detail(@PathVariable Long id) {
        Community community = communityService.getCommunity(id);
        return Result.success(community);
    }

    // 帖子功能已屏蔽 - 社群不需要帖子功能
    /*
    @GetMapping("/{id}/posts")
    public Result<Page<CommunityPost>> posts(@PathVariable Long id,
                                            @RequestParam(defaultValue = "1") int pageNo,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CommunityPost> page = communityService.pagePosts(id, pageNo, pageSize);
        return Result.success(page);
    }
    */

    /**
     * 根据社群ID获取活动总结
     * 注意：由于活动和社群之间没有直接关联，此接口返回null
     */
    @GetMapping("/{id}/summary")
    public Result<Object> getSummary(@PathVariable Long id) {
        // 由于活动和社群之间没有直接关联关系，返回null
        // 前端会处理null值，显示"暂无活动总结"
        return Result.success(null);
    }
}


