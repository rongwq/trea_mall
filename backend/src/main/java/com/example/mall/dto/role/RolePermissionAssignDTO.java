package com.example.mall.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "分配权限参数")
public class RolePermissionAssignDTO {

    @NotNull(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> permissionIds;
}
