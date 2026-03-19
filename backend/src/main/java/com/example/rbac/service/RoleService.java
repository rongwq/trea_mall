package com.example.rbac.service;

import com.example.rbac.dto.PageResult;
import com.example.rbac.dto.RoleDTO;

import java.util.List;
import java.util.Set;

public interface RoleService {
    
    RoleDTO createRole(RoleDTO roleDTO);
    
    RoleDTO updateRole(Long id, RoleDTO roleDTO);
    
    void deleteRole(Long id);
    
    RoleDTO getRoleById(Long id);
    
    PageResult<RoleDTO> getAllRoles(int page, int size, String keyword);
    
    List<RoleDTO> getAllRoles();
    
    void assignPermissions(Long roleId, Set<Long> permissionIds);
}
