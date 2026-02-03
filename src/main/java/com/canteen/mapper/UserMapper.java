package com.canteen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(String username);

    /**
     * 查询所有用户（排除balance字段）
     * @return 用户列表
     */
    @Select("SELECT id, username, password, name, role_id, department_id, restaurant_id, phone, email, status, created_at, updated_at FROM user")
    List<User> selectAllUsers();

}
