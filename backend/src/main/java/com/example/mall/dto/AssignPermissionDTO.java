package com.example.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AssignPermissionDTO implements Serializable {
    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
}
