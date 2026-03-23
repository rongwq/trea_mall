package com.example.mall.dto.role;

import com.example.mall.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询参数")
public class RoleQueryDTO extends PageQuery {

    @Schema(description = "角色名称", example = "管理员")
    private String name;

    @Schema(description = "角色编码", example = "ADMIN")
    private String code;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
