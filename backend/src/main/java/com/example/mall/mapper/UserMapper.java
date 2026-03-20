package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper extends BaseMapper<User> {
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
