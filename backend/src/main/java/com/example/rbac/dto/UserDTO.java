package com.example.rbac.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private Boolean isActive;
    private Set<Long> roleIds;
    private Set<String> roleNames;
    private Set<String> roles;
    private Set<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginTime;
}
