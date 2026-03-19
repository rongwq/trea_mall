package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.Result;
import com.example.mall.entity.Role;
import com.example.mall.service.PermissionService;
import com.example.mall.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private PermissionService permissionService;
    
    @GetMapping
    public Result<Page<Role>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String name) {
        Page<Role> rolePage = new Page<>(page, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Role::getName, name);
        }
        wrapper.orderByDesc(Role::getCreateTime);
        roleService.page(rolePage, wrapper);
        return Result.success(rolePage);
    }
    
    @GetMapping("/all")
    public Result<List<Role>> listAll() {
        List<Role> roles = roleService.list(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 1)
                .orderByDesc(Role::getCreateTime));
        return Result.success(roles);
    }
    
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        if (role != null) {
            role.setPermissions(permissionService.getPermissionsByRoleId(id));
        }
        return Result.success(role);
    }
    
    @PostMapping
    public Result<Void> save(@RequestBody Role role) {
        role.setStatus(1);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleService.save(role);
        return Result.success();
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        role.setUpdateTime(LocalDateTime.now());
        roleService.updateById(role);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
}
