package com.example.mall.enums;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(500, "业务错误"),
    SYSTEM_ERROR(500, "系统错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USERNAME_ALREADY_EXISTS(1003, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(1004, "邮箱已存在"),
    PHONE_ALREADY_EXISTS(1005, "手机号已存在"),
    INVALID_PASSWORD(1006, "密码错误"),
    USER_DISABLED(1007, "用户已禁用"),

    ROLE_NOT_FOUND(2001, "角色不存在"),
    PERMISSION_DENIED(2002, "权限不足");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
