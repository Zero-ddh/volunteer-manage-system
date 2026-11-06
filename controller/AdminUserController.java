package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.SysUserRole;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.SysUserRoleMapper;
import com.volunteer.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台用户管理控制器
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 分页查询管理员用户列表
     */
    @GetMapping
    public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("username", keyword)
                .or()
                .like("real_name", keyword)
                .or()
                .like("email", keyword)
            );
        }
        
        // 状态筛选
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("created_time");
        
        Page<SysUser> result = sysUserMapper.selectPage(page, queryWrapper);
        
        // 为每个用户添加角色信息
        for (SysUser user : result.getRecords()) {
            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new QueryWrapper<SysUserRole>().eq("user_id", user.getId())
            );
            List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
            user.setRoleIds(roleIds);
        }
        
        return Result.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 获取用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
            new QueryWrapper<SysUserRole>().eq("user_id", id)
        );
        List<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());
        user.setRoleIds(roleIds);
        
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<String> create(@RequestBody SysUser user) {
        try {
            // 检查用户名是否已存在
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user.getUsername());
            SysUser existUser = sysUserMapper.selectOne(queryWrapper);
            if (existUser != null) {
                return Result.error("用户名已存在");
            }
            
            // 设置默认状态
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            
            boolean success = sysUserMapper.insert(user) > 0;
            if (success) {
                return Result.success("创建成功", "创建成功");
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建用户失败：{}", e.getMessage());
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping
    public Result<String> update(@RequestBody SysUser user) {
        try {
            if (user.getId() == null) {
                return Result.error("用户ID不能为空");
            }
            
            boolean success = sysUserMapper.updateById(user) > 0;
            if (success) {
                return Result.success("更新成功", "更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败：{}", e.getMessage());
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            // 检查是否有用户角色关联
            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new QueryWrapper<SysUserRole>().eq("user_id", id)
            );
            if (!userRoles.isEmpty()) {
                return Result.error("该用户已分配角色，无法删除");
            }
            
            boolean success = sysUserMapper.deleteById(id) > 0;
            if (success) {
                return Result.success("删除成功", "删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败：{}", e.getMessage());
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            SysUser user = new SysUser();
            user.setId(id);
            user.setStatus(status);
            
            boolean success = sysUserMapper.updateById(user) > 0;
            if (success) {
                return Result.success("状态更新成功", "状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败：{}", e.getMessage());
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestParam String password) {
        try {
            SysUser user = new SysUser();
            user.setId(id);
            user.setPassword(password); // 实际项目中应该加密
            
            boolean success = sysUserMapper.updateById(user) > 0;
            if (success) {
                return Result.success("密码重置成功", "密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            log.error("重置用户密码失败：{}", e.getMessage());
            return Result.error("密码重置失败：" + e.getMessage());
        }
    }

    /**
     * 为用户分配角色
     */
    @PutMapping("/{id}/roles")
    public Result<String> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        try {
            // 清空旧关系
            sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().eq("user_id", id));
            
            // 新增关系
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(id);
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
            
            return Result.success("角色分配成功", "角色分配成功");
        } catch (Exception e) {
            log.error("分配用户角色失败：{}", e.getMessage());
            return Result.error("角色分配失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总用户数
            Long totalUsers = sysUserMapper.selectCount(null);
            stats.put("totalUsers", totalUsers);
            
            // 启用用户数
            Long activeUsers = sysUserMapper.selectCount(
                new QueryWrapper<SysUser>().eq("status", 1)
            );
            stats.put("activeUsers", activeUsers);
            
            // 禁用用户数
            Long inactiveUsers = sysUserMapper.selectCount(
                new QueryWrapper<SysUser>().eq("status", 0)
            );
            stats.put("inactiveUsers", inactiveUsers);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计失败：{}", e.getMessage());
            return Result.error("获取统计信息失败：" + e.getMessage());
        }
    }
}
