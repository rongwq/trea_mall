package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.entity.Permission;
import com.example.mall.mapper.PermissionMapper;
import com.example.mall.service.PermissionService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    
    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return baseMapper.selectPermissionsByRoleId(roleId);
    }
    
    @Override
    public List<Permission> getPermissionTree() {
        List<Permission> allPermissions = list(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getStatus, 1)
                .orderByAsc(Permission::getSort));
        
        Map<Long, List<Permission>> groupMap = allPermissions.stream()
                .collect(Collectors.groupingBy(Permission::getParentId));
        
        return buildTree(groupMap, 0L);
    }
    
    private List<Permission> buildTree(Map<Long, List<Permission>> groupMap, Long parentId) {
        List<Permission> children = groupMap.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }
        return children;
    }
}
