package com.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.entity.SysMenu;
import com.volunteer.entity.SysRoleMenu;
import com.volunteer.entity.SysUserRole;
import com.volunteer.mapper.SysRoleMenuMapper;
import com.volunteer.mapper.SysUserRoleMapper;
import com.volunteer.service.SysMenuService;
import com.volunteer.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/menu")
public class MenuController {

    @Autowired
    private SysMenuService sysMenuService;
    
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @GetMapping("/list")
    public Result<List<SysMenu>> list(@RequestParam(value = "status", required = false) Integer status) {
        QueryWrapper<SysMenu> qw = new QueryWrapper<>();
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByAsc("sort_order");
        return Result.success(sysMenuService.list(qw));
    }

    @PostMapping
    public Result<String> create(@RequestBody SysMenu menu) {
        try {
            boolean ok = sysMenuService.save(menu);
            return ok ? Result.success("创建成功", "创建成功") : Result.error("创建失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PutMapping
    public Result<String> update(@RequestBody SysMenu menu) {
        try {
            // 清除children字段，避免更新时出现问题
            menu.setChildren(null);
            boolean ok = sysMenuService.updateById(menu);
            return ok ? Result.success("更新成功", "更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            boolean ok = sysMenuService.removeById(id);
            return ok ? Result.success("删除成功", "删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户的菜单权限
     */
    @GetMapping("/user-menus")
    public Result<List<SysMenu>> getUserMenus(HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取用户角色
            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new QueryWrapper<SysUserRole>().eq("user_id", userId)
            );
            
            if (userRoles.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            
            // 获取角色对应的菜单ID
            List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
                
            List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new QueryWrapper<SysRoleMenu>().in("role_id", roleIds)
            );
            
            if (roleMenus.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            
            // 获取菜单详情
            List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
                
            List<SysMenu> menus = sysMenuService.list(
                new QueryWrapper<SysMenu>()
                    .in("id", menuIds)
                    .eq("status", 1)
                    .orderByAsc("sort_order")
            );
            
            // 构建菜单树
            List<SysMenu> menuTree = buildMenuTree(menus);
            
            return Result.success(menuTree);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取菜单失败：" + e.getMessage());
        }
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        Map<Long, SysMenu> menuMap = menus.stream()
            .collect(Collectors.toMap(SysMenu::getId, menu -> menu));
        
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                roots.add(menu);
            } else {
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        return roots;
    }
}


