package com.example.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.user.AssignRolesDTO;
import com.example.mall.dto.user.UserCreateDTO;
import com.example.mall.dto.user.UserQueryDTO;
import com.example.mall.dto.user.UserUpdateDTO;
import com.example.mall.entity.User;
import com.example.mall.vo.user.UserVO;
import java.util.List;
import java.util.Optional;

public interface UserService extends IService<User> {

    IPage<UserVO> queryUsers(UserQueryDTO queryDTO);

    Optional<UserVO> getUserWithRolesById(Long id);

    UserVO createUser(UserCreateDTO createDTO);

    UserVO updateUser(Long id, UserUpdateDTO updateDTO);

    void deleteUser(Long id);

    void deleteUsers(List<Long> ids);

    void assignRoles(Long userId, AssignRolesDTO assignRolesDTO);

    Optional<User> getByUsername(String username);

    List<String> getPermissionsByUserId(Long userId);

    boolean existsByUsername(String username);

    UserVO convertToVO(User user);
}
