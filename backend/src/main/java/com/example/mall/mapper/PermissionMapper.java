package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.Permission;
import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {
    List<Permission> selectPermissionsByRoleId(Long roleId);
}
