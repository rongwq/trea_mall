package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.dto.AssignPermissionDTO;
import com.example.mall.dto.RoleCreateDTO;
import com.example.mall.dto.RoleQueryDTO;
import com.example.mall.dto.RoleUpdateDTO;
import com.example.mall.service.RoleService;
import com.example.mall.vo.PageResultVO;
import com.example.mall.vo.RoleDetailVO;
import com.example.mall.vo.RoleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public Result<PageResultVO<RoleVO>> list(@Valid RoleQueryDTO queryDTO) {
        PageResultVO<RoleVO> result = roleService.getRolePage(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<RoleVO>> listAll() {
        List<RoleVO> roles = roleService.getAllEnabledRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    public Result<RoleDetailVO> getById(@PathVariable @NotNull(message = "角色ID不能为空") Long id) {
        RoleDetailVO role = roleService.getRoleDetailById(id);
        return Result.success(role);
    }

    @PostMapping
    public ResponseEntity<Result<Long>> create(@Valid @RequestBody RoleCreateDTO createDTO) {
        Long roleId = roleService.createRole(createDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/roles/" + roleId))
                .body(Result.success(roleId));
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable @NotNull(message = "角色ID不能为空") Long id,
            @Valid @RequestBody RoleUpdateDTO updateDTO) {
        updateDTO.setId(id);
        roleService.updateRole(updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @NotNull(message = "角色ID不能为空") Long id) {
        roleService.deleteRole(id);
    }

    @DeleteMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void batchDelete(@RequestBody @NotEmpty(message = "角色ID列表不能为空") List<Long> ids) {
        roleService.batchDeleteRoles(ids);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable @NotNull(message = "角色ID不能为空") Long id,
            @RequestParam @NotNull(message = "状态不能为空") Integer status) {
        roleService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(
            @PathVariable @NotNull(message = "角色ID不能为空") Long id,
            @Valid @RequestBody AssignPermissionDTO assignDTO) {
        roleService.assignPermissions(id, assignDTO);
        return Result.success();
    }
}
