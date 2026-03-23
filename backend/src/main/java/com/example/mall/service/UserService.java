package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.UserCreateDTO;
import com.example.mall.dto.UserQueryDTO;
import com.example.mall.dto.UserUpdateDTO;
import com.example.mall.entity.User;
import com.example.mall.vo.PageVO;
import com.example.mall.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageVO<UserVO> queryUserPage(UserQueryDTO queryDTO);

    /**
     * 根据ID查询用户详情
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 创建用户
     *
     * @param createDTO 创建参数
     * @return 用户ID
     */
    Long createUser(UserCreateDTO createDTO);

    /**
     * 更新用户信息
     *
     * @param userId    用户ID
     * @param updateDTO 更新参数
     */
    void updateUser(Long userId, UserUpdateDTO updateDTO);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     */
    void batchDeleteUser(List<Long> userIds);

    /**
     * 启用用户
     *
     * @param userId 用户ID
     */
    void enableUser(Long userId);

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     */
    void disableUser(Long userId);

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 新密码
     */
    String resetPassword(Long userId);

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getPermissionsByUserId(Long userId);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true-存在 false-不存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return true-存在 false-不存在
     */
    boolean isEmailExists(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return true-存在 false-不存在
     */
    boolean isPhoneExists(String phone);
}
