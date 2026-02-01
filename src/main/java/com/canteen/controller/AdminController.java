package com.canteen.controller;

import com.canteen.entity.MealType;
import com.canteen.entity.Role;
import com.canteen.entity.SystemConfig;
import com.canteen.entity.User;
import com.canteen.service.MealTypeService;
import com.canteen.service.RoleService;
import com.canteen.service.SystemConfigService;
import com.canteen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MealTypeService mealTypeService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 餐食类型管理 ====================

    /**
     * 获取餐食类型列表
     * @return 结果
     */
    @GetMapping("/meal-type/list")
    public Map<String, Object> getMealTypeList() {
        Map<String, Object> result = new HashMap<>();
        List<MealType> mealTypes = mealTypeService.list();
        result.put("success", true);
        result.put("data", mealTypes);
        return result;
    }

    /**
     * 添加餐食类型
     * @param mealType 餐食类型信息
     * @return 结果
     */
    @PostMapping("/meal-type/add")
    public Map<String, Object> addMealType(@RequestBody MealType mealType) {
        Map<String, Object> result = new HashMap<>();
        boolean success = mealTypeService.save(mealType);
        result.put("success", success);
        result.put("message", success ? "添加成功" : "添加失败");
        return result;
    }

    /**
     * 修改餐食类型
     * @param mealType 餐食类型信息
     * @return 结果
     */
    @PutMapping("/meal-type/update")
    public Map<String, Object> updateMealType(@RequestBody MealType mealType) {
        Map<String, Object> result = new HashMap<>();
        boolean success = mealTypeService.updateById(mealType);
        result.put("success", success);
        result.put("message", success ? "修改成功" : "修改失败");
        return result;
    }

    /**
     * 删除餐食类型
     * @param id 餐食类型ID
     * @return 结果
     */
    @DeleteMapping("/meal-type/delete/{id}")
    public Map<String, Object> deleteMealType(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = mealTypeService.removeById(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

    // ==================== 系统配置管理 ====================

    /**
     * 获取系统配置
     * @return 结果
     */
    @GetMapping("/system-config/list")
    public Map<String, Object> getSystemConfigList() {
        Map<String, Object> result = new HashMap<>();
        List<SystemConfig> systemConfigs = systemConfigService.list();
        result.put("success", true);
        result.put("data", systemConfigs);
        return result;
    }

    /**
     * 更新系统配置
     * @param systemConfig 系统配置信息
     * @return 结果
     */
    @PutMapping("/system-config/update")
    public Map<String, Object> updateSystemConfig(@RequestBody SystemConfig systemConfig) {
        Map<String, Object> result = new HashMap<>();
        boolean success = systemConfigService.updateById(systemConfig);
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败");
        return result;
    }

    // ==================== 员工管理 ====================

    /**
     * 获取员工列表
     * @return 结果
     */
    @GetMapping("/user/list")
    public Map<String, Object> getUserList() {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userService.list();
        // 关联角色信息
        for (User user : users) {
            Role role = roleService.getById(user.getRoleId());
            user.setRoleName(role != null ? role.getName() : "");
        }
        result.put("success", true);
        result.put("data", users);
        return result;
    }

    /**
     * 添加员工
     * @param user 员工信息
     * @return 结果
     */
    @PostMapping("/user/add")
    public Map<String, Object> addUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean success = userService.save(user);
        result.put("success", success);
        result.put("message", success ? "添加成功" : "添加失败");
        return result;
    }

    /**
     * 修改员工
     * @param user 员工信息
     * @return 结果
     */
    @PutMapping("/user/update")
    public Map<String, Object> updateUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        // 如果修改密码，则加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        boolean success = userService.updateById(user);
        result.put("success", success);
        result.put("message", success ? "修改成功" : "修改失败");
        return result;
    }

    /**
     * 删除员工
     * @param id 员工ID
     * @return 结果
     */
    @DeleteMapping("/user/delete/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = userService.removeById(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

    // ==================== 角色管理 ====================

    /**
     * 获取角色列表
     * @return 结果
     */
    @GetMapping("/role/list")
    public Map<String, Object> getRoleList() {
        Map<String, Object> result = new HashMap<>();
        List<Role> roles = roleService.list();
        result.put("success", true);
        result.put("data", roles);
        return result;
    }

    /**
     * 添加角色
     * @param role 角色信息
     * @return 结果
     */
    @PostMapping("/role/add")
    public Map<String, Object> addRole(@RequestBody Role role) {
        Map<String, Object> result = new HashMap<>();
        boolean success = roleService.save(role);
        result.put("success", success);
        result.put("message", success ? "添加成功" : "添加失败");
        return result;
    }

    /**
     * 修改角色
     * @param role 角色信息
     * @return 结果
     */
    @PutMapping("/role/update")
    public Map<String, Object> updateRole(@RequestBody Role role) {
        Map<String, Object> result = new HashMap<>();
        boolean success = roleService.updateById(role);
        result.put("success", success);
        result.put("message", success ? "修改成功" : "修改失败");
        return result;
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 结果
     */
    @DeleteMapping("/role/delete/{id}")
    public Map<String, Object> deleteRole(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        // 检查是否有用户使用该角色
        List<User> users = userService.list();
        for (User user : users) {
            if (user.getRoleId().equals(id)) {
                result.put("success", false);
                result.put("message", "删除失败，该角色已被使用");
                return result;
            }
        }
        boolean success = roleService.removeById(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

}
