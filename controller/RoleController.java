package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.entity.SysRole;
import com.volunteer.entity.SysRoleMenu;
import com.volunteer.mapper.SysRoleMenuMapper;
import com.volunteer.service.SysRoleService;
import com.volunteer.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/role")
public class RoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(sysRoleService.list());
    }

    @PostMapping
    public Result<String> create(@RequestBody SysRole role) {
        boolean ok = sysRoleService.save(role);
        return ok ? Result.success("创建成功", "创建成功") : Result.error("创建失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody SysRole role) {
        boolean ok = sysRoleService.updateById(role);
        return ok ? Result.success("更新成功", "更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean ok = sysRoleService.removeById(id);
        return ok ? Result.success("删除成功", "删除成功") : Result.error("删除失败");
    }

    @GetMapping("/{roleId}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        List<SysRoleMenu> list = sysRoleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
        List<Long> menuIds = list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return Result.success(menuIds);
    }

    @PostMapping("/{roleId}/menus")
    public Result<String> bindMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        // 先清空
        sysRoleMenuMapper.delete(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
        // 重新绑定
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            sysRoleMenuMapper.insert(rm);
        }
        return Result.success("绑定成功", "绑定成功");
    }
}


