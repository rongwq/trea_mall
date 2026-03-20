CREATE DATABASE IF NOT EXISTS mall_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_admin;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    type VARCHAR(20) NOT NULL COMMENT '类型：menu-菜单，button-按钮',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    path VARCHAR(200) COMMENT '路由路径',
    icon VARCHAR(50) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

INSERT INTO sys_user (username, password, email, phone, status) VALUES
('admin', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', 'admin@example.com', '13800138000', 1);

INSERT INTO sys_role (name, code, description, status) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('普通用户', 'USER', '普通用户角色', 1);

INSERT INTO sys_permission (name, code, type, parent_id, path, icon, sort, status) VALUES
('系统管理', 'system', 'menu', 0, '/system', 'Setting', 1, 1),
('用户管理', 'user:manage', 'menu', 1, '/system/user', 'User', 1, 1),
('用户查看', 'user:view', 'button', 2, NULL, NULL, 1, 1),
('用户新增', 'user:add', 'button', 2, NULL, NULL, 2, 1),
('用户编辑', 'user:edit', 'button', 2, NULL, NULL, 3, 1),
('用户删除', 'user:delete', 'button', 2, NULL, NULL, 4, 1),
('角色管理', 'role:manage', 'menu', 1, '/system/role', 'UserFilled', 2, 1),
('角色查看', 'role:view', 'button', 7, NULL, NULL, 1, 1),
('角色新增', 'role:add', 'button', 7, NULL, NULL, 2, 1),
('角色编辑', 'role:edit', 'button', 7, NULL, NULL, 3, 1),
('角色删除', 'role:delete', 'button', 7, NULL, NULL, 4, 1),
('权限管理', 'permission:manage', 'menu', 1, '/system/permission', 'Lock', 3, 1),
('权限查看', 'permission:view', 'button', 12, NULL, NULL, 1, 1),
('权限新增', 'permission:add', 'button', 12, NULL, NULL, 2, 1),
('权限编辑', 'permission:edit', 'button', 12, NULL, NULL, 3, 1),
('权限删除', 'permission:delete', 'button', 12, NULL, NULL, 4, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16);
