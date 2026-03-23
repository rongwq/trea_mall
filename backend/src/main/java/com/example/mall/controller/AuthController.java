package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.config.JwtTokenUtil;
import com.example.mall.entity.User;
import com.example.mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        User user = userService.getByUsername(username).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }
        if (user.getStatus() != 1) {
            return Result.error("账号已被禁用");
        }
        String token = jwtTokenUtil.generateToken(username, user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success(data);
    }
    
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        User existUser = userService.getByUsername(user.getUsername()).orElse(null);
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);
        return Result.success();
    }
}
