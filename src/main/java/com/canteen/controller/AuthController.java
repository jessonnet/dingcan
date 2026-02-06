package com.canteen.controller;

import com.canteen.annotation.Log;
import com.canteen.entity.Department;
import com.canteen.entity.User;
import com.canteen.mapper.DepartmentMapper;
import com.canteen.service.UserService;
import com.canteen.service.LoginSecurityService;
import com.canteen.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private LoginSecurityService loginSecurityService;

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
        
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入用户名");
            return result;
        }
        
        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入密码");
            return result;
        }
        
        if (loginSecurityService.isAccountLocked(username)) {
            result.put("success", false);
            result.put("message", "账户已被锁定，请30分钟后再试");
            return result;
        }
        
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println("登录失败 - 用户不存在: " + username);
            loginSecurityService.recordLoginAttempt(username, false);
            int remainingAttempts = loginSecurityService.getRemainingAttempts(username);
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            result.put("remainingAttempts", remainingAttempts);
            return result;
        }
        
        System.out.println("找到用户 - ID: " + user.getId() + ", 用户名: " + user.getUsername());
        System.out.println("数据库密码: " + user.getPassword());
        System.out.println("输入密码: " + password);
        
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("密码验证结果: " + passwordMatches);
        
        if (!passwordMatches) {
            System.out.println("登录失败 - 密码不匹配");
            loginSecurityService.recordLoginAttempt(username, false);
            int remainingAttempts = loginSecurityService.getRemainingAttempts(username);
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            result.put("remainingAttempts", remainingAttempts);
            return result;
        }
        
        loginSecurityService.recordLoginAttempt(username, true);
        
        String token = jwtUtils.generateToken(user.getUsername());
        
        String roleName = userService.getRoleNameByUserId(user.getId());
        
        String departmentName = "";
        if (user.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(user.getDepartmentId());
            if (department != null) {
                departmentName = department.getName();
            }
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("role", roleName);
        userInfo.put("department", departmentName);
        userInfo.put("departmentId", user.getDepartmentId());
        userInfo.put("restaurantId", user.getRestaurantId());
        
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", userInfo);
        
        return result;
    }

    /**
     * 获取当前用户信息
     * @param request HttpServletRequest
     * @return 用户信息
     */
    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
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
        
        // 获取用户角色
        String roleName = userService.getRoleNameByUserId(user.getId());
        
        // 获取部门名称
        String departmentName = "";
        if (user.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(user.getDepartmentId());
            if (department != null) {
                departmentName = department.getName();
            }
        }
        
        // 构建返回数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("role", roleName);
        userInfo.put("department", departmentName);
        userInfo.put("departmentId", user.getDepartmentId());
        userInfo.put("restaurantId", user.getRestaurantId());
        
        result.put("success", true);
        result.put("data", userInfo);
        
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
        
        if (!loginSecurityService.validatePasswordStrength(newPassword)) {
            result.put("success", false);
            result.put("message", "新密码必须包含字母和数字");
            return result;
        }
        
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            result.put("success", false);
            result.put("message", "未授权访问");
            return result;
        }
        
        token = token.substring(7);
        String username = jwtUtils.getUsernameFromToken(token);
        
        User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            result.put("success", false);
            result.put("message", "当前密码错误");
            return result;
        }
        
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
            String[] usernames = {"admin", "employee1", "employee2", "chef1"};
            int resetCount = 0;
            
            for (String username : usernames) {
                User user = userService.findByUsername(username);
                if (user != null) {
                    String newPassword = passwordEncoder.encode("123456");
                    user.setPassword(newPassword);
                    userService.updateById(user);
                    System.out.println("重置" + username + "密码成功: " + newPassword);
                    resetCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", "密码重置成功，已重置" + resetCount + "个用户密码为123456");
        } catch (Exception e) {
            System.out.println("密码重置失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "密码重置失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 测试密码哈希（仅用于调试）
     * @param testData 测试数据
     * @return 结果
     */
    @PostMapping("/test-password")
    public Map<String, Object> testPassword(@RequestBody Map<String, String> testData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String username = testData.get("username");
            String password = testData.get("password");
            
            if (username == null || password == null) {
                result.put("success", false);
                result.put("message", "请提供用户名和密码");
                return result;
            }
            
            User user = userService.findByUsername(username);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            
            String dbPassword = user.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            boolean matches = passwordEncoder.matches(password, dbPassword);
            
            result.put("success", true);
            result.put("username", username);
            result.put("inputPassword", password);
            result.put("dbPassword", dbPassword);
            result.put("encodedPassword", encodedPassword);
            result.put("passwordMatches", matches);
            result.put("message", matches ? "密码匹配成功" : "密码不匹配");
            
            System.out.println("密码测试结果:");
            System.out.println("用户名: " + username);
            System.out.println("输入密码: " + password);
            System.out.println("数据库密码: " + dbPassword);
            System.out.println("新编码密码: " + encodedPassword);
            System.out.println("匹配结果: " + matches);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

}
