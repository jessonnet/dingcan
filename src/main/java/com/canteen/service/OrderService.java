package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.dto.AggregatedOrderDTO;
import com.canteen.entity.Order;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     * @param order 订单信息
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 是否成功
     */
    boolean createOrder(Order order, Long userId, String ipAddress);

    /**
     * 批量创建订单
     * @param mealTypeId 餐食类型ID
     * @param orderDates 订单日期列表
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 创建结果（成功数量和失败信息）
     */
    Map<String, Object> batchCreateOrders(Long mealTypeId, List<LocalDate> orderDates, Long userId, String ipAddress);

    /**
     * 修改订单
     * @param order 订单信息
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 是否成功
     */
    boolean updateOrder(Order order, Long userId, String ipAddress);

    /**
     * 删除订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 是否成功
     */
    boolean deleteOrder(Long orderId, Long userId, String ipAddress);

    /**
     * 根据用户ID和日期查询订单
     * @param userId 用户ID
     * @param orderDate 订单日期
     * @return 订单列表
     */
    List<Order> getOrdersByUserIdAndDate(Long userId, LocalDate orderDate);

    /**
     * 根据用户ID查询订单历史
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    List<Order> getOrderHistoryByUserId(Long userId, int page, int size);

    /**
     * 根据用户ID查询聚合订单历史
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 聚合订单列表
     */
    List<AggregatedOrderDTO> getAggregatedOrderHistoryByUserId(Long userId, int page, int size);

    /**
     * 根据日期查询订单
     * @param orderDate 订单日期
     * @return 订单列表
     */
    List<Order> getOrdersByDate(LocalDate orderDate);

    /**
     * 检查是否可以预订指定日期的餐食
     * @param orderDate 订单日期
     * @return 是否可以预订
     */
    boolean canOrder(LocalDate orderDate);

    /**
     * 检查是否可以删除指定日期的订单
     * @param orderDate 订单日期
     * @return 是否可以删除
     */
    boolean canDeleteOrder(LocalDate orderDate);

}
