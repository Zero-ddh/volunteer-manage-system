package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ActivitySummary;

/**
 * 活动总结服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
public interface ActivitySummaryService {

    /**
     * 创建活动总结
     */
    boolean create(ActivitySummary summary);

    /**
     * 更新活动总结
     */
    boolean update(ActivitySummary summary);

    /**
     * 删除活动总结
     */
    boolean delete(Long id);

    /**
     * 根据ID获取活动总结
     */
    ActivitySummary getById(Long id);

    /**
     * 根据社群ID获取活动总结
     */
    ActivitySummary getByCommunityId(Long communityId);

    /**
     * 分页查询活动总结列表
     */
    Page<ActivitySummary> pageList(int pageNo, int pageSize, String keyword, Long communityId);

    /**
     * 分页查询活动总结列表（支持状态筛选）
     */
    Page<ActivitySummary> pageList(int pageNo, int pageSize, String keyword, Long communityId, Integer status);

    /**
     * 管理后台分页查询活动总结列表（支持状态筛选）
     */
    Page<ActivitySummary> adminPageList(int pageNo, int pageSize, String keyword, Long communityId, Integer status);

    /**
     * 根据发布人ID分页查询活动总结
     */
    Page<ActivitySummary> pageListByPublisher(Long publisherId, int pageNo, int pageSize);
}
