package com.example.mall.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.dto.UserCreateDTO;
import com.example.mall.dto.UserQueryDTO;
import com.example.mall.dto.UserRoleAssignDTO;
import com.example.mall.dto.UserUpdateDTO;
import com.example.mall.service.UserService;
import com.example.mall.vo.PageVO;
import com.example.mall.vo.UserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * RESTful API 设计规范
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @GetMapping
    public PageResult<UserVO> list(@Valid UserQueryDTO queryDTO) {
        log.info("查询用户列表, 参数: {}", queryDTO);
        PageVO<UserVO> pageVO = userService.queryUserPage(queryDTO);
        return PageResult.success(pageVO);
    }

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        log.info("查询用户详情, id: {}", id);
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 创建用户
     *
     * @param createDTO 用户创建参数
     * @return 创建结果
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid UserCreateDTO createDTO) {
        log.info("创建用户, username: {}", createDTO.getUsername());
        Long userId = userService.createUser(createDTO);
        return Result.success("创建成功", userId);
    }

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 用户更新参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable @NotNull(message = "用户ID不能为空") Long id,
                               @RequestBody @Valid UserUpdateDTO updateDTO) {
        log.info("更新用户, id: {}", id);
        userService.updateUser(id, updateDTO);
        return Result.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        log.info("删除用户, id: {}", id);
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 删除结果
     */
    @DeleteMapping
    public Result<Void> batchDelete(@RequestParam @NotNull(message = "用户ID列表不能为空") List<Long> ids) {
        log.info("批量删除用户, ids: {}", ids);
        userService.batchDeleteUser(ids);
        return Result.success();
    }

    /**
     * 启用用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        log.info("启用用户, id: {}", id);
        userService.enableUser(id);
        return Result.success();
    }

    /**
     * 禁用用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        log.info("禁用用户, id: {}", id);
        userService.disableUser(id);
        return Result.success();
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        log.info("重置用户密码, id: {}", id);
        String newPassword = userService.resetPassword(id);
        return Result.success("密码重置成功", newPassword);
    }

    /**
     * 分配用户角色
     *
     * @param id               用户ID
     * @param roleAssignDTO 角色分配参数
     * @return 操作结果
     */
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable @NotNull(message = "用户ID不能为空") Long id,
                                    @RequestBody @Valid UserRoleAssignDTO roleAssignDTO) {
        log.info("分配用户角色, userId: {}, roleIds: {}", id, roleAssignDTO.getRoleIds());
        userService.assignRoles(id, roleAssignDTO.getRoleIds());
        return Result.success();
    }

    /**
     * 获取用户角色列表
     *
     * @param id 用户ID
     * @return 角色ID列表
     */
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable @NotNull(message = "用户ID不能为空") Long id) {
        log.info("获取用户角色列表, userId: {}", id);
        List<Long> roleIds = userService.getUserRoleIds(id);
        return Result.success(roleIds);
    }
}
