package com.canteen.controller;

import com.canteen.annotation.Log;
import com.canteen.entity.User;
import com.canteen.service.UserService;
import com.canteen.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Map<String, Object> login(@RequestBody Map<String, Object> loginData) {
        Map<String, Object> result = new HashMap<>();
        
        String username = loginData.get("username") != null ? loginData.get("username").toString() : null;
        String password = loginData.get("password") != null ? loginData.get("password").toString() : null;
        
        System.out.println("登录请求 - 用户名: " + username + ", 密码: " + (password != null ? "已提供" : "未提供"));
        
        // 根据用户名查询用户
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println("登录失败 - 用户不存在: " + username);
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        System.out.println("找到用户 - ID: " + user.getId() + ", 用户名: " + user.getUsername());
        System.out.println("数据库密码: " + user.getPassword());
        System.out.println("输入密码: " + password);
        
        // 验证密码
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("密码验证结果: " + passwordMatches);
        
        if (!passwordMatches) {
            System.out.println("登录失败 - 密码不匹配");
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
        userInfo.put("department", user.getDepartmentName());
        
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", userInfo);
        
        return result;
    }

    /**
     * 修改密码
     * @param passwordData 密码数据
     * @param request HttpServletRequest
     * @return 结果
     */
    @Log(module = "认证管理", description = "修改密码", operationType = "UPDATE")
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> passwordData, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        // 参数校验
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入当前密码");
            return result;
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入新密码");
            return result;
        }
        
        if (newPassword.length() < 6) {
            result.put("success", false);
            result.put("message", "新密码长度不能少于6位");
            return result;
        }
        
        // 从JWT令牌中获取用户名
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            result.put("success", false);
            result.put("message", "未授权访问");
            return result;
        }
        
        token = token.substring(7);
        String username = jwtUtils.getUsernameFromToken(token);
        
        // 根据用户名查询用户
        User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            result.put("success", false);
            result.put("message", "当前密码错误");
            return result;
        }
        
        // 更新密码
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        boolean updateSuccess = userService.updateById(user);
        
        if (updateSuccess) {
            result.put("success", true);
            result.put("message", "密码修改成功");
        } else {
            result.put("success", false);
            result.put("message", "密码修改失败");
        }
        
        return result;
    }

    /**
     * 登出
     * @param request HttpServletRequest
     * @return 结果
     */
    @Log(module = "认证管理", description = "用户登出", operationType = "LOGOUT")
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        // 前端需要清除localStorage中的token
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登出成功");
        return result;
    }

    /**
     * 临时密码重置接口（仅用于调试）
     * @return 结果
     */
    @PostMapping("/reset-passwords")
    public Map<String, Object> resetAllPasswords() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 重置admin用户密码
            User admin = userService.findByUsername("admin");
            if (admin != null) {
                String newPassword = passwordEncoder.encode("123456");
                admin.setPassword(newPassword);
                userService.updateById(admin);
                System.out.println("重置admin密码成功: " + newPassword);
            }
            
            // 重置wang用户密码
            User wang = userService.findByUsername("wang");
            if (wang != null) {
                String newPassword = passwordEncoder.encode("123456");
                wang.setPassword(newPassword);
                userService.updateById(wang);
                System.out.println("重置wang密码成功: " + newPassword);
            }
            
            result.put("success", true);
            result.put("message", "密码重置成功，所有用户密码已重置为123456");
        } catch (Exception e) {
            System.out.println("密码重置失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "密码重置失败: " + e.getMessage());
        }
        
        return result;
    }

}
