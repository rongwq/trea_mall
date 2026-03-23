package com.example.mall.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建角色参数")
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过50个字符")
    @Schema(description = "角色名称", example = "商品管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码不能超过50个字符")
    @Pattern(regexp = "^[A-Z_]+$", message = "角色编码只能包含大写字母和下划线")
    @Schema(description = "角色编码", example = "PRODUCT_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Size(max = 200, message = "角色描述不能超过200个字符")
    @Schema(description = "角色描述", example = "负责商品管理的管理员角色")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;
}
