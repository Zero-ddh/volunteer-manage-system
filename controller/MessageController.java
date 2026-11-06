package com.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.UserMessage;
import com.volunteer.service.MessageService;
import com.volunteer.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取用户消息列表
     */
    @GetMapping("/list")
    public Result<Page<UserMessage>> getMessages(
            @RequestParam(required = false) Integer messageType,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        Page<UserMessage> messages = messageService.getUserMessages(userId, messageType, isRead, pageNo, pageSize);
        return Result.success(messages);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean result = messageService.markAsRead(id, userId);
        return result ? Result.success(true) : Result.error("操作失败");
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        int count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 批量标记为已读
     */
    @PutMapping("/mark-all-read")
    public Result<Boolean> markAllAsRead(
            @RequestParam(required = false) Integer messageType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean result = messageService.markAllAsRead(userId, messageType);
        return result ? Result.success(true) : Result.error("操作失败");
    }
}
