package com.example.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.Result;
import com.example.mall.dto.role.RoleCreateDTO;
import com.example.mall.dto.role.RolePermissionAssignDTO;
import com.example.mall.dto.role.RoleQueryDTO;
import com.example.mall.dto.role.RoleUpdateDTO;
import com.example.mall.service.IRoleService;
import com.example.mall.vo.role.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "系统角色管理接口")
public class RoleController {

    private final IRoleService roleService;

    @GetMapping
    @Operation(summary = "分页查询角色列表")
    @PreAuthorize("hasAuthority('role:view')")
    public Result<Page<RoleVO>> getRolePage(@Valid RoleQueryDTO queryDTO) {
        Page<RoleVO> page = roleService.getRolePage(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有启用的角色列表")
    @PreAuthorize("hasAuthority('role:view')")
    public Result<List<RoleVO>> getAllEnabledRoles() {
        List<RoleVO> roles = roleService.getAllEnabledRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    @PreAuthorize("hasAuthority('role:view')")
    public Result<RoleVO> getRoleDetail(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        RoleVO roleVO = roleService.getRoleDetail(id);
        return Result.success(roleVO);
    }

    @PostMapping
    @Operation(summary = "创建角色")
    @PreAuthorize("hasAuthority('role:add')")
    public Result<Void> createRole(@Valid @RequestBody RoleCreateDTO createDTO) {
        roleService.createRole(createDTO);
        return Result.success("创建角色成功", null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    @PreAuthorize("hasAuthority('role:edit')")
    public Result<Void> updateRole(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RoleUpdateDTO updateDTO) {
        roleService.updateRole(id, updateDTO);
        return Result.success("更新角色成功", null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('role:delete')")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("删除角色成功", null);
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配角色权限")
    @PreAuthorize("hasAuthority('role:edit')")
    public Result<Void> assignPermissions(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RolePermissionAssignDTO assignDTO) {
        roleService.assignPermissions(id, assignDTO);
        return Result.success("分配权限成功", null);
    }

    @GetMapping("/{id}/permission-ids")
    @Operation(summary = "获取角色的权限ID列表")
    @PreAuthorize("hasAuthority('role:view')")
    public Result<List<Long>> getRolePermissionIds(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        List<Long> permissionIds = roleService.getRolePermissionIds(id);
        return Result.success(permissionIds);
    }
}
