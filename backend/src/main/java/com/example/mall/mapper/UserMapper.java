package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户数据访问层
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} LIMIT 1")
    User selectByUsername(@Param("username") String username);

    /**
     * 查询用户权限列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户角色列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> selectRolesByUserId(@Param("userId") Long userId);
}
