package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.UserMessage;

public interface MessageService {
    
    /**
     * 发送消息
     */
    boolean sendMessage(Long userId, Integer messageType, String title, String content, Long relatedId, String relatedType);
    
    /**
     * 获取用户消息列表
     */
    Page<UserMessage> getUserMessages(Long userId, Integer messageType, Integer isRead, int pageNo, int pageSize);
    
    /**
     * 标记消息为已读
     */
    boolean markAsRead(Long messageId, Long userId);
    
    /**
     * 获取未读消息数量
     */
    int getUnreadCount(Long userId);
    
    /**
     * 批量标记为已读
     */
    boolean markAllAsRead(Long userId, Integer messageType);
}
