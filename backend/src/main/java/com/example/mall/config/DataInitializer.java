package com.example.mall.config;

import com.example.mall.entity.Permission;
import com.example.mall.entity.Role;
import com.example.mall.entity.RolePermission;
import com.example.mall.entity.User;
import com.example.mall.entity.UserRole;
import com.example.mall.mapper.PermissionMapper;
import com.example.mall.mapper.RoleMapper;
import com.example.mall.mapper.RolePermissionMapper;
import com.example.mall.mapper.UserMapper;
import com.example.mall.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initAdminUser();
    }
    
    private void initAdminUser() {
        User existUser = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")
        );
        
        if (existUser == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@example.com");
            admin.setPhone("13800138000");
            admin.setStatus(1);
            userMapper.insert(admin);
            
            Role adminRole = new Role();
            adminRole.setName("超级管理员");
            adminRole.setCode("SUPER_ADMIN");
            adminRole.setDescription("拥有所有权限");
            adminRole.setStatus(1);
            roleMapper.insert(adminRole);
            
            UserRole userRole = new UserRole();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleMapper.insert(userRole);
            
            createPermissions(adminRole.getId());
            
            System.out.println("Admin user initialized with password: 123456");
        } else {
            existUser.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(existUser);
            System.out.println("Admin password reset to: 123456");
        }
    }
    
    private void createPermissions(Long roleId) {
        List<Permission> permissions = Arrays.asList(
            createPermission("系统管理", "system", "menu", 0L, "/system", "Setting", 1),
            createPermission("用户管理", "user:manage", "menu", 1L, "/system/user", "User", 1),
            createPermission("用户查看", "user:view", "button", 2L, null, null, 1),
            createPermission("用户新增", "user:add", "button", 2L, null, null, 2),
            createPermission("用户编辑", "user:edit", "button", 2L, null, null, 3),
            createPermission("用户删除", "user:delete", "button", 2L, null, null, 4),
            createPermission("角色管理", "role:manage", "menu", 1L, "/system/role", "UserFilled", 2),
            createPermission("角色查看", "role:view", "button", 7L, null, null, 1),
            createPermission("角色新增", "role:add", "button", 7L, null, null, 2),
            createPermission("角色编辑", "role:edit", "button", 7L, null, null, 3),
            createPermission("角色删除", "role:delete", "button", 7L, null, null, 4),
            createPermission("权限管理", "permission:manage", "menu", 1L, "/system/permission", "Lock", 3),
            createPermission("权限查看", "permission:view", "button", 12L, null, null, 1),
            createPermission("权限新增", "permission:add", "button", 12L, null, null, 2),
            createPermission("权限编辑", "permission:edit", "button", 12L, null, null, 3),
            createPermission("权限删除", "permission:delete", "button", 12L, null, null, 4)
        );
        
        for (Permission permission : permissions) {
            Permission existPermission = permissionMapper.selectOne(
                new LambdaQueryWrapper<Permission>().eq(Permission::getCode, permission.getCode())
            );
            if (existPermission == null) {
                permissionMapper.insert(permission);
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permission.getId());
                rolePermissionMapper.insert(rolePermission);
            } else {
                RolePermission rp = rolePermissionMapper.selectOne(
                    new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
                        .eq(RolePermission::getPermissionId, existPermission.getId())
                );
                if (rp == null) {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(roleId);
                    rolePermission.setPermissionId(existPermission.getId());
                    rolePermissionMapper.insert(rolePermission);
                }
            }
        }
    }
    
    private Permission createPermission(String name, String code, String type, Long parentId, String path, String icon, Integer sort) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setCode(code);
        permission.setType(type);
        permission.setParentId(parentId);
        permission.setPath(path);
        permission.setIcon(icon);
        permission.setSort(sort);
        permission.setStatus(1);
        return permission;
    }
}
