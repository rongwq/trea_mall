-- RBAC 权限管理系统初始数据脚本
-- 默认管理员账号：admin / admin123

USE rbac_db;

-- 插入权限数据
INSERT INTO permissions (code, name, description) VALUES
-- 用户管理权限
('user:create', '创建用户', '创建新用户'),
('user:read', '查看用户', '查看用户列表和详情'),
('user:update', '更新用户', '更新用户信息'),
('user:delete', '删除用户', '删除用户'),

-- 角色管理权限
('role:create', '创建角色', '创建新角色'),
('role:read', '查看角色', '查看角色列表和详情'),
('role:update', '更新角色', '更新角色信息'),
('role:delete', '删除角色', '删除角色'),

-- 权限管理权限
('permission:create', '创建权限', '创建新权限'),
('permission:read', '查看权限', '查看权限列表和详情'),
('permission:update', '更新权限', '更新权限信息'),
('permission:delete', '删除权限', '删除权限');

-- 插入角色数据
INSERT INTO roles (code, name, description) VALUES
('ADMIN', '管理员', '系统管理员，拥有所有权限'),
('USER_MANAGER', '用户管理员', '管理用户'),
('READ_ONLY', '只读用户', '只能查看数据');

-- 为管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.code = 'ADMIN';

-- 为用户管理员角色分配用户管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.code = 'USER_MANAGER' 
AND p.code IN ('user:create', 'user:read', 'user:update', 'user:delete');

-- 为只读角色分配查看权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.code = 'READ_ONLY' 
AND p.code IN ('user:read', 'role:read', 'permission:read');

-- 插入默认管理员用户（密码：admin123，BCrypt加密）
INSERT INTO users (username, password, email, real_name, is_active) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'admin@example.com', '系统管理员', 1);

-- 为管理员用户分配管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.code = 'ADMIN';
