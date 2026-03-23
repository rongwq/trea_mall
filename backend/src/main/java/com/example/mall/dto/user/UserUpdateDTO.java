package com.example.mall.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UserUpdateDTO {

    @Schema(description = "密码", example = "123456")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    private String password;

    @Schema(description = "邮箱", example = "admin@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "状态", example = "1")
    private Integer status;
}
