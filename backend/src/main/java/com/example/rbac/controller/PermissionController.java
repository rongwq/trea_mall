package com.example.rbac.controller;

import com.example.rbac.common.Result;
import com.example.rbac.entity.Permission;
import com.example.rbac.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public Result<List<Permission>> findAll() {
        return Result.success(permissionService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Permission> findById(@PathVariable Long id) {
        return permissionService.findById(id)
                .map(Result::success)
                .orElse(Result.error("权限不存在"));
    }

    @PostMapping
    public Result<Permission> save(@RequestBody Permission permission) {
        if (permissionService.existsByName(permission.getName())) {
            return Result.error("权限名已存在");
        }
        return Result.success(permissionService.save(permission));
    }

    @PutMapping("/{id}")
    public Result<Permission> update(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        return Result.success(permissionService.update(permission));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        permissionService.deleteById(id);
        return Result.success();
    }
}