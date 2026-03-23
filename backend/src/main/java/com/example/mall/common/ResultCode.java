package com.example.mall.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    ROLE_NOT_FOUND(1005, "角色不存在"),
    PERMISSION_NOT_FOUND(1006, "权限不存在"),
    ROLE_CODE_EXISTS(1007, "角色编码已存在"),
    PERMISSION_CODE_EXISTS(1008, "权限编码已存在"),
    
    VALIDATION_ERROR(2001, "参数校验失败"),
    DUPLICATE_DATA(2002, "数据重复"),
    DATA_INTEGRITY_VIOLATION(2003, "数据完整性约束违反"),
    
    TOKEN_INVALID(3001, "Token无效"),
    TOKEN_EXPIRED(3002, "Token已过期"),
    TOKEN_MISSING(3003, "Token缺失");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
