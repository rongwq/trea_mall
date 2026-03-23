package com.example.mall.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.dto.role.RoleAssignPermissionDTO;
import com.example.mall.dto.role.RoleCreateDTO;
import com.example.mall.dto.role.RoleQueryDTO;
import com.example.mall.dto.role.RoleUpdateDTO;
import com.example.mall.service.RoleService;
import com.example.mall.vo.role.RoleSimpleVO;
import com.example.mall.vo.role.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询角色列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<RoleVO>> page(@Validated RoleQueryDTO queryDTO) {
        PageResult<RoleVO> result = roleService.pageRoles(queryDTO);
        return Result.success(result);
    }

    /**
     * 获取所有启用的角色（用于下拉选择）
     *
     * @return 角色列表
     */
    @GetMapping("/all")
    public Result<List<RoleSimpleVO>> listAll() {
        List<RoleSimpleVO> roles = roleService.listAllEnabledRoles();
        return Result.success(roles);
    }

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@PathVariable Long id) {
        RoleVO roleVO = roleService.getRoleById(id);
        return Result.success(roleVO);
    }

    /**
     * 创建角色
     *
     * @param createDTO 角色创建参数
     * @return 创建结果
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Validated RoleCreateDTO createDTO) {
        Long roleId = roleService.createRole(createDTO);
        return Result.success("创建成功", roleId);
    }

    /**
     * 更新角色
     *
     * @param id        角色ID
     * @param updateDTO 角色更新参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Validated RoleUpdateDTO updateDTO) {
        updateDTO.setId(id);
        roleService.updateRole(updateDTO);
        return Result.success("更新成功", null);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("删除成功", null);
    }

    /**
     * 为角色分配权限
     *
     * @param id                  角色ID
     * @param assignPermissionDTO 权限分配参数
     * @return 分配结果
     */
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                          @RequestBody @Validated RoleAssignPermissionDTO assignPermissionDTO) {
        assignPermissionDTO.setRoleId(id);
        roleService.assignPermissions(assignPermissionDTO);
        return Result.success("权限分配成功", null);
    }
}
