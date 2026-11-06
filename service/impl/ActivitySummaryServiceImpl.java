package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.ActivitySummary;
import com.volunteer.entity.Community;
import com.volunteer.mapper.ActivitySummaryMapper;
import com.volunteer.mapper.CommunityMapper;
import com.volunteer.service.ActivitySummaryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动总结服务实现类
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
@Service
public class ActivitySummaryServiceImpl implements ActivitySummaryService {

    @Resource
    private ActivitySummaryMapper summaryMapper;
    
    @Resource
    private CommunityMapper communityMapper;

    @Override
    public boolean create(ActivitySummary summary) {
        return summaryMapper.insert(summary) > 0;
    }

    @Override
    public boolean update(ActivitySummary summary) {
        return summaryMapper.updateById(summary) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return summaryMapper.deleteById(id) > 0;
    }

    @Override
    public ActivitySummary getById(Long id) {
        return summaryMapper.selectById(id);
    }

    @Override
    public ActivitySummary getByCommunityId(Long communityId) {
        LambdaQueryWrapper<ActivitySummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySummary::getCommunityId, communityId)
                   .orderByDesc(ActivitySummary::getPublishTime)
                   .last("LIMIT 1");
        return summaryMapper.selectOne(queryWrapper);
    }

    @Override
    public Page<ActivitySummary> pageList(int pageNo, int pageSize, String keyword, Long communityId) {
        return pageList(pageNo, pageSize, keyword, communityId, null);
    }

    @Override
    public Page<ActivitySummary> pageList(int pageNo, int pageSize, String keyword, Long communityId, Integer status) {
        Page<ActivitySummary> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ActivitySummary> queryWrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(w -> w.like(ActivitySummary::getTitle, keyword)
                                 .or()
                                 .like(ActivitySummary::getContent, keyword));
        }
        
        if (communityId != null) {
            queryWrapper.eq(ActivitySummary::getCommunityId, communityId);
        }
        
        if (status != null) {
            queryWrapper.eq(ActivitySummary::getStatus, status);
        }
        
        queryWrapper.orderByDesc(ActivitySummary::getPublishTime);
        
        Page<ActivitySummary> result = summaryMapper.selectPage(page, queryWrapper);
        
        // 填充社群名称
        fillCommunityNames(result.getRecords());
        
        return result;
    }

    @Override
    public Page<ActivitySummary> adminPageList(int pageNo, int pageSize, String keyword, Long communityId, Integer status) {
        Page<ActivitySummary> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ActivitySummary> queryWrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(w -> w.like(ActivitySummary::getTitle, keyword)
                                 .or()
                                 .like(ActivitySummary::getContent, keyword)
                                 .or()
                                 .like(ActivitySummary::getPublisherName, keyword));
        }
        
        if (communityId != null) {
            queryWrapper.eq(ActivitySummary::getCommunityId, communityId);
        }
        
        if (status != null) {
            queryWrapper.eq(ActivitySummary::getStatus, status);
        }
        
        queryWrapper.orderByDesc(ActivitySummary::getPublishTime);
        
        Page<ActivitySummary> result = summaryMapper.selectPage(page, queryWrapper);
        
        // 填充社群名称
        fillCommunityNames(result.getRecords());
        
        return result;
    }

    /**
     * 填充社群名称
     */
    private void fillCommunityNames(List<ActivitySummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return;
        }
        
        // 获取所有社群ID
        List<Long> communityIds = summaries.stream()
                .map(ActivitySummary::getCommunityId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (communityIds.isEmpty()) {
            return;
        }
        
        // 批量查询社群信息
        LambdaQueryWrapper<Community> communityWrapper = new LambdaQueryWrapper<>();
        communityWrapper.in(Community::getId, communityIds);
        List<Community> communities = communityMapper.selectList(communityWrapper);
        
        // 构建社群ID到名称的映射
        Map<Long, String> communityNameMap = communities.stream()
                .collect(Collectors.toMap(Community::getId, Community::getName));
        
        // 填充社群名称
        summaries.forEach(summary -> {
            if (summary.getCommunityId() != null) {
                summary.setCommunityName(communityNameMap.get(summary.getCommunityId()));
            }
        });
    }

    @Override
    public Page<ActivitySummary> pageListByPublisher(Long publisherId, int pageNo, int pageSize) {
        Page<ActivitySummary> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ActivitySummary> queryWrapper = new LambdaQueryWrapper<>();
        
        if (publisherId != null) {
            queryWrapper.eq(ActivitySummary::getPublisherId, publisherId);
        }
        
        queryWrapper.orderByDesc(ActivitySummary::getPublishTime);
        
        return summaryMapper.selectPage(page, queryWrapper);
    }
}
