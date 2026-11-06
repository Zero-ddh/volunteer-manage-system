package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Community;
import com.volunteer.entity.CommunityMember;
import com.volunteer.mapper.CommunityMapper;
import com.volunteer.mapper.CommunityMemberMapper;
import com.volunteer.service.CommunityMemberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommunityMemberServiceImpl implements CommunityMemberService {

    @Resource
    private CommunityMemberMapper memberMapper;
    
    @Resource
    private CommunityMapper communityMapper;

    @Override
    public boolean join(Long communityId, Long userId) {
        CommunityMember exist = memberMapper.selectOne(new LambdaQueryWrapper<CommunityMember>()
                .eq(CommunityMember::getCommunityId, communityId)
                .eq(CommunityMember::getUserId, userId)
                .last("limit 1"));
        if (exist != null) {
            if (exist.getStatus() == 1) return true;
            exist.setStatus(1);
            exist.setJoinTime(java.time.LocalDateTime.now());
            return memberMapper.updateById(exist) > 0;
        }
        CommunityMember m = new CommunityMember();
        m.setCommunityId(communityId);
        m.setUserId(userId);
        m.setStatus(1);
        m.setRole(1); // 普通成员
        m.setJoinTime(java.time.LocalDateTime.now());
        return memberMapper.insert(m) > 0;
    }

    @Override
    public boolean quit(Long communityId, Long userId) {
        CommunityMember exist = memberMapper.selectOne(new LambdaQueryWrapper<CommunityMember>()
                .eq(CommunityMember::getCommunityId, communityId)
                .eq(CommunityMember::getUserId, userId)
                .last("limit 1"));
        if (exist == null) return true;
        exist.setStatus(0);
        return memberMapper.updateById(exist) > 0;
    }

    @Override
    public boolean isMember(Long communityId, Long userId) {
        Long cnt = memberMapper.selectCount(new LambdaQueryWrapper<CommunityMember>()
                .eq(CommunityMember::getCommunityId, communityId)
                .eq(CommunityMember::getUserId, userId)
                .eq(CommunityMember::getStatus, 1));
        return cnt != null && cnt > 0;
    }

    @Override
    public boolean hasMembers(Long communityId) {
        Long cnt = memberMapper.selectCount(new LambdaQueryWrapper<CommunityMember>()
                .eq(CommunityMember::getCommunityId, communityId)
                .eq(CommunityMember::getStatus, 1));
        return cnt != null && cnt > 0;
    }
    
    @Override
    public Page<Community> getUserCommunities(Long userId, int pageNo, int pageSize, String keyword, String category) {
        // 先查询用户加入的社群ID列表
        List<CommunityMember> members = memberMapper.selectList(
            new LambdaQueryWrapper<CommunityMember>()
                .eq(CommunityMember::getUserId, userId)
                .eq(CommunityMember::getStatus, 1)
                .orderByDesc(CommunityMember::getJoinTime)
        );
        
        if (members.isEmpty()) {
            return new Page<>(pageNo, pageSize);
        }
        
        // 提取社群ID列表
        List<Long> communityIds = members.stream()
            .map(CommunityMember::getCommunityId)
            .collect(java.util.stream.Collectors.toList());
        
        // 分页查询社群信息
        Page<Community> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<Community>()
            .in(Community::getId, communityIds)
            .orderByDesc(Community::getCreateTime);
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Community::getName, keyword.trim());
        }
        
        // 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(Community::getCategory, category.trim());
        }
        
        Page<Community> result = communityMapper.selectPage(page, wrapper);
        
        // 为每个社群添加加入时间信息
        for (Community community : result.getRecords()) {
            // 查找对应的成员记录获取加入时间
            CommunityMember member = members.stream()
                .filter(m -> m.getCommunityId().equals(community.getId()))
                .findFirst()
                .orElse(null);
            
            if (member != null) {
                // 将加入时间添加到社群对象中（使用非数据库字段）
                community.setJoinTime(member.getJoinTime());
            }
        }
        
        return result;
    }
    
    @Override
    public Page<Community> getUserCommunities(Long userId, int pageNo, int pageSize) {
        return getUserCommunities(userId, pageNo, pageSize, null, null);
    }
}


