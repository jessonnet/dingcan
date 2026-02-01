package com.canteen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.OperationLog;
import com.canteen.entity.Order;
import com.canteen.entity.SystemConfig;
import com.canteen.mapper.OrderMapper;
import com.canteen.mapper.OperationLogMapper;
import com.canteen.mapper.SystemConfigMapper;
import com.canteen.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setOperationType("订餐");
            log.setOperationContent("用户 " + userId + " 预订了 " + order.getOrderDate() + " 的餐食");
            log.setOperationTime(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            operationLogMapper.insert(log);
        }

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

        // 检查是否可以预订
        if (!canOrder(order.getOrderDate())) {
            return false;
        }

        // 修改订单
        order.setUserId(userId);
        order.setStatus(1);
        order.setUpdatedAt(LocalDateTime.now());
        boolean result = orderMapper.updateById(order) > 0;

        // 记录操作日志
        if (result) {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setOperationType("修改订单");
            log.setOperationContent("用户 " + userId + " 修改了订单 " + order.getId());
            log.setOperationTime(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            operationLogMapper.insert(log);
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
        if (!canOrder(existingOrder.getOrderDate())) {
            return false;
        }

        // 删除订单
        existingOrder.setStatus(0);
        existingOrder.setUpdatedAt(LocalDateTime.now());
        boolean result = orderMapper.updateById(existingOrder) > 0;

        // 记录操作日志
        if (result) {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setOperationType("删除订单");
            log.setOperationContent("用户 " + userId + " 删除了订单 " + orderId);
            log.setOperationTime(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            operationLogMapper.insert(log);
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

}
