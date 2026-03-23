package com.example.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.AssignPermissionDTO;
import com.example.mall.dto.RoleCreateDTO;
import com.example.mall.dto.RoleQueryDTO;
import com.example.mall.dto.RoleUpdateDTO;
import com.example.mall.entity.Role;
import com.example.mall.vo.PageResultVO;
import com.example.mall.vo.RoleDetailVO;
import com.example.mall.vo.RoleVO;

import java.util.List;

public interface RoleService extends IService<Role> {
    PageResultVO<RoleVO> getRolePage(RoleQueryDTO queryDTO);
    
    List<RoleVO> getAllEnabledRoles();
    
    RoleDetailVO getRoleDetailById(Long id);
    
    Long createRole(RoleCreateDTO createDTO);
    
    void updateRole(RoleUpdateDTO updateDTO);
    
    void deleteRole(Long id);
    
    void batchDeleteRoles(List<Long> ids);
    
    void assignPermissions(Long roleId, AssignPermissionDTO assignDTO);
    
    void updateStatus(Long id, Integer status);
    
    List<Role> getRolesByUserId(Long userId);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);
    
    boolean isRoleInUse(Long roleId);
}
