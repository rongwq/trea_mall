package com.example.mall.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    ROLE_NOT_FOUND(1001, "角色不存在"),
    ROLE_CODE_EXISTS(1002, "角色编码已存在"),
    ROLE_NAME_EXISTS(1003, "角色名称已存在"),
    ROLE_IN_USE(1004, "角色正在使用中，无法删除"),
    ROLE_STATUS_INVALID(1005, "角色状态无效"),

    PERMISSION_NOT_FOUND(1101, "权限不存在"),
    PERMISSION_ASSIGN_FAILED(1102, "权限分配失败"),

    PARAM_INVALID(1201, "参数校验失败"),
    PARAM_MISSING(1202, "必要参数缺失");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
