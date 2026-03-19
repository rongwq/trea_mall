package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.Role;
import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {
    List<Role> selectRolesByUserId(Long userId);
}
