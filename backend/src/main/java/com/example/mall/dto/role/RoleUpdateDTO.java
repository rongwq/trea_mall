package com.example.mall.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新角色参数")
public class RoleUpdateDTO {

    @Size(max = 50, message = "角色名称不能超过50个字符")
    @Schema(description = "角色名称", example = "商品管理员")
    private String name;

    @Size(max = 200, message = "角色描述不能超过200个字符")
    @Schema(description = "角色描述", example = "负责商品管理的管理员角色")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
