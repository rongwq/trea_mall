package com.example.rbac.config;

import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.User;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data initialization...");
        
        // Initialize permissions
        initPermissions();
        
        // Initialize roles
        initRoles();
        
        // Initialize admin user
        initAdminUser();
        
        log.info("Data initialization completed.");
    }

    private void initPermissions() {
        Map<String, String[]> permissionMap = Map.of(
            "user:create", new String[]{"创建用户", "创建新用户"},
            "user:read", new String[]{"查看用户", "查看用户列表和详情"},
            "user:update", new String[]{"更新用户", "更新用户信息"},
            "user:delete", new String[]{"删除用户", "删除用户"},
            "role:create", new String[]{"创建角色", "创建新角色"},
            "role:read", new String[]{"查看角色", "查看角色列表和详情"},
            "role:update", new String[]{"更新角色", "更新角色信息"},
            "role:delete", new String[]{"删除角色", "删除角色"},
            "permission:create", new String[]{"创建权限", "创建新权限"},
            "permission:read", new String[]{"查看权限", "查看权限列表和详情"},
            "permission:update", new String[]{"更新权限", "更新权限信息"},
            "permission:delete", new String[]{"删除权限", "删除权限"}
        );

        permissionMap.forEach((code, names) -> {
            if (!permissionRepository.existsByCode(code)) {
                Permission permission = new Permission();
                permission.setCode(code);
                permission.setName(names[0]);
                permission.setDescription(names[1]);
                permissionRepository.save(permission);
                log.info("Created permission: {}", code);
            }
        });
    }

    private void initRoles() {
        // Admin role - has all permissions
        Role adminRole = roleRepository.findByCode("ADMIN")
            .orElseGet(() -> {
                Role role = new Role();
                role.setCode("ADMIN");
                role.setName("管理员");
                role.setDescription("系统管理员，拥有所有权限");
                return role;
            });
        
        List<Permission> allPermissions = permissionRepository.findAll();
        adminRole.setPermissions(new HashSet<>(allPermissions));
        roleRepository.save(adminRole);
        log.info("Initialized ADMIN role with {} permissions", allPermissions.size());

        // User manager role - has user management permissions
        Role userManagerRole = roleRepository.findByCode("USER_MANAGER")
            .orElseGet(() -> {
                Role role = new Role();
                role.setCode("USER_MANAGER");
                role.setName("用户管理员");
                role.setDescription("管理用户");
                return role;
            });
        
        Set<Permission> userPermissions = allPermissions.stream()
            .filter(p -> p.getCode().startsWith("user:"))
            .collect(Collectors.toSet());
        userManagerRole.setPermissions(userPermissions);
        roleRepository.save(userManagerRole);
        log.info("Initialized USER_MANAGER role with {} permissions", userPermissions.size());

        // Read only role - has read permissions only
        Role readOnlyRole = roleRepository.findByCode("READ_ONLY")
            .orElseGet(() -> {
                Role role = new Role();
                role.setCode("READ_ONLY");
                role.setName("只读用户");
                role.setDescription("只能查看数据");
                return role;
            });
        
        Set<Permission> readPermissions = allPermissions.stream()
            .filter(p -> p.getCode().endsWith(":read"))
            .collect(Collectors.toSet());
        readOnlyRole.setPermissions(readPermissions);
        roleRepository.save(readOnlyRole);
        log.info("Initialized READ_ONLY role with {} permissions", readPermissions.size());
    }

    private void initAdminUser() {
        User admin = userRepository.findByUsername("admin")
            .orElseGet(() -> {
                User user = new User();
                user.setUsername("admin");
                user.setPassword(passwordEncoder.encode("admin123"));
                user.setEmail("admin@example.com");
                user.setRealName("系统管理员");
                user.setIsActive(true);
                return user;
            });
        
        Role adminRole = roleRepository.findByCode("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
        log.info("Initialized admin user with ADMIN role");
    }
}
