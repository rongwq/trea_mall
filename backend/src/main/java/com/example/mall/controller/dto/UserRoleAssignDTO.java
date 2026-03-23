package com.example.mall.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "用户角色分配DTO")
public class UserRoleAssignDTO {
    
    @Schema(description = "用户ID", required = true, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @Schema(description = "角色ID列表", required = true)
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
