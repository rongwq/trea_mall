package com.example.mall.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class RoleUpdateDTO implements Serializable {
    @NotNull(message = "角色ID不能为空")
    private Long id;

    @Size(min = 1, max = 50, message = "角色名称长度必须在1-50个字符之间")
    private String name;

    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    private Integer status;
}
