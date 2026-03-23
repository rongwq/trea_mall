package com.example.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class RoleCreateDTO implements Serializable {
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 1, max = 50, message = "角色名称长度必须在1-50个字符之间")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(min = 1, max = 50, message = "角色编码长度必须在1-50个字符之间")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "角色编码必须以大写字母开头，只能包含大写字母、数字和下划线")
    private String code;

    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    private Integer status = 1;
}
