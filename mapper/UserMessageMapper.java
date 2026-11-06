package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {
    
    /**
     * 获取用户消息列表（分页）
     */
    Page<UserMessage> selectUserMessages(Page<UserMessage> page, 
                                       @Param("userId") Long userId,
                                       @Param("messageType") Integer messageType,
                                       @Param("isRead") Integer isRead);
    
    /**
     * 获取用户未读消息数量
     */
    int selectUnreadCount(@Param("userId") Long userId);
    
    /**
     * 批量标记消息为已读
     */
    int markMessagesAsRead(@Param("userId") Long userId, 
                          @Param("messageType") Integer messageType);
}
