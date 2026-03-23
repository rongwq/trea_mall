package com.example.mall.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户信息响应")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "角色列表")
    private List<RoleVO> roles;

    @Data
    @Schema(description = "角色信息")
    public static class RoleVO {

        @Schema(description = "角色ID")
        private Long id;

        @Schema(description = "角色名称")
        private String name;

        @Schema(description = "角色编码")
        private String code;
    }
}
