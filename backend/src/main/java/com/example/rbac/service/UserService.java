package com.example.rbac.service;

import com.example.rbac.dto.PageResult;
import com.example.rbac.dto.UserDTO;

import java.util.Set;

public interface UserService {
    
    UserDTO createUser(UserDTO userDTO);
    
    UserDTO updateUser(Long id, UserDTO userDTO);
    
    void deleteUser(Long id);
    
    UserDTO getUserById(Long id);
    
    UserDTO getUserByUsername(String username);
    
    PageResult<UserDTO> getAllUsers(int page, int size, String keyword);
    
    void assignRoles(Long userId, Set<Long> roleIds);
    
    void changePassword(Long userId, String oldPassword, String newPassword);
}
