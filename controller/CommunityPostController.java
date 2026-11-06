package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.CommunityComment;
import com.volunteer.entity.CommunityPost;
import com.volunteer.mapper.CommunityCommentMapper;
import com.volunteer.mapper.CommunityPostMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/community/post")
public class CommunityPostController {

    @Resource
    private CommunityPostMapper postMapper;

    @Resource
    private CommunityCommentMapper commentMapper;

    @PostMapping("/create")
    public boolean create(@RequestBody CommunityPost post, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        post.setAuthorId(userId);
        post.setStatus(1);
        return postMapper.insert(post) > 0;
    }

    @GetMapping("/{id}")
    public CommunityPost detail(@PathVariable Long id) {
        return postMapper.selectById(id);
    }

    @GetMapping("/{id}/comments")
    public Page<CommunityComment> comments(@PathVariable Long id,
                                           @RequestParam(defaultValue = "1") int pageNo,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        Page<CommunityComment> page = new Page<>(pageNo, pageSize);
        return commentMapper.selectPage(page, new LambdaQueryWrapper<CommunityComment>()
                .eq(CommunityComment::getPostId, id)
                .eq(CommunityComment::getStatus, 1)
                .orderByAsc(CommunityComment::getCreatedTime));
    }

    @PostMapping("/{id}/comment")
    public boolean comment(@PathVariable Long id, @RequestBody CommunityComment comment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        comment.setPostId(id);
        comment.setUserId(userId);
        comment.setStatus(1);
        return commentMapper.insert(comment) > 0;
    }
}


