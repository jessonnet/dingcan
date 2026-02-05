package com.canteen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.Role;
import com.canteen.entity.User;
import com.canteen.mapper.RoleMapper;
import com.canteen.mapper.UserMapper;
import com.canteen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 根据用户ID获取角色名称
     * @param userId 用户ID
     * @return 角色名称
     */
    @Override
    public String getRoleNameByUserId(Long userId) {
        return roleMapper.selectRoleNameByUserId(userId);
    }

    /**
     * 查询所有用户（排除balance字段）
     * @return 用户列表
     */
    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAllUsers();
    }

    /**
     * 查询所有用户
     * @return 用户列表
     */
    @Override
    public List<User> findAll() {
        return userMapper.selectList(null);
    }

    /**
     * 根据用户名加载用户
     * @param username 用户名
     * @return UserDetails
     * @throws UsernameNotFoundException 用户名未找到异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

}
