package com.example.rbac.controller;

import com.example.rbac.dto.ApiResponse;
import com.example.rbac.entity.User;
import com.example.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserRepository userRepository;

    @GetMapping("/public/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Service is running"));
    }

    @GetMapping("/auth-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> authStatus(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication != null) {
            result.put("name", authentication.getName());
            result.put("isAuthenticated", authentication.isAuthenticated());
            result.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            result.put("principal", authentication.getPrincipal().getClass().getName());
        } else {
            result.put("message", "No authentication");
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user-details")
    public ResponseEntity<ApiResponse<Map<String, Object>>> userDetails(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication != null) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElseThrow();
            result.put("username", user.getUsername());
            result.put("roles", user.getRoles().stream()
                    .map(role -> role.getCode())
                    .collect(Collectors.toList()));
            result.put("permissions", user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(perm -> perm.getCode())
                    .distinct()
                    .collect(Collectors.toList()));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
