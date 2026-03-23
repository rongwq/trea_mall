package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.ResultCode;
import com.example.mall.dto.user.AssignRolesDTO;
import com.example.mall.dto.user.UserCreateDTO;
import com.example.mall.dto.user.UserQueryDTO;
import com.example.mall.dto.user.UserUpdateDTO;
import com.example.mall.entity.Role;
import com.example.mall.entity.User;
import com.example.mall.entity.UserRole;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.UserMapper;
import com.example.mall.mapper.UserRoleMapper;
import com.example.mall.service.RoleService;
import com.example.mall.service.UserService;
import com.example.mall.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<UserVO> queryUsers(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<User> wrapper = buildQueryWrapper(queryDTO);
        IPage<User> userPage = baseMapper.selectUserPage(page, wrapper);
        return userPage.convert(this::convertToVO);
    }

    @Override
    public Optional<UserVO> getUserWithRolesById(Long id) {
        return baseMapper.selectUserWithRolesById(id)
                .map(user -> {
                    UserVO vo = convertToVO(user);
                    List<Role> roles = roleService.getRolesByUserId(id);
                    vo.setRoles(convertToRoleVOList(roles));
                    return vo;
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserCreateDTO createDTO) {
        if (existsByUsername(createDTO.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        User user = new User();
        BeanUtils.copyProperties(createDTO, user);
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        save(user);
        log.info("创建用户成功: userId={}, username={}", user.getId(), user.getUsername());
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long id, UserUpdateDTO updateDTO) {
        User existUser = getById(id);
        if (existUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        User user = new User();
        BeanUtils.copyProperties(updateDTO, user);
        user.setId(id);
        if (StringUtils.hasText(updateDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        } else {
            user.setPassword(null);
        }
        updateById(user);
        log.info("更新用户成功: userId={}", id);
        return getUserWithRolesById(id).orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        removeById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        log.info("删除用户成功: userId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUsers(List<Long> ids) {
        removeBatchByIds(ids);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, ids));
        log.info("批量删除用户成功: userIds={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, AssignRolesDTO assignRolesDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        List<Long> roleIds = assignRolesDTO.getRoleIds();
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> userRoleList = roleIds.stream()
                    .map(roleId -> {
                        UserRole userRole = new UserRole();
                        userRole.setUserId(userId);
                        userRole.setRoleId(roleId);
                        return userRole;
                    })
                    .collect(Collectors.toList());
            userRoleMapper.batchInsert(userRoleList);
        }
        log.info("分配角色成功: userId={}, roleIds={}", userId, roleIds);
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return Optional.ofNullable(getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)));
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        return baseMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return baseMapper.selectIdByUsername(username).isPresent();
    }

    @Override
    public UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getRoles() != null) {
            vo.setRoles(convertToRoleVOList(user.getRoles()));
        }
        return vo;
    }

    private LambdaQueryWrapper<User> buildQueryWrapper(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(User::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(User::getPhone, queryDTO.getPhone());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(User::getCreateTime);
        return wrapper;
    }

    private List<UserVO.RoleVO> convertToRoleVOList(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        return roles.stream()
                .map(this::convertToRoleVO)
                .collect(Collectors.toList());
    }

    private UserVO.RoleVO convertToRoleVO(Role role) {
        UserVO.RoleVO vo = new UserVO.RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        return vo;
    }
}
