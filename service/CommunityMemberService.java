package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Community;

public interface CommunityMemberService {
    boolean join(Long communityId, Long userId);
    boolean quit(Long communityId, Long userId);
    boolean isMember(Long communityId, Long userId);
    boolean hasMembers(Long communityId);
    
    /**
     * 获取用户加入的社群列表
     */
    Page<Community> getUserCommunities(Long userId, int pageNo, int pageSize, String keyword, String category);
    
    /**
     * 获取用户加入的社群列表（兼容旧版本）
     */
    Page<Community> getUserCommunities(Long userId, int pageNo, int pageSize);
}


