package com.canteen.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canteen.annotation.Log;
import com.canteen.entity.OperationLog;
import com.canteen.entity.Restaurant;
import com.canteen.entity.User;
import com.canteen.service.OperationLogService;
import com.canteen.service.RestaurantService;
import com.canteen.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/restaurant")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Map<String, Object> getRestaurantList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        Page<Restaurant> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Restaurant> queryWrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(Restaurant::getName, name);
        }
        if (location != null && !location.trim().isEmpty()) {
            queryWrapper.like(Restaurant::getLocation, location);
        }
        
        queryWrapper.orderByDesc(Restaurant::getCreatedAt);
        IPage<Restaurant> pageResult = restaurantService.page(pageParam, queryWrapper);
        
        result.put("success", true);
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @GetMapping("/detail/{id}")
    public Map<String, Object> getRestaurantDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Restaurant restaurant = restaurantService.getById(id);
        
        if (restaurant != null) {
            result.put("success", true);
            result.put("data", restaurant);
        } else {
            result.put("success", false);
            result.put("message", "餐厅不存在");
        }
        return result;
    }

    @PostMapping("/add")
    @Log(module = "餐厅管理", description = "添加餐厅", operationType = "CREATE")
    public Map<String, Object> addRestaurant(@RequestBody Restaurant restaurant, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        String ipAddress = request.getRemoteAddr();
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());
        
        boolean success = restaurantService.save(restaurant);
        if (success) {
            recordOperationLog(user.getId(), username, "添加餐厅", 
                "添加餐厅：" + restaurant.getName() + "，地点：" + restaurant.getLocation(), ipAddress);
            result.put("success", true);
            result.put("message", "添加餐厅成功");
        } else {
            result.put("success", false);
            result.put("message", "添加餐厅失败");
        }
        
        return result;
    }

    @PutMapping("/update")
    @Log(module = "餐厅管理", description = "修改餐厅", operationType = "UPDATE")
    public Map<String, Object> updateRestaurant(@RequestBody Restaurant restaurant, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        String ipAddress = request.getRemoteAddr();
        Restaurant existingRestaurant = restaurantService.getById(restaurant.getId());
        if (existingRestaurant == null) {
            result.put("success", false);
            result.put("message", "餐厅不存在");
            return result;
        }
        
        restaurant.setUpdatedAt(LocalDateTime.now());
        boolean success = restaurantService.updateById(restaurant);
        if (success) {
            recordOperationLog(user.getId(), username, "修改餐厅", 
                "修改餐厅ID：" + restaurant.getId() + "，名称：" + restaurant.getName(), ipAddress);
            result.put("success", true);
            result.put("message", "修改餐厅成功");
        } else {
            result.put("success", false);
            result.put("message", "修改餐厅失败");
        }
        
        return result;
    }

    @DeleteMapping("/delete/{id}")
    @Log(module = "餐厅管理", description = "删除餐厅", operationType = "DELETE")
    public Map<String, Object> deleteRestaurant(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        String ipAddress = request.getRemoteAddr();
        Restaurant restaurant = restaurantService.getById(id);
        if (restaurant == null) {
            result.put("success", false);
            result.put("message", "餐厅不存在");
            return result;
        }
        
        boolean success = restaurantService.removeById(id);
        if (success) {
            recordOperationLog(user.getId(), username, "删除餐厅", 
                "删除餐厅ID：" + id + "，名称：" + restaurant.getName(), ipAddress);
            result.put("success", true);
            result.put("message", "删除餐厅成功");
        } else {
            result.put("success", false);
            result.put("message", "删除餐厅失败");
        }
        
        return result;
    }

    private void recordOperationLog(Long userId, String username, String operationType, String description, String ipAddress) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setOperationType("RESTAURANT");
            log.setModule("餐厅管理");
            log.setDescription(description);
            log.setCreatedAt(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            log.setStatus(1);
            operationLogService.save(log);
        } catch (Exception e) {
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
    }
}
