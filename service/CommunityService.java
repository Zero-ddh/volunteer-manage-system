package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Community;
import com.volunteer.entity.CommunityPost;

/**
 * 社群服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
public interface CommunityService {
    
    /**
     * 分页查询社群列表
     */
    Page<Community> pageCommunities(int pageNo, int pageSize, String keyword, String category);
    
    /**
     * 获取社群详情
     */
    Community getCommunity(Long id);
    
    /**
     * 分页查询社群帖子
     */
    Page<CommunityPost> pagePosts(Long communityId, int pageNo, int pageSize);
    
    /**
     * 创建社群
     */
    boolean createCommunity(Community community);
    
    /**
     * 更新社群
     */
    boolean updateCommunity(Community community);
    
    /**
     * 删除社群
     */
    boolean deleteCommunity(Long id);
    
    /**
     * 更新社群状态
     */
    boolean updateCommunityStatus(Long id, Integer status);
}
