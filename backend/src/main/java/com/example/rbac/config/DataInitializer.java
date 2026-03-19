package com.example.rbac.config;

import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.User;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

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
        // Initialize permissions
        if (permissionRepository.count() == 0) {
            initPermissions();
        }

        // Initialize roles
        if (roleRepository.count() == 0) {
            initRoles();
        }

        // Initialize admin user
        if (userRepository.count() == 0) {
            initAdminUser();
        }
    }

    private void initPermissions() {
        // User permissions
        createPermission("user:create", "创建用户", "创建新用户");
        createPermission("user:read", "查看用户", "查看用户列表和详情");
        createPermission("user:update", "更新用户", "更新用户信息");
        createPermission("user:delete", "删除用户", "删除用户");

        // Role permissions
        createPermission("role:create", "创建角色", "创建新角色");
        createPermission("role:read", "查看角色", "查看角色列表和详情");
        createPermission("role:update", "更新角色", "更新角色信息");
        createPermission("role:delete", "删除角色", "删除角色");

        // Permission permissions
        createPermission("permission:create", "创建权限", "创建新权限");
        createPermission("permission:read", "查看权限", "查看权限列表和详情");
        createPermission("permission:update", "更新权限", "更新权限信息");
        createPermission("permission:delete", "删除权限", "删除权限");
    }

    private void createPermission(String code, String name, String description) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);
        permission.setDescription(description);
        permissionRepository.save(permission);
    }

    private void initRoles() {
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

        // Admin role
        Role adminRole = new Role();
        adminRole.setCode("ADMIN");
        adminRole.setName("管理员");
        adminRole.setDescription("系统管理员，拥有所有权限");
        adminRole.setPermissions(allPermissions);
        roleRepository.save(adminRole);

        // User manager role
        Set<Permission> userPermissions = new HashSet<>(permissionRepository.findAllById(Set.of(1L, 2L, 3L, 4L)));
        Role userManagerRole = new Role();
        userManagerRole.setCode("USER_MANAGER");
        userManagerRole.setName("用户管理员");
        userManagerRole.setDescription("管理用户");
        userManagerRole.setPermissions(userPermissions);
        roleRepository.save(userManagerRole);

        // Read only role
        Set<Permission> readPermissions = new HashSet<>(permissionRepository.findAllById(Set.of(2L, 6L, 10L)));
        Role readOnlyRole = new Role();
        readOnlyRole.setCode("READ_ONLY");
        readOnlyRole.setName("只读用户");
        readOnlyRole.setDescription("只能查看数据");
        readOnlyRole.setPermissions(readPermissions);
        roleRepository.save(readOnlyRole);
    }

    private void initAdminUser() {
        Role adminRole = roleRepository.findByCode("ADMIN").orElseThrow();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@example.com");
        admin.setRealName("系统管理员");
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
    }
}
