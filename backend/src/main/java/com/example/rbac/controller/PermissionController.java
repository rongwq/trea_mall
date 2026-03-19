package com.example.rbac.controller;

import com.example.rbac.dto.ApiResponse;
import com.example.rbac.dto.PageResult;
import com.example.rbac.dto.PermissionDTO;
import com.example.rbac.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('permission:read') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<PermissionDTO>>> getAllPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.getAllPermissions(page, size, keyword)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('permission:read') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success(permissionService.getAllPermissions()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:read') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionDTO>> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.getPermissionById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('permission:create') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionDTO>> createPermission(@RequestBody PermissionDTO permissionDTO) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.createPermission(permissionDTO)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionDTO>> updatePermission(@PathVariable Long id, @RequestBody PermissionDTO permissionDTO) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.updatePermission(id, permissionDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
