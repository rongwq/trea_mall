package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 " +
            "ORDER BY r.id")
    List<Role> selectRolesByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(1) FROM sys_role WHERE code = #{code}")
    int countByCode(@Param("code") String code);
    
    @Select("SELECT COUNT(1) FROM sys_role WHERE name = #{name}")
    int countByName(@Param("name") String name);
    
    @Select("SELECT COUNT(1) FROM sys_user_role WHERE role_id = #{roleId}")
    int countUserByRoleId(@Param("roleId") Long roleId);
    
    @Insert("<script>" +
            "INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.roleId}, #{item.permissionId})" +
            "</foreach>" +
            "</script>")
    int batchInsertRolePermission(@Param("list") List<RolePermissionDO> list);
    
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deletePermissionByRoleId(@Param("roleId") Long roleId);
    
    @Delete("<script>" +
            "DELETE FROM sys_role_permission WHERE role_id IN " +
            "<foreach collection='list' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deletePermissionByRoleIds(@Param("list") List<Long> roleIds);
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    class RolePermissionDO {
        private Long roleId;
        private Long permissionId;
    }
}
