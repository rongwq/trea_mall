package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Optional;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT DISTINCT p.code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT u.* FROM sys_user u ${ew.customSqlSegment}")
    IPage<User> selectUserPage(Page<User> page, @Param(Constants.WRAPPER) Wrapper<User> queryWrapper);

    @Select("SELECT u.*, r.id as role_id, r.name as role_name, r.code as role_code " +
            "FROM sys_user u " +
            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE u.id = #{id} AND u.deleted = 0")
    Optional<User> selectUserWithRolesById(@Param("id") Long id);

    @Select("SELECT u.id FROM sys_user u WHERE u.username = #{username} AND u.deleted = 0 LIMIT 1")
    Optional<Long> selectIdByUsername(@Param("username") String username);
}
