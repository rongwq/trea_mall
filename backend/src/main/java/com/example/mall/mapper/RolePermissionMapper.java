package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    void batchInsert(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
