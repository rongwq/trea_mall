package com.example.rbac.service.impl;

import com.example.rbac.dto.PageResult;
import com.example.rbac.dto.PermissionDTO;
import com.example.rbac.entity.Permission;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        if (permissionRepository.existsByCode(permissionDTO.getCode())) {
            throw new RuntimeException("权限编码已存在");
        }

        Permission permission = new Permission();
        permission.setCode(permissionDTO.getCode());
        permission.setName(permissionDTO.getName());
        permission.setDescription(permissionDTO.getDescription());

        Permission savedPermission = permissionRepository.save(permission);
        return convertToDTO(savedPermission);
    }

    @Override
    @Transactional
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));

        permission.setName(permissionDTO.getName());
        permission.setDescription(permissionDTO.getDescription());

        Permission savedPermission = permissionRepository.save(permission);
        return convertToDTO(savedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        return convertToDTO(permission);
    }

    @Override
    public PageResult<PermissionDTO> getAllPermissions(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Permission> permissionPage = permissionRepository.searchPermissions(keyword, pageable);
        Page<PermissionDTO> dtoPage = permissionPage.map(this::convertToDTO);
        return PageResult.from(dtoPage);
    }

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PermissionDTO convertToDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setCreatedAt(permission.getCreatedAt());
        return dto;
    }
}
