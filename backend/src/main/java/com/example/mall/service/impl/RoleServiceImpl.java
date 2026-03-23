package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.dto.AssignPermissionDTO;
import com.example.mall.dto.RoleCreateDTO;
import com.example.mall.dto.RoleQueryDTO;
import com.example.mall.dto.RoleUpdateDTO;
import com.example.mall.entity.Permission;
import com.example.mall.entity.Role;
import com.example.mall.entity.UserRole;
import com.example.mall.exception.BusinessException;
import com.example.mall.exception.ErrorCode;
import com.example.mall.mapper.RoleMapper;
import com.example.mall.mapper.UserRoleMapper;
import com.example.mall.service.PermissionService;
import com.example.mall.service.RoleService;
import com.example.mall.vo.PageResultVO;
import com.example.mall.vo.RoleDetailVO;
import com.example.mall.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final UserRoleMapper userRoleMapper;
    private final PermissionService permissionService;

    @Override
    public PageResultVO<RoleVO> getRolePage(RoleQueryDTO queryDTO) {
        Page<Role> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getName()), Role::getName, queryDTO.getName())
               .like(StringUtils.hasText(queryDTO.getCode()), Role::getCode, queryDTO.getCode())
               .eq(queryDTO.getStatus() != null, Role::getStatus, queryDTO.getStatus())
               .orderByDesc(Role::getCreateTime);
        
        Page<Role> rolePage = this.page(page, wrapper);
        
        List<RoleVO> records = rolePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResultVO.of(records, rolePage.getTotal(), rolePage.getCurrent(), rolePage.getSize());
    }

    @Override
    public List<RoleVO> getAllEnabledRoles() {
        List<Role> roles = this.list(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 1)
                .orderByAsc(Role::getId));
        
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDetailVO getRoleDetailById(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        
        RoleDetailVO detailVO = new RoleDetailVO();
        BeanUtils.copyProperties(role, detailVO);
        
        List<Permission> permissions = permissionService.getPermissionsByRoleId(id);
        List<RoleDetailVO.PermissionVO> permissionVOList = permissions.stream()
                .map(this::convertToPermissionVO)
                .collect(Collectors.toList());
        detailVO.setPermissions(permissionVOList);
        
        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleCreateDTO createDTO) {
        if (existsByCode(createDTO.getCode())) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        
        if (existsByName(createDTO.getName())) {
            throw new BusinessException(ErrorCode.ROLE_NAME_EXISTS);
        }
        
        Role role = new Role();
        BeanUtils.copyProperties(createDTO, role);
        role.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 1);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        
        this.save(role);
        
        log.info("创建角色成功, id: {}, code: {}", role.getId(), role.getCode());
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateDTO updateDTO) {
        Role existingRole = this.getById(updateDTO.getId());
        if (existingRole == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        
        if (StringUtils.hasText(updateDTO.getName()) 
                && !updateDTO.getName().equals(existingRole.getName())
                && existsByName(updateDTO.getName())) {
            throw new BusinessException(ErrorCode.ROLE_NAME_EXISTS);
        }
        
        Role role = new Role();
        BeanUtils.copyProperties(updateDTO, role);
        role.setUpdateTime(LocalDateTime.now());
        
        this.updateById(role);
        
        log.info("更新角色成功, id: {}", updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        
        if (isRoleInUse(id)) {
            throw new BusinessException(ErrorCode.ROLE_IN_USE);
        }
        
        baseMapper.deletePermissionByRoleId(id);
        
        this.removeById(id);
        
        log.info("删除角色成功, id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRoles(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        for (Long id : ids) {
            if (isRoleInUse(id)) {
                Role role = this.getById(id);
                throw new BusinessException(ErrorCode.ROLE_IN_USE, 
                        "角色【" + (role != null ? role.getName() : id) + "】正在使用中，无法删除");
            }
        }
        
        baseMapper.deletePermissionByRoleIds(ids);
        
        this.removeByIds(ids);
        
        log.info("批量删除角色成功, ids: {}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, AssignPermissionDTO assignDTO) {
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        
        baseMapper.deletePermissionByRoleId(roleId);
        
        List<Long> permissionIds = assignDTO.getPermissionIds();
        if (!CollectionUtils.isEmpty(permissionIds)) {
            List<RoleMapper.RolePermissionDO> rolePermissions = permissionIds.stream()
                    .map(permissionId -> new RoleMapper.RolePermissionDO(roleId, permissionId))
                    .collect(Collectors.toList());
            
            baseMapper.batchInsertRolePermission(rolePermissions);
        }
        
        log.info("分配角色权限成功, roleId: {}, permissionCount: {}", roleId, 
                permissionIds != null ? permissionIds.size() : 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.ROLE_STATUS_INVALID);
        }
        
        Role updateRole = new Role();
        updateRole.setId(id);
        updateRole.setStatus(status);
        updateRole.setUpdateTime(LocalDateTime.now());
        
        this.updateById(updateRole);
        
        log.info("更新角色状态成功, id: {}, status: {}", id, status);
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectRolesByUserId(userId);
    }

    @Override
    public boolean existsByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        return this.count(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, code)) > 0;
    }

    @Override
    public boolean existsByName(String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        return this.count(new LambdaQueryWrapper<Role>()
                .eq(Role::getName, name)) > 0;
    }

    @Override
    public boolean isRoleInUse(Long roleId) {
        if (roleId == null) {
            return false;
        }
        return userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getRoleId, roleId)) > 0;
    }

    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }

    private RoleDetailVO.PermissionVO convertToPermissionVO(Permission permission) {
        RoleDetailVO.PermissionVO vo = new RoleDetailVO.PermissionVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }
}
