package com.canteen.controller;

import com.canteen.entity.Order;
import com.canteen.entity.MealType;
import com.canteen.service.OrderService;
import com.canteen.service.UserService;
import com.canteen.service.MealTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private MealTypeService mealTypeService;

    /**
     * 创建订单
     * @param order 订单信息
     * @param request HttpServletRequest
     * @return 结果
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody Order order, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        com.canteen.entity.User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        // 获取IP地址
        String ipAddress = request.getRemoteAddr();

        // 创建订单
        boolean success = orderService.createOrder(order, user.getId(), ipAddress);
        if (success) {
            result.put("success", true);
            result.put("message", "订餐成功");
        } else {
            result.put("success", false);
            result.put("message", "订餐失败，可能已经预订或超过预订时间");
        }

        return result;
    }

    /**
     * 修改订单
     * @param order 订单信息
     * @param request HttpServletRequest
     * @return 结果
     */
    @PutMapping("/update")
    public Map<String, Object> updateOrder(@RequestBody Order order, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        com.canteen.entity.User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        // 获取IP地址
        String ipAddress = request.getRemoteAddr();

        // 修改订单
        boolean success = orderService.updateOrder(order, user.getId(), ipAddress);
        if (success) {
            result.put("success", true);
            result.put("message", "修改成功");
        } else {
            result.put("success", false);
            result.put("message", "修改失败，可能超过修改时间");
        }

        return result;
    }

    /**
     * 删除订单
     * @param orderId 订单ID
     * @param request HttpServletRequest
     * @return 结果
     */
    @DeleteMapping("/delete/{orderId}")
    public Map<String, Object> deleteOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        com.canteen.entity.User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        // 获取IP地址
        String ipAddress = request.getRemoteAddr();

        // 删除订单
        boolean success = orderService.deleteOrder(orderId, user.getId(), ipAddress);
        if (success) {
            result.put("success", true);
            result.put("message", "删除成功");
        } else {
            result.put("success", false);
            result.put("message", "删除失败，可能超过删除时间");
        }

        return result;
    }

    /**
     * 根据日期查询订单
     * @param orderDate 订单日期
     * @return 结果
     */
    @GetMapping("/by-date")
    public Map<String, Object> getOrdersByDate(@RequestParam LocalDate orderDate) {
        Map<String, Object> result = new HashMap<>();

        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        com.canteen.entity.User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        // 查询订单
        List<Order> orders = orderService.getOrdersByUserIdAndDate(user.getId(), orderDate);
        result.put("success", true);
        result.put("data", orders);

        return result;
    }

    /**
     * 查询订单历史
     * @param page 页码
     * @param size 每页大小
     * @return 结果
     */
    @GetMapping("/history")
    public Map<String, Object> getOrderHistory(@RequestParam int page, @RequestParam int size) {
        Map<String, Object> result = new HashMap<>();

        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        com.canteen.entity.User user = userService.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        // 查询订单历史
        List<Order> orders = orderService.getOrderHistoryByUserId(user.getId(), page, size);
        result.put("success", true);
        result.put("data", orders);

        return result;
    }

    /**
     * 检查是否可以预订
     * @param orderDate 订单日期
     * @return 结果
     */
    @GetMapping("/can-order")
    public Map<String, Object> canOrder(@RequestParam LocalDate orderDate) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否可以预订
        boolean canOrder = orderService.canOrder(orderDate);
        result.put("success", true);
        result.put("canOrder", canOrder);

        return result;
    }

    /**
     * 获取餐食类型列表
     * @return 结果
     */
    @GetMapping("/meal-types")
    public Map<String, Object> getMealTypes() {
        Map<String, Object> result = new HashMap<>();
        List<MealType> mealTypes = mealTypeService.list();
        result.put("success", true);
        result.put("data", mealTypes);
        return result;
    }

}
