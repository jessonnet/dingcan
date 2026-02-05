package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User>, UserDetailsService {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);

    /**
     * 根据用户ID获取角色名称
     * @param userId 用户ID
     * @return 角色名称
     */
    String getRoleNameByUserId(Long userId);

    /**
     * 查询所有用户（排除balance字段）
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> findAll();
}
