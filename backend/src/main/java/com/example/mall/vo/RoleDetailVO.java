package com.example.mall.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleDetailVO implements Serializable {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    private List<PermissionVO> permissions;

    @Data
    public static class PermissionVO implements Serializable {
        private Long id;
        private String name;
        private String code;
        private String type;
        private Long parentId;
    }
}
