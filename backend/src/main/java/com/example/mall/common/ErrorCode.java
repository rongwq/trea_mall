package com.example.mall.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // 业务错误码
    ROLE_NOT_FOUND(1001, "角色不存在"),
    ROLE_CODE_EXISTS(1002, "角色编码已存在"),
    ROLE_NAME_EXISTS(1003, "角色名称已存在"),
    ROLE_CANNOT_DELETE(1004, "角色无法删除（可能已分配给用户）"),
    PERMISSION_NOT_FOUND(1005, "权限不存在"),
    USER_NOT_FOUND(1006, "用户不存在"),
    USERNAME_EXISTS(1007, "用户名已存在");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
