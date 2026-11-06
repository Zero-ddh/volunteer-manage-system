package com.volunteer.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.UserMessage;
import com.volunteer.mapper.UserMessageMapper;
import com.volunteer.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private UserMessageMapper userMessageMapper;

    @Override
    public boolean sendMessage(Long userId, Integer messageType, String title, String content, Long relatedId, String relatedType) {
        try {
            UserMessage message = new UserMessage();
            message.setUserId(userId);
            message.setMessageType(messageType);
            message.setTitle(title);
            message.setContent(content);
            message.setRelatedId(relatedId);
            message.setRelatedType(relatedType);
            message.setIsRead(0);
            message.setDeleted(0);
            
            return userMessageMapper.insert(message) > 0;
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return false;
        }
    }

    @Override
    public Page<UserMessage> getUserMessages(Long userId, Integer messageType, Integer isRead, int pageNo, int pageSize) {
        Page<UserMessage> page = new Page<>(pageNo, pageSize);
        return userMessageMapper.selectUserMessages(page, userId, messageType, isRead);
    }

    @Override
    public boolean markAsRead(Long messageId, Long userId) {
        try {
            UserMessage message = userMessageMapper.selectById(messageId);
            if (message != null && message.getUserId().equals(userId)) {
                message.setIsRead(1);
                message.setReadTime(LocalDateTime.now());
                return userMessageMapper.updateById(message) > 0;
            }
            return false;
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return false;
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        return userMessageMapper.selectUnreadCount(userId);
    }

    @Override
    public boolean markAllAsRead(Long userId, Integer messageType) {
        try {
            return userMessageMapper.markMessagesAsRead(userId, messageType) > 0;
        } catch (Exception e) {
            log.error("批量标记消息已读失败", e);
            return false;
        }
    }
}
