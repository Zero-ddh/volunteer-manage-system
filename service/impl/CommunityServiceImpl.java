package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Community;
import com.volunteer.entity.CommunityMember;
import com.volunteer.entity.CommunityPost;
import com.volunteer.mapper.CommunityMapper;
import com.volunteer.mapper.CommunityMemberMapper;
import com.volunteer.mapper.CommunityPostMapper;
import com.volunteer.service.CommunityService;
import com.volunteer.service.CommunityMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 社群服务实现类
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Service
public class CommunityServiceImpl implements CommunityService {

    @Autowired
    private CommunityMapper communityMapper;

    @Autowired
    private CommunityMemberMapper memberMapper;

    @Autowired
    private CommunityPostMapper postMapper;

    @Autowired
    private CommunityMemberService communityMemberService;

    @Override
    public Page<Community> pageCommunities(int pageNo, int pageSize, String keyword, String category) {
        Page<Community> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Community::getName, keyword.trim());
        }
        
        // 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(Community::getCategory, category.trim());
        }
        
        wrapper.eq(Community::getStatus, 1)
               .orderByDesc(Community::getCreateTime);
        
        Page<Community> result = communityMapper.selectPage(page, wrapper);
        
        // 实时统计每个社群的成员数
        for (Community community : result.getRecords()) {
            Long memberCount = memberMapper.selectCount(new LambdaQueryWrapper<CommunityMember>()
                .eq(CommunityMember::getCommunityId, community.getId())
                .eq(CommunityMember::getStatus, 1)); // 只统计正常状态的成员
            community.setMemberCount(memberCount.intValue());
        }
        
        return result;
    }

    @Override
    public Community getCommunity(Long id) {
        Community community = communityMapper.selectById(id);
        if (community != null) {
            // 实时统计成员数
            Long memberCount = memberMapper.selectCount(new LambdaQueryWrapper<CommunityMember>()
                .eq(CommunityMember::getCommunityId, id)
                .eq(CommunityMember::getStatus, 1)); // 只统计正常状态的成员
            community.setMemberCount(memberCount.intValue());
        }
        return community;
    }

    @Override
    public Page<CommunityPost> pagePosts(Long communityId, int pageNo, int pageSize) {
        Page<CommunityPost> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getCommunityId, communityId)
                .eq(CommunityPost::getStatus, 1)
                .orderByDesc(CommunityPost::getCreateTime);
        
        return postMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean createCommunity(Community community) {
        // 设置默认值
        community.setStatus(1); // 正常状态
        community.setMemberCount(0);
        community.setPostCount(0);
        community.setCreatorId(1L); // 默认管理员创建
        
        return communityMapper.insert(community) > 0;
    }

    @Override
    public boolean updateCommunity(Community community) {
        return communityMapper.updateById(community) > 0;
    }

    @Override
    public boolean deleteCommunity(Long id) {
        // 检查社群是否存在
        Community community = communityMapper.selectById(id);
        if (community == null) {
            throw new RuntimeException("社群不存在");
        }
        
        // 检查社群是否有成员
        if (communityMemberService.hasMembers(id)) {
            throw new RuntimeException("该社群存在成员数据，无法删除");
        }
        
        return communityMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateCommunityStatus(Long id, Integer status) {
        Community community = new Community();
        community.setId(id);
        community.setStatus(status);
        return communityMapper.updateById(community) > 0;
    }
}
