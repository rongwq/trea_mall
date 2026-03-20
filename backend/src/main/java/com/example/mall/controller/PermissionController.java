package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.Result;
import com.example.mall.entity.Permission;
import com.example.mall.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    @GetMapping
    public Result<Page<Permission>> list(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String name) {
        Page<Permission> permissionPage = new Page<>(page, size);
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Permission::getName, name);
        }
        wrapper.orderByAsc(Permission::getSort);
        permissionService.page(permissionPage, wrapper);
        return Result.success(permissionPage);
    }
    
    @GetMapping("/tree")
    public Result<List<Permission>> tree() {
        List<Permission> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }
    
    @GetMapping("/all")
    public Result<List<Permission>> listAll() {
        List<Permission> permissions = permissionService.list(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getStatus, 1)
                .orderByAsc(Permission::getSort));
        return Result.success(permissions);
    }
    
    @GetMapping("/{id}")
    public Result<Permission> getById(@PathVariable Long id) {
        Permission permission = permissionService.getById(id);
        return Result.success(permission);
    }
    
    @PostMapping
    public Result<Void> save(@RequestBody Permission permission) {
        permission.setStatus(1);
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        permissionService.save(permission);
        return Result.success();
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        permission.setUpdateTime(LocalDateTime.now());
        permissionService.updateById(permission);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.removeById(id);
        return Result.success();
    }
}
