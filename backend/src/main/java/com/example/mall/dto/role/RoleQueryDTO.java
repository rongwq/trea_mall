package com.example.mall.dto.role;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 角色查询DTO
 */
@Data
public class RoleQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    @Min(value = 1, message = "页码必须大于0")
    private Long current = 1L;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数必须大于0")
    @Max(value = 100, message = "每页条数不能超过100")
    private Long size = 10L;

    /**
     * 角色名称（模糊查询）
     */
    private String name;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 角色编码
     */
    private String code;
}
