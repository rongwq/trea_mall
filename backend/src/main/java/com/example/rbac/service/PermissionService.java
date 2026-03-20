package com.example.rbac.service;

import com.example.rbac.dto.PageResult;
import com.example.rbac.dto.PermissionDTO;

import java.util.List;

public interface PermissionService {
    
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    
    PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO);
    
    void deletePermission(Long id);
    
    PermissionDTO getPermissionById(Long id);
    
    PageResult<PermissionDTO> getAllPermissions(int page, int size, String keyword);
    
    List<PermissionDTO> getAllPermissions();
}
