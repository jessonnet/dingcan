package com.canteen.controller;

import com.canteen.annotation.Log;
import com.canteen.entity.Department;
import com.canteen.entity.MealType;
import com.canteen.entity.Restaurant;
import com.canteen.entity.Role;
import com.canteen.entity.SystemConfig;
import com.canteen.entity.User;
import com.canteen.service.DepartmentService;
import com.canteen.service.MealTypeService;
import com.canteen.service.OrderService;
import com.canteen.service.RestaurantService;
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

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RestaurantService restaurantService;

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
    @Log(module = "餐食管理", description = "添加餐食类型", operationType = "CREATE")
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
    @Log(module = "餐食管理", description = "修改餐食类型", operationType = "UPDATE")
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
    @Log(module = "餐食管理", description = "删除餐食类型", operationType = "DELETE")
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
        List<User> users = userService.getAllUsers();
        // 关联角色、部门和餐厅信息
        for (User user : users) {
            Role role = roleService.getById(user.getRoleId());
            user.setRoleName(role != null ? role.getName() : "");
            if (user.getDepartmentId() != null) {
                Department department = departmentService.getById(user.getDepartmentId());
                user.setDepartmentName(department != null ? department.getName() : "");
            }
            if (user.getRestaurantId() != null) {
                Restaurant restaurant = restaurantService.getById(user.getRestaurantId());
                user.setRestaurantName(restaurant != null ? restaurant.getName() : "");
            }
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
    @Log(module = "用户管理", description = "添加员工", operationType = "CREATE")
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
    @Log(module = "用户管理", description = "修改员工", operationType = "UPDATE")
    @PutMapping("/user/update")
    public Map<String, Object> updateUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        // 如果密码为空或null，设置为null，不更新密码字段
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(null);
        } else {
            // 如果修改密码，则加密
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
    @Log(module = "用户管理", description = "删除员工", operationType = "DELETE")
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
    @Log(module = "角色管理", description = "添加角色", operationType = "CREATE")
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
    @Log(module = "角色管理", description = "修改角色", operationType = "UPDATE")
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

    // ==================== 订单管理 ====================

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表（支持分页、筛选、排序）
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 订单状态
     * @param userName 用户姓名
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param sortField 排序字段
     * @param sortOrder 排序方式（asc/desc）
     * @return 结果
     */
    @GetMapping("/order/list")
    public Map<String, Object> getOrderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> data = orderService.getOrderList(page, pageSize, status, userName, 
                    startDate, endDate, sortField, sortOrder);
            result.put("success", true);
            result.put("data", data.get("list"));
            result.put("total", data.get("total"));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取订单列表失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 结果
     */
    @GetMapping("/order/detail/{id}")
    public Map<String, Object> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        com.canteen.dto.OrderDTO orderDTO = orderService.getOrderDetail(id);
        if (orderDTO != null) {
            result.put("success", true);
            result.put("data", orderDTO);
        } else {
            result.put("success", false);
            result.put("message", "订单不存在");
        }
        
        return result;
    }

    /**
     * 修改订单
     * @param order 订单信息
     * @return 结果
     */
    @Log(module = "订单管理", description = "修改订单", operationType = "UPDATE")
    @PutMapping("/order/update")
    public Map<String, Object> updateOrder(@RequestBody com.canteen.entity.Order order) {
        Map<String, Object> result = new HashMap<>();
        boolean success = orderService.updateOrderByAdmin(order);
        result.put("success", success);
        result.put("message", success ? "修改成功" : "修改失败");
        return result;
    }

    /**
     * 删除订单
     * @param id 订单ID
     * @return 结果
     */
    @Log(module = "订单管理", description = "删除订单", operationType = "DELETE")
    @DeleteMapping("/order/delete/{id}")
    public Map<String, Object> deleteOrder(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = orderService.deleteOrderByAdmin(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

    /**
     * 导出订单数据
     * @param status 订单状态
     * @param userName 用户姓名
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param response HttpServletResponse
     */
    @GetMapping("/order/export")
    public void exportOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) {
        orderService.exportOrders(status, userName, startDate, endDate, response);
    }

}
