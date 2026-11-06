package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.CommunityMemberDetailDTO;
import com.volunteer.entity.Community;
import com.volunteer.entity.CommunityMember;
import com.volunteer.entity.VolunteerUser;
import com.volunteer.mapper.CommunityMapper;
import com.volunteer.mapper.CommunityMemberMapper;
import com.volunteer.mapper.VolunteerUserMapper;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理后台社群成员管理控制器
 * 
 * @author volunteer-team
 * @since 2025-01-25
 */
@Slf4j
@RestController
@RequestMapping("/admin/community-members")
public class AdminCommunityMemberController {

    @Autowired
    private CommunityMemberMapper memberMapper;
    
    @Autowired
    private VolunteerUserMapper userMapper;
    
    @Autowired
    private CommunityMapper communityMapper;
    
    /**
     * 测试数据库连接和表结构
     */
    @GetMapping("/test")
    public Result<String> test() {
        try {
            // 测试查询所有成员
            long count = memberMapper.selectCount(null);
            log.info("数据库连接正常，community_member表存在，总记录数：{}", count);
            return Result.success("数据库连接正常，表存在，总记录数：" + count);
        } catch (Exception e) {
            log.error("数据库测试失败：{}", e.getMessage(), e);
            return Result.error("数据库测试失败：" + e.getMessage());
        }
    }

    /**
     * 更新成员状态（屏蔽/解除屏蔽）
     */
    @PutMapping("/{id}/status")
    public Result<String> updateMemberStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            if (status == null) {
                return Result.error("状态参数不能为空");
            }
            
            CommunityMember member = memberMapper.selectById(id);
            if (member == null) {
                return Result.error("成员不存在");
            }
            
            member.setStatus(status);
            int result = memberMapper.updateById(member);
            
