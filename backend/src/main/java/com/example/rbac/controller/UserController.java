package com.example.rbac.controller;

import com.example.rbac.dto.ApiResponse;
import com.example.rbac.dto.PageResult;
import com.example.rbac.dto.UserDTO;
import com.example.rbac.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(page, size, keyword)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(ApiResponse.success(userService.createUser(userDTO)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, userDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRoles(@PathVariable Long id, @RequestBody Map<String, Set<Long>> request) {
        userService.assignRoles(id, request.get("roleIds"));
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        userService.changePassword(id, request.get("oldPassword"), request.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success());
    }
}
