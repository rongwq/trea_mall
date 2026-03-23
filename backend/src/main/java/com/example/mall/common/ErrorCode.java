package com.example.mall.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_INVALID(1003, "密码不能为空"),
    USER_DISABLED(1004, "用户已被禁用"),
    
    ROLE_NOT_FOUND(2001, "角色不存在"),
    ROLE_EXISTS(2002, "角色编码已存在"),
    
    PERMISSION_NOT_FOUND(3001, "权限不存在"),
    
    INVALID_PARAMETER(4001, "参数无效"),
    UNAUTHORIZED(4002, "未授权"),
    FORBIDDEN(4003, "禁止访问"),
    INTERNAL_SERVER_ERROR(5000, "服务器内部错误");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
