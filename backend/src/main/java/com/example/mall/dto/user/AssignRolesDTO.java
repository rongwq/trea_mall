package com.example.mall.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "分配角色请求")
public class AssignRolesDTO {

    @Schema(description = "角色ID列表", required = true)
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
