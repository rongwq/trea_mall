package com.example.mall.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "权限信息VO")
public class PermissionVO {

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "权限编码")
    private String code;

    @Schema(description = "权限类型：1-菜单，2-按钮")
    private Integer type;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子权限列表")
    private List<PermissionVO> children;
}
