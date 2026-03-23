package com.example.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mall.common.Result;
import com.example.mall.common.ResultCode;
import com.example.mall.dto.user.AssignRolesDTO;
import com.example.mall.dto.user.UserCreateDTO;
import com.example.mall.dto.user.UserQueryDTO;
import com.example.mall.dto.user.UserUpdateDTO;
import com.example.mall.exception.BusinessException;
import com.example.mall.service.UserService;
import com.example.mall.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页查询用户列表")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<IPage<UserVO>> list(@Valid UserQueryDTO queryDTO) {
        IPage<UserVO> page = userService.queryUsers(queryDTO);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户详情")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<UserVO> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getUserWithRolesById(id)
                .map(Result::success)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<UserVO> create(@Valid @RequestBody UserCreateDTO createDTO) {
        UserVO userVO = userService.createUser(createDTO);
        return Result.success(userVO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<UserVO> update(@Parameter(description = "用户ID") @PathVariable Long id,
                                 @Valid @RequestBody UserUpdateDTO updateDTO) {
        UserVO userVO = userService.updateUser(id, updateDTO);
        return Result.success(userVO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> deleteBatch(@Parameter(description = "用户ID列表") @RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID列表不能为空");
        }
        userService.deleteUsers(ids);
        return Result.success();
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "分配用户角色")
    @PreAuthorize("hasAuthority('system:user:role')")
    public Result<Void> assignRoles(@Parameter(description = "用户ID") @PathVariable Long id,
                                    @Valid @RequestBody AssignRolesDTO assignRolesDTO) {
        userService.assignRoles(id, assignRolesDTO);
        return Result.success();
    }
}
