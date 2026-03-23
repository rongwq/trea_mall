package com.example.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.role.RoleCreateDTO;
import com.example.mall.dto.role.RolePermissionAssignDTO;
import com.example.mall.dto.role.RoleQueryDTO;
import com.example.mall.dto.role.RoleUpdateDTO;
import com.example.mall.entity.Role;
import com.example.mall.vo.role.RoleVO;
import java.util.List;

public interface IRoleService extends IService<Role> {

    Page<RoleVO> getRolePage(RoleQueryDTO queryDTO);

    List<RoleVO> getAllEnabledRoles();

    RoleVO getRoleDetail(Long id);

    void createRole(RoleCreateDTO createDTO);

    void updateRole(Long id, RoleUpdateDTO updateDTO);

    void deleteRole(Long id);

    void assignPermissions(Long roleId, RolePermissionAssignDTO assignDTO);

    List<Long> getRolePermissionIds(Long roleId);

    List<RoleVO> getRolesByUserId(Long userId);

    boolean isRoleCodeExists(String code);

    boolean isRoleNameExists(String name);
}
