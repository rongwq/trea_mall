package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.PageResult;
import com.example.mall.common.exception.BusinessException;
import com.example.mall.dto.role.RoleAssignPermissionDTO;
import com.example.mall.dto.role.RoleCreateDTO;
import com.example.mall.dto.role.RoleQueryDTO;
import com.example.mall.dto.role.RoleUpdateDTO;
import com.example.mall.entity.Permission;
import com.example.mall.entity.Role;
import com.example.mall.entity.RolePermission;
import com.example.mall.mapper.RoleMapper;
import com.example.mall.mapper.RolePermissionMapper;
import com.example.mall.mapper.UserRoleMapper;
import com.example.mall.service.PermissionService;
import com.example.mall.service.RoleService;
import com.example.mall.vo.role.RoleSimpleVO;
import com.example.mall.vo.role.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionService permissionService;

    @Override
    public PageResult<RoleVO> pageRoles(RoleQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery();
        
        // 角色名称模糊查询
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Role::getName, queryDTO.getName());
        }
        
        // 角色编码精确查询
        if (StringUtils.hasText(queryDTO.getCode())) {
            wrapper.eq(Role::getCode, queryDTO.getCode());
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Role::getStatus, queryDTO.getStatus());
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Role::getCreateTime);
        
        // 执行分页查询
        Page<Role> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Role> rolePage = this.page(page, wrapper);
        
        // 转换为VO
        List<RoleVO> voList = rolePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(rolePage.getCurrent(), rolePage.getSize(), 
                rolePage.getTotal(), voList);
    }

    @Override
    public List<RoleSimpleVO> listAllEnabledRoles() {
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Role::getStatus, 1)
               .orderByDesc(Role::getCreateTime);
        
        List<Role> roles = this.list(wrapper);
        
        return roles.stream()
                .map(role -> {
                    RoleSimpleVO vo = new RoleSimpleVO();
                    BeanUtils.copyProperties(role, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleById(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        
        RoleVO vo = convertToVO(role);
        
        // 加载权限列表
        List<Permission> permissions = permissionService.getPermissionsByRoleId(id);
        vo.setPermissions(permissions);
        
        return vo;
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return baseMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleCreateDTO createDTO) {
        // 检查角色编码是否已存在
        checkCodeExists(createDTO.getCode(), null);
        
        // 检查角色名称是否已存在
        checkNameExists(createDTO.getName(), null);
        
        // 创建角色实体
        Role role = new Role();
        BeanUtils.copyProperties(createDTO, role);
        role.setStatus(1);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        
        // 保存角色
        this.save(role);
        
        log.info("创建角色成功: id={}, name={}, code={}", role.getId(), role.getName(), role.getCode());
        
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateDTO updateDTO) {
        Long id = updateDTO.getId();
        
        // 检查角色是否存在
        Role existingRole = this.getById(id);
        if (existingRole == null) {
            throw new BusinessException(404, "角色不存在");
        }
        
        // 检查角色编码是否被其他角色使用
        if (!existingRole.getCode().equals(updateDTO.getCode())) {
            checkCodeExists(updateDTO.getCode(), id);
        }
        
        // 检查角色名称是否被其他角色使用
        if (!existingRole.getName().equals(updateDTO.getName())) {
            checkNameExists(updateDTO.getName(), id);
        }
        
        // 更新角色实体
        BeanUtils.copyProperties(updateDTO, existingRole);
        existingRole.setUpdateTime(LocalDateTime.now());
        
        // 更新角色
        this.updateById(existingRole);
        
        log.info("更新角色成功: id={}, name={}", id, existingRole.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        // 检查角色是否存在
        Role role = this.getById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        
        // 检查角色是否被用户使用
        Long userCount = userRoleMapper.selectCount(
                Wrappers.<com.example.mall.entity.UserRole>lambdaQuery()
                        .eq(com.example.mall.entity.UserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException(400, "该角色已被用户使用，无法删除");
        }
        
        // 删除角色权限关联
        rolePermissionMapper.delete(
                Wrappers.<RolePermission>lambdaQuery()
                        .eq(RolePermission::getRoleId, id));
        
        // 删除角色
        this.removeById(id);
        
        log.info("删除角色成功: id={}, name={}", id, role.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(RoleAssignPermissionDTO assignPermissionDTO) {
        Long roleId = assignPermissionDTO.getRoleId();
        List<Long> permissionIds = assignPermissionDTO.getPermissionIds();
        
        // 检查角色是否存在
        Role role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        
        // 去重并过滤空值
        List<Long> distinctPermissionIds = permissionIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(distinctPermissionIds)) {
            throw new BusinessException(400, "权限ID列表不能为空");
        }
        
        // 删除原有权限关联
        rolePermissionMapper.delete(
                Wrappers.<RolePermission>lambdaQuery()
                        .eq(RolePermission::getRoleId, roleId));
        
        // 批量插入新的权限关联
        if (!CollectionUtils.isEmpty(distinctPermissionIds)) {
            List<RolePermission> rolePermissions = distinctPermissionIds.stream()
                    .map(permissionId -> {
                        RolePermission rp = new RolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(permissionId);
                        return rp;
                    })
                    .collect(Collectors.toList());
            
            // 使用批量插入
            saveBatchRolePermissions(rolePermissions);
        }
        
        log.info("角色分配权限成功: roleId={}, permissionCount={}", roleId, distinctPermissionIds.size());
    }
    
    /**
     * 批量保存角色权限关联
     */
    private void saveBatchRolePermissions(List<RolePermission> rolePermissions) {
        // 分批处理，每批1000条，避免SQL过长
        int batchSize = 1000;
        int size = rolePermissions.size();
        
        for (int i = 0; i < size; i += batchSize) {
            List<RolePermission> batch = rolePermissions.subList(i, Math.min(i + batchSize, size));
            // 使用XML批量插入，性能更优
            rolePermissionMapper.batchInsert(batch);
        }
    }
    
    /**
     * 检查角色编码是否已存在
     */
    private void checkCodeExists(String code, Long excludeId) {
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Role::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }
        
        Long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(400, "角色编码已存在: " + code);
        }
    }
    
    /**
     * 检查角色名称是否已存在
     */
    private void checkNameExists(String name, Long excludeId) {
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Role::getName, name);
        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }
        
        Long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(400, "角色名称已存在: " + name);
        }
    }
    
    /**
     * 转换为VO
     */
    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
