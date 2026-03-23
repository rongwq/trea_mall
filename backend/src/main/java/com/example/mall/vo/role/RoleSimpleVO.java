package com.example.mall.vo.role;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色简要信息VO（用于下拉选择等场景）
 */
@Data
public class RoleSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;
}
