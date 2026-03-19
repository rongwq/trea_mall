package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.entity.Role;
import java.util.List;

public interface RoleService extends IService<Role> {
    List<Role> getRolesByUserId(Long userId);
    void assignPermissions(Long roleId, List<Long> permissionIds);
}