            if (result > 0) {
                String statusText = status == 1 ? "正常" : status == 2 ? "禁言" : "已退出";
                return Result.success("状态更新成功，成员状态已设为：" + statusText);
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新成员状态失败：{}", e.getMessage(), e);
            return Result.error("更新成员状态失败：" + e.getMessage());
        }
    }

    /**
     * 更新成员角色
     */
    @PutMapping("/{id}/role")
    public Result<String> updateMemberRole(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer role = (Integer) params.get("role");
            if (role == null) {
                return Result.error("角色参数不能为空");
            }
            
            CommunityMember member = memberMapper.selectById(id);
            if (member == null) {
                return Result.error("成员不存在");
            }
            
            member.setRole(role);
            int result = memberMapper.updateById(member);
            
            if (result > 0) {
                String roleText = role == 1 ? "普通成员" : role == 2 ? "管理员" : "群主";
                return Result.success("角色更新成功，成员角色已设为：" + roleText);
            } else {
                return Result.error("角色更新失败");
            }
        } catch (Exception e) {
            log.error("更新成员角色失败：{}", e.getMessage(), e);
            return Result.error("更新成员角色失败：" + e.getMessage());
        }
    }

    /**
     * 移除成员
     */
    @DeleteMapping("/{id}/remove")
    public Result<String> removeMember(@PathVariable Long id) {
        try {
            CommunityMember member = memberMapper.selectById(id);
            if (member == null) {
                return Result.error("成员不存在");
            }
            
            // 设置状态为已退出
            member.setStatus(3);
            int result = memberMapper.updateById(member);
            
            if (result > 0) {
                return Result.success("移除成功");
            } else {
                return Result.error("移除失败");
            }
        } catch (Exception e) {
            log.error("移除成员失败：{}", e.getMessage(), e);
            return Result.error("移除成员失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询成员列表
     */
    @PostMapping
    public Result<Page<CommunityMemberDetailDTO>> list(@RequestBody Map<String, Object> params) {
        try {
            log.info("收到查询成员列表请求，参数：{}", params);
            
            int pageNo = (Integer) params.getOrDefault("pageNo", 1);
            int pageSize = (Integer) params.getOrDefault("pageSize", 10);
            
            // 安全处理communityId参数
            Long communityId = null;
            Object communityIdObj = params.get("communityId");
            if (communityIdObj != null) {
                if (communityIdObj instanceof Integer) {
                    communityId = ((Integer) communityIdObj).longValue();
                } else if (communityIdObj instanceof Long) {
                    communityId = (Long) communityIdObj;
                } else if (communityIdObj instanceof String) {
                    try {
                        communityId = Long.parseLong((String) communityIdObj);
                    } catch (NumberFormatException e) {
                        log.warn("无效的communityId参数: {}", communityIdObj);
                    }
                }
            }
            
            log.info("解析后的参数 - pageNo: {}, pageSize: {}, communityId: {}", pageNo, pageSize, communityId);
            
            // 处理角色和状态筛选参数
            Integer role = (Integer) params.get("role");
            Integer status = (Integer) params.get("status");
            String keyword = (String) params.get("keyword");
            
            // 查询社群成员基本信息
            Page<CommunityMember> page = new Page<>(pageNo, pageSize);
            LambdaQueryWrapper<CommunityMember> wrapper = new LambdaQueryWrapper<>();

            if (communityId != null) {
                wrapper.eq(CommunityMember::getCommunityId, communityId);
            }
            
            if (role != null) {
                wrapper.eq(CommunityMember::getRole, role);
            }
            
            if (status != null) {
                wrapper.eq(CommunityMember::getStatus, status);
            }
            // 当status为null时，不添加状态筛选条件，显示所有状态的成员
            
            wrapper.orderByDesc(CommunityMember::getJoinTime);

            log.info("执行查询，wrapper: {}", wrapper);
            Page<CommunityMember> memberPage = memberMapper.selectPage(page, wrapper);
            log.info("查询结果：总数={}, 当前页数据={}", memberPage.getTotal(), memberPage.getRecords().size());
            
            // 如果有关键词搜索，需要先筛选用户
            List<Long> userIds = null;
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 根据关键词查询匹配的用户ID
                LambdaQueryWrapper<VolunteerUser> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.and(w -> w.like(VolunteerUser::getUsername, keyword)
                                    .or()
                                    .like(VolunteerUser::getPhone, keyword));
                
                List<VolunteerUser> matchedUsers = userMapper.selectList(userWrapper);
                userIds = matchedUsers.stream()
                    .map(VolunteerUser::getId)
                    .collect(java.util.stream.Collectors.toList());
                
                if (userIds.isEmpty()) {
                    // 没有匹配的用户，返回空结果
                    Page<CommunityMemberDetailDTO> emptyPage = new Page<>(pageNo, pageSize);
                    emptyPage.setTotal(0);
                    emptyPage.setRecords(new ArrayList<>());
                    return Result.success("查询成功", emptyPage);
                }
                
                // 重新查询社群成员，添加用户ID筛选
                wrapper.in(CommunityMember::getUserId, userIds);
                memberPage = memberMapper.selectPage(page, wrapper);
            }
            
            // 转换为详细DTO
            List<CommunityMemberDetailDTO> detailList = new ArrayList<>();
            for (CommunityMember member : memberPage.getRecords()) {
                CommunityMemberDetailDTO dto = new CommunityMemberDetailDTO();
                dto.setId(member.getId());
                dto.setCommunityId(member.getCommunityId());
                dto.setUserId(member.getUserId());
                dto.setRole(member.getRole());
                dto.setJoinTime(member.getJoinTime());
                dto.setStatus(member.getStatus());
                dto.setLastActiveTime(member.getLastActiveTime());
                
                // 查询用户信息
                VolunteerUser user = userMapper.selectById(member.getUserId());
                if (user != null) {
                    dto.setUsername(user.getUsername());
                    dto.setPhone(user.getPhone());
                }
                
                // 查询社群信息
                Community community = communityMapper.selectById(member.getCommunityId());
                if (community != null) {
                    dto.setCommunityName(community.getName());
                }
                
                detailList.add(dto);
            }
            
            // 创建新的分页结果
            Page<CommunityMemberDetailDTO> resultPage = new Page<>(pageNo, pageSize);
            resultPage.setTotal(memberPage.getTotal());
            resultPage.setRecords(detailList);
            
            return Result.success("查询成功", resultPage);
        } catch (Exception e) {
            log.error("查询成员列表失败：{}", e.getMessage(), e);
            return Result.error("查询成员列表失败：" + e.getMessage());
        }
    }
}
