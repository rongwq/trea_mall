package com.example.mall.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户更新DTO")
public class UserUpdateDTO {
    
    @Schema(description = "邮箱", example = "test@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
    
    @Schema(description = "新密码（可选）", example = "NewPassword123!")
    private String password;
}
