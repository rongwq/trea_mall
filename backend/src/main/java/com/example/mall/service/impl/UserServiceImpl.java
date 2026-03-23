package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.constants.SystemConstants;
import com.example.mall.dto.UserCreateDTO;
import com.example.mall.dto.UserQueryDTO;
import com.example.mall.dto.UserUpdateDTO;
import com.example.mall.entity.User;
import com.example.mall.entity.UserRole;
import com.example.mall.enums.ErrorCode;
import com.example.mall.enums.UserStatus;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.UserMapper;
import com.example.mall.mapper.UserRoleMapper;
import com.example.mall.service.RoleService;
import com.example.mall.service.UserService;
import com.example.mall.util.BeanCopyUtils;
import com.example.mall.util.SecurityUtils;
import com.example.mall.vo.PageVO;
import com.example.mall.vo.RoleVO;
import com.example.mall.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageVO<UserVO> queryUserPage(UserQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = buildQueryWrapper(queryDTO);

        // 执行分页查询
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<User> userPage = this.page(page, wrapper);

        // 转换为VO并填充角色信息
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageVO.of(
                userPage.getCurrent(),
                userPage.getSize(),
                userPage.getTotal(),
                userPage.getPages(),
                userVOList
        );
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    @Override
    public User getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return this.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .last("LIMIT 1")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO createDTO) {
        // 校验用户名唯一性
        if (isUsernameExists(createDTO.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 校验邮箱唯一性
        if (StringUtils.hasText(createDTO.getEmail()) && isEmailExists(createDTO.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 校验手机号唯一性
        if (StringUtils.hasText(createDTO.getPhone()) && isPhoneExists(createDTO.getPhone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // 构建用户实体
        User user = new User();
        user.setUsername(createDTO.getUsername());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setEmail(createDTO.getEmail());
        user.setPhone(createDTO.getPhone());
        user.setStatus(Objects.requireNonNullElse(createDTO.getStatus(), UserStatus.ENABLED.getCode()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户
        this.save(user);

        // 分配角色
        if (!CollectionUtils.isEmpty(createDTO.getRoleIds())) {
            assignRoles(user.getId(), createDTO.getRoleIds());
        }

        log.info("用户创建成功, userId: {}, username: {}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserUpdateDTO updateDTO) {
        // 校验用户是否存在
        User existUser = this.getById(userId);
        if (existUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 校验用户名唯一性
        if (StringUtils.hasText(updateDTO.getUsername())
                && !existUser.getUsername().equals(updateDTO.getUsername())
                && isUsernameExists(updateDTO.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 校验邮箱唯一性
        if (StringUtils.hasText(updateDTO.getEmail())
                && !updateDTO.getEmail().equals(existUser.getEmail())
                && isEmailExists(updateDTO.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 校验手机号唯一性
        if (StringUtils.hasText(updateDTO.getPhone())
                && !updateDTO.getPhone().equals(existUser.getPhone())
                && isPhoneExists(updateDTO.getPhone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // 更新用户信息
        User user = new User();
        user.setId(userId);
        user.setUsername(updateDTO.getUsername());
        user.setEmail(updateDTO.getEmail());
        user.setPhone(updateDTO.getPhone());
        if (updateDTO.getStatus() != null) {
            user.setStatus(updateDTO.getStatus());
        }

        // 更新密码
        if (StringUtils.hasText(updateDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);

        // 更新角色
        if (updateDTO.getRoleIds() != null) {
            assignRoles(userId, updateDTO.getRoleIds());
        }

        log.info("用户更新成功, userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        // 校验用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 禁止删除超级管理员
        if (SystemConstants.SUPER_ADMIN_ID.equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "不能删除超级管理员");
        }

        // 删除用户角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
        );

        // 删除用户
        this.removeById(userId);

        log.info("用户删除成功, userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUser(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        // 过滤掉超级管理员
        if (userIds.contains(SystemConstants.SUPER_ADMIN_ID)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "不能删除超级管理员");
        }

        // 批量删除用户角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .in(UserRole::getUserId, userIds)
        );

        // 批量删除用户
        this.removeByIds(userIds);

        log.info("批量删除用户成功, userIds: {}", userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long userId) {
        updateUserStatus(userId, UserStatus.ENABLED.getCode());
        log.info("用户启用成功, userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId) {
        // 禁止禁用超级管理员
        if (SystemConstants.SUPER_ADMIN_ID.equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "不能禁用超级管理员");
        }

        // 禁止禁用当前登录用户
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (userId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "不能禁用当前登录用户");
        }

        updateUserStatus(userId, UserStatus.DISABLED.getCode());
        log.info("用户禁用成功, userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long userId) {
        // 校验用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 生成随机密码
        String newPassword = RandomStringUtils.randomAlphanumeric(8);

        // 更新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        updateUser.setUpdateTime(LocalDateTime.now());
        this.updateById(updateUser);

        log.info("用户密码重置成功, userId: {}", userId);
        return newPassword;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 校验用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 删除原有角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
        );

        // 批量插入新角色关联
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<UserRole> userRoles = roleIds.stream()
                    .map(roleId -> {
                        UserRole userRole = new UserRole();
                        userRole.setUserId(userId);
                        userRole.setRoleId(roleId);
                        return userRole;
                    })
                    .collect(Collectors.toList());

            // 使用批量插入优化性能
            for (UserRole userRole : userRoles) {
                userRoleMapper.insert(userRole);
            }
        }

        log.info("用户角色分配成功, userId: {}, roleIds: {}", userId, roleIds);
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRole>()
                                .eq(UserRole::getUserId, userId)
                ).stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return this.count(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        ) > 0;
    }

    @Override
    public boolean isEmailExists(String email) {
        return this.count(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, email)
        ) > 0;
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return this.count(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, phone)
        ) > 0;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<User> buildQueryWrapper(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 用户名模糊查询
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(User::getUsername, queryDTO.getUsername());
        }

        // 邮箱模糊查询
        if (StringUtils.hasText(queryDTO.getEmail())) {
            wrapper.like(User::getEmail, queryDTO.getEmail());
        }

        // 手机号模糊查询
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(User::getPhone, queryDTO.getPhone());
        }

        // 状态精确查询
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        return wrapper;
    }

    /**
     * 更新用户状态
     */
    private void updateUserStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(status);
        updateUser.setUpdateTime(LocalDateTime.now());
        this.updateById(updateUser);
    }

    /**
     * 转换为VO对象
     */
    private UserVO convertToVO(User user) {
        if (user == null) {
            return null;
        }

        UserVO userVO = BeanCopyUtils.copy(user, UserVO.class);

        // 设置状态名称
        UserStatus status = UserStatus.of(user.getStatus());
        if (status != null) {
            userVO.setStatusName(status.getDescription());
        }

        // 填充角色信息
        List<Long> roleIds = getUserRoleIds(user.getId());
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<RoleVO> roles = roleIds.stream()
                    .map(roleId -> {
                        RoleVO roleVO = new RoleVO();
                        roleVO.setId(roleId);
                        return roleVO;
                    })
                    .collect(Collectors.toList());
            userVO.setRoles(roles);
        }

        return userVO;
    }
}
