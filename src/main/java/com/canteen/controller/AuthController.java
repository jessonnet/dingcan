package com.canteen.controller;

import com.canteen.entity.User;
import com.canteen.service.UserService;
import com.canteen.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 登录
     * @param loginData 登录数据
     * @return 结果
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> result = new HashMap<>();
        
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        // 根据用户名查询用户
        User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUsername());
        
        // 获取用户角色
        String roleName = userService.getRoleNameByUserId(user.getId());
        
        // 构建返回数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("role", roleName);
        userInfo.put("department", user.getDepartment());
        
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", userInfo);
        
        return result;
    }

    /**
     * 登出
     * @param request HttpServletRequest
     * @return 结果
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        // 前端需要清除localStorage中的token
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登出成功");
        return result;
    }

}
