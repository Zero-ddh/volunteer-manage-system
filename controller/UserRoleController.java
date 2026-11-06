package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.entity.SysUserRole;
import com.volunteer.mapper.SysUserRoleMapper;
import com.volunteer.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/user")
public class UserRoleController {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @GetMapping("/{userId}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        List<SysUserRole> list = sysUserRoleMapper.selectList(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        List<Long> roleIds = list.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return Result.success(roleIds);
    }

    @PostMapping("/{userId}/roles")
    public Result<String> setUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        // 清空旧关系
        sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        // 新增关系
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            sysUserRoleMapper.insert(ur);
        }
        return Result.success("绑定成功", "绑定成功");
    }
}


