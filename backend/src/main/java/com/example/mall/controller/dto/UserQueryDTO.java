package com.example.mall.controller.dto;

import com.example.mall.common.constant.UserConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "用户查询DTO")
public class UserQueryDTO {
    
    @Schema(description = "用户名（模糊匹配）")
    private String username;
    
    @Schema(description = "邮箱（模糊匹配）")
    private String email;
    
    @Schema(description = "手机号（模糊匹配）")
    private String phone;
    
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
    
    @Schema(description = "页码，默认1", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量，默认10", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能大于100")
    private Integer pageSize = 10;
}
