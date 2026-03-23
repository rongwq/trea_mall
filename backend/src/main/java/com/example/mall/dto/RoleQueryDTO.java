package com.example.mall.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleQueryDTO implements Serializable {
    private Integer page = 1;
    private Integer size = 10;
    private String name;
    private String code;
    private Integer status;
}
