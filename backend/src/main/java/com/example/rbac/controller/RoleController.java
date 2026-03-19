package com.example.rbac.controller;

import com.example.rbac.common.Result;
import com.example.rbac.entity.Role;
import com.example.rbac.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping
    public Result<List<Role>> findAll() {
        return Result.success(roleService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Role> findById(@PathVariable Long id) {
        return roleService.findById(id)
                .map(Result::success)
                .orElse(Result.error("角色不存在"));
    }

    @PostMapping
    public Result<Role> save(@RequestBody Role role) {
        if (roleService.existsByName(role.getName())) {
            return Result.error("角色名已存在");
        }
        return Result.success(roleService.save(role));
    }

    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return Result.success(roleService.update(role));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        roleService.deleteById(id);
        return Result.success();
    }
}