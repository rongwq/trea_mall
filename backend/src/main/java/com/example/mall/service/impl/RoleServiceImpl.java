package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.BusinessException;
import com.example.mall.common.ErrorCode;
import com.example.mall.dto.role.RoleCreateDTO;
import com.example.mall.dto.role.RolePermissionAssignDTO;
import com.example.mall.dto.role.RoleQueryDTO;
import com.example.mall.dto.role.RoleUpdateDTO;
import com.example.mall.entity.Role;
import com.example.mall.entity.RolePermission;
import com.example.mall.mapper.RoleMapper;
import com.example.mall.mapper.RolePermissionMapper;
import com.example.mall.service.IRoleService;
import com.example.mall.vo.permission.PermissionVO;
import com.example.mall.vo.role.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public Page<RoleVO> getRolePage(RoleQueryDTO queryDTO) {
        Page<Role> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Role::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getCode())) {
            wrapper.like(Role::getCode, queryDTO.getCode());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Role::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(Role::getCreateTime);

        Page<Role> rolePage = this.page(page, wrapper);
        return rolePage.convert(this::convertToVO);
    }

    @Override
    public List<RoleVO> getAllEnabledRoles() {
        List<Role> roles = this.list(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getStatus, 1)
                .orderByDesc(Role::getCreateTime));
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleDetail(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleCreateDTO createDTO) {
        if (isRoleCodeExists(createDTO.getCode())) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        if (isRoleNameExists(createDTO.getName())) {
            throw new BusinessException(ErrorCode.ROLE_NAME_EXISTS);
        }

        Role role = new Role();
        BeanUtils.copyProperties(createDTO, role);
        this.save(role);
        log.info("创建角色成功，角色ID: {}, 角色编码: {}", role.getId(), role.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleUpdateDTO updateDTO) {
        Role existingRole = this.getById(id);
        if (existingRole == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingRole.getName())) {
            if (isRoleNameExists(updateDTO.getName())) {
                throw new BusinessException(ErrorCode.ROLE_NAME_EXISTS);
            }
        }

        Role roleToUpdate = new Role();
        roleToUpdate.setId(id);
        if (updateDTO.getName() != null) {
            roleToUpdate.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            roleToUpdate.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getStatus() != null) {
            roleToUpdate.setStatus(updateDTO.getStatus());
        }

        this.updateById(roleToUpdate);
        log.info("更新角色成功，角色ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        this.removeById(id);
        rolePermissionMapper.delete(Wrappers.lambdaQuery(RolePermission.class)
                .eq(RolePermission::getRoleId, id));
        log.info("删除角色成功，角色ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, RolePermissionAssignDTO assignDTO) {
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        rolePermissionMapper.delete(Wrappers.lambdaQuery(RolePermission.class)
                .eq(RolePermission::getRoleId, roleId));

        List<Long> permissionIds = assignDTO.getPermissionIds();
        if (!CollectionUtils.isEmpty(permissionIds)) {
            rolePermissionMapper.batchInsert(roleId, permissionIds);
        }
        log.info("分配权限成功，角色ID: {}, 权限数量: {}", roleId, permissionIds.size());
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<RoleVO> getRolesByUserId(Long userId) {
        List<Role> roles = baseMapper.selectRolesByUserId(userId);
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isRoleCodeExists(String code) {
        return this.count(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getCode, code)) > 0;
    }

    @Override
    public boolean isRoleNameExists(String name) {
        return this.count(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getName, name)) > 0;
    }

    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
