package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.entity.User;
import java.util.List;

public interface UserService extends IService<User> {
    User getByUsername(String username);
    List<String> getPermissionsByUserId(Long userId);
    void assignRoles(Long userId, List<Long> roleIds);
}
