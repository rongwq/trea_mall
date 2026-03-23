package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.role.RoleAssignPermissionDTO;
import com.example.mall.dto.role.RoleCreateDTO;
import com.example.mall.dto.role.RoleQueryDTO;
import com.example.mall.dto.role.RoleUpdateDTO;
import com.example.mall.entity.Role;
import com.example.mall.common.PageResult;
import com.example.mall.vo.role.RoleSimpleVO;
import com.example.mall.vo.role.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<RoleVO> pageRoles(RoleQueryDTO queryDTO);

    /**
     * 获取所有启用的角色
     *
     * @return 角色列表
     */
    List<RoleSimpleVO> listAllEnabledRoles();

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    RoleVO getRoleById(Long id);

    /**
     * 根据用户ID获取角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 创建角色
     *
     * @param createDTO 角色创建参数
     * @return 角色ID
     */
    Long createRole(RoleCreateDTO createDTO);

    /**
     * 更新角色
     *
     * @param updateDTO 角色更新参数
     */
    void updateRole(RoleUpdateDTO updateDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 为角色分配权限
     *
     * @param assignPermissionDTO 权限分配参数
     */
    void assignPermissions(RoleAssignPermissionDTO assignPermissionDTO);
}
