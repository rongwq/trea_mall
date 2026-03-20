package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.entity.Permission;
import java.util.List;

public interface PermissionService extends IService<Permission> {
    List<Permission> getPermissionsByRoleId(Long roleId);
    List<Permission> getPermissionTree();
}
