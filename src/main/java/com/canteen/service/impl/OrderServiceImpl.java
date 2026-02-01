package com.canteen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.dto.AggregatedOrderDTO;
import com.canteen.entity.OperationLog;
import com.canteen.entity.Order;
import com.canteen.entity.SystemConfig;
import com.canteen.mapper.OrderMapper;
import com.canteen.mapper.OperationLogMapper;
import com.canteen.mapper.SystemConfigMapper;
import com.canteen.service.OrderService;
import com.canteen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private UserService userService;

    /**
     * 创建订单
     * @param order 订单信息
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 是否成功
     */
    @Override
    public boolean createOrder(Order order, Long userId, String ipAddress) {
        // 检查是否可以预订
        if (!canOrder(order.getOrderDate())) {
            return false;
        }

        // 检查是否已经预订同一种餐食类型
        List<Order> existingOrders = orderMapper.selectByUserIdAndDate(userId, order.getOrderDate());
        for (Order existingOrder : existingOrders) {
            if (existingOrder.getMealTypeId().equals(order.getMealTypeId())) {
                return false;
            }
        }

        // 创建订单
        order.setUserId(userId);
        order.setStatus(1);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        boolean result = orderMapper.insert(order) > 0;

        // 记录操作日志
        if (result) {
            try {
                OperationLog log = new OperationLog();
                log.setUserId(userId);
                // 获取用户名
                com.canteen.entity.User user = userService.getById(userId);
                log.setUsername(user != null ? user.getUsername() : String.valueOf(userId));
                log.setOperationType("ORDER");
                log.setModule("订单管理");
                log.setDescription("用户预订了 " + order.getOrderDate() + " 的餐食");
                log.setCreatedAt(LocalDateTime.now());
                log.setIpAddress(ipAddress);
                log.setStatus(1);
                operationLogMapper.insert(log);
            } catch (Exception e) {
                // 日志记录失败不影响主业务流程
                System.err.println("记录操作日志失败: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 批量创建订单
     * @param mealTypeId 餐食类型ID
     * @param orderDates 订单日期列表
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 创建结果（成功数量和失败信息）
     */
    @Override
    public Map<String, Object> batchCreateOrders(Long mealTypeId, List<LocalDate> orderDates, Long userId, String ipAddress) {
        Map<String, Object> result = new java.util.HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> failMessages = new java.util.ArrayList<>();

        for (LocalDate orderDate : orderDates) {
            // 检查是否可以预订
            if (!canOrder(orderDate)) {
                failCount++;
                failMessages.add(orderDate + ": 超过预订时间");
                continue;
            }

            // 检查是否已经预订同一种餐食类型
            List<Order> existingOrders = orderMapper.selectByUserIdAndDate(userId, orderDate);
            boolean alreadyOrdered = false;
            for (Order existingOrder : existingOrders) {
                if (existingOrder.getMealTypeId().equals(mealTypeId)) {
                    alreadyOrdered = true;
                    break;
                }
            }

            if (alreadyOrdered) {
                failCount++;
                failMessages.add(orderDate + ": 已预订该餐食类型");
                continue;
            }

            // 创建订单
            Order order = new Order();
            order.setMealTypeId(mealTypeId);
            order.setOrderDate(orderDate);
            order.setUserId(userId);
            order.setStatus(1);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            boolean insertResult = orderMapper.insert(order) > 0;
            if (insertResult) {
                successCount++;
            } else {
                failCount++;
                failMessages.add(orderDate + ": 创建订单失败");
            }
        }

        // 记录操作日志
        if (successCount > 0) {
            try {
                OperationLog log = new OperationLog();
                log.setUserId(userId);
                // 获取用户名
                com.canteen.entity.User user = userService.getById(userId);
                log.setUsername(user != null ? user.getUsername() : String.valueOf(userId));
                log.setOperationType("ORDER");
                log.setModule("订单管理");
                log.setDescription("用户批量预订了 " + successCount + " 天的餐食");
                log.setCreatedAt(LocalDateTime.now());
                log.setIpAddress(ipAddress);
                log.setStatus(1);
                operationLogMapper.insert(log);
            } catch (Exception e) {
                // 日志记录失败不影响主业务流程
                System.err.println("记录操作日志失败: " + e.getMessage());
            }
        }

        result.put("success", successCount > 0);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failMessages", failMessages);

        return result;
    }

    /**
     * 修改订单
     * @param order 订单信息
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 是否成功
     */
    @Override
    public boolean updateOrder(Order order, Long userId, String ipAddress) {
        // 检查是否可以修改
        Order existingOrder = orderMapper.selectById(order.getId());
        if (existingOrder == null || !existingOrder.getUserId().equals(userId)) {
            return false;
        }

        // 检查是否可以修改
        if (!canDeleteOrder(order.getOrderDate())) {
            return false;
        }

        // 修改订单
        order.setUserId(userId);
        order.setStatus(1);
        order.setUpdatedAt(LocalDateTime.now());
        boolean result = orderMapper.updateById(order) > 0;

        // 记录操作日志
        if (result) {
            try {
                OperationLog log = new OperationLog();
                log.setUserId(userId);
                // 获取用户名
                com.canteen.entity.User user = userService.getById(userId);
                log.setUsername(user != null ? user.getUsername() : String.valueOf(userId));
                log.setOperationType("UPDATE");
                log.setModule("订单管理");
                log.setDescription("用户修改了订单 " + order.getId());
                log.setCreatedAt(LocalDateTime.now());
                log.setIpAddress(ipAddress);
                log.setStatus(1);
                operationLogMapper.insert(log);
            } catch (Exception e) {
                // 日志记录失败不影响主业务流程
                System.err.println("记录操作日志失败: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 删除订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 是否成功
     */
    @Override
    public boolean deleteOrder(Long orderId, Long userId, String ipAddress) {
        // 检查订单是否存在
        Order existingOrder = orderMapper.selectById(orderId);
        if (existingOrder == null || !existingOrder.getUserId().equals(userId)) {
            return false;
        }

        // 检查是否可以删除
        if (!canDeleteOrder(existingOrder.getOrderDate())) {
            return false;
        }

        // 删除订单
        existingOrder.setStatus(0);
        existingOrder.setUpdatedAt(LocalDateTime.now());
        boolean result = orderMapper.updateById(existingOrder) > 0;

        // 记录操作日志
        if (result) {
            try {
                OperationLog log = new OperationLog();
                log.setUserId(userId);
                // 获取用户名
                com.canteen.entity.User user = userService.getById(userId);
                log.setUsername(user != null ? user.getUsername() : String.valueOf(userId));
                log.setOperationType("DELETE");
                log.setModule("订单管理");
                log.setDescription("用户删除了订单 " + orderId);
                log.setCreatedAt(LocalDateTime.now());
                log.setIpAddress(ipAddress);
                log.setStatus(1);
                operationLogMapper.insert(log);
            } catch (Exception e) {
                // 日志记录失败不影响主业务流程
                System.err.println("记录操作日志失败: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 根据用户ID和日期查询订单
     * @param userId 用户ID
     * @param orderDate 订单日期
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersByUserIdAndDate(Long userId, LocalDate orderDate) {
        return orderMapper.selectByUserIdAndDate(userId, orderDate);
    }

    /**
     * 根据用户ID查询订单历史
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    @Override
    public List<Order> getOrderHistoryByUserId(Long userId, int page, int size) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("order_date");
        queryWrapper.last("LIMIT " + (page - 1) * size + ", " + size);
        return orderMapper.selectList(queryWrapper);
    }

    /**
     * 根据日期查询订单
     * @param orderDate 订单日期
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersByDate(LocalDate orderDate) {
        return orderMapper.selectByDate(orderDate);
    }

    /**
     * 检查是否可以预订指定日期的餐食
     * @param orderDate 订单日期
     * @return 是否可以预订
     */
    @Override
    public boolean canOrder(LocalDate orderDate) {
        // 获取当前日期和时间
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 检查是否是明天及以后的日期
        if (orderDate.isBefore(today.plusDays(1))) {
            return false;
        }

        // 检查是否超过锁单时间
        if (orderDate.equals(today.plusDays(1))) {
            LocalTime lockTime = getLockTime();
            LocalTime currentTime = now.toLocalTime();
            if (currentTime.isAfter(lockTime)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取锁单时间
     * @return 锁单时间
     */
    private LocalTime getLockTime() {
        SystemConfig config = systemConfigMapper.selectByConfigKey("lock_time");
        if (config == null) {
            // 默认锁单时间为16:00
            return LocalTime.of(16, 0);
        }
        String lockTimeStr = config.getConfigValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(lockTimeStr, formatter);
    }

    /**
     * 根据用户ID查询聚合订单历史
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 聚合订单列表
     */
    @Override
    public List<AggregatedOrderDTO> getAggregatedOrderHistoryByUserId(Long userId, int page, int size) {
        // 查询所有有效订单（status = 1）
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("order_date");
        
        List<Order> allOrders = orderMapper.selectList(queryWrapper);
        
        // 按日期分组
        Map<LocalDate, List<Order>> groupedByDate = allOrders.stream()
            .collect(Collectors.groupingBy(Order::getOrderDate));
        
        // 转换为聚合订单列表
        List<AggregatedOrderDTO> aggregatedOrders = new ArrayList<>();
        
        for (Map.Entry<LocalDate, List<Order>> entry : groupedByDate.entrySet()) {
            LocalDate orderDate = entry.getKey();
            List<Order> orders = entry.getValue();
            
            AggregatedOrderDTO aggregatedOrder = new AggregatedOrderDTO();
            aggregatedOrder.setOrderDate(orderDate);
            
            // 构建订单详情列表
            List<AggregatedOrderDTO.OrderDetail> orderDetails = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (Order order : orders) {
                AggregatedOrderDTO.OrderDetail detail = new AggregatedOrderDTO.OrderDetail();
                detail.setId(order.getId());
                detail.setMealTypeId(order.getMealTypeId());
                detail.setPrice(BigDecimal.ZERO); // 价格需要从餐食类型获取
                detail.setStatus(order.getStatus());
                detail.setStatusText(order.getStatus() == 1 ? "有效" : "无效");
                detail.setCreatedAt(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                orderDetails.add(detail);
            }
            
            aggregatedOrder.setOrders(orderDetails);
            aggregatedOrder.setTotalCount(orders.size());
            aggregatedOrder.setTotalAmount(totalAmount);
            
            aggregatedOrders.add(aggregatedOrder);
        }
        
        // 按日期降序排序
        aggregatedOrders.sort(Comparator.comparing(AggregatedOrderDTO::getOrderDate).reversed());
        
        // 分页处理
        int start = (page - 1) * size;
        int end = Math.min(start + size, aggregatedOrders.size());
        
        if (start >= aggregatedOrders.size()) {
            return new ArrayList<>();
        }
        
        return aggregatedOrders.subList(start, end);
    }

    /**
     * 检查是否可以删除指定日期的订单
     * @param orderDate 订单日期
     * @return 是否可以删除
     */
    @Override
    public boolean canDeleteOrder(LocalDate orderDate) {
        // 获取当前日期和时间
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 不能删除今天或以前的订单
        if (orderDate.isBefore(today) || orderDate.isEqual(today)) {
            return false;
        }

        // 如果是明天的订单，需要检查是否在锁定时间之前
        if (orderDate.equals(today.plusDays(1))) {
            LocalTime lockTime = getLockTime();
            LocalTime currentTime = now.toLocalTime();
            // 如果当前时间已经过了锁定时间，则不能删除明天的订单
            if (currentTime.isAfter(lockTime)) {
                return false;
            }
        }

        // 可以删除后天及以后的订单
        return true;
    }

}
