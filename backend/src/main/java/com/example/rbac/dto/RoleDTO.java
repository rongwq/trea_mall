package com.example.rbac.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RoleDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Set<Long> permissionIds;
    private Set<String> permissionCodes;
    private LocalDateTime createdAt;
}
