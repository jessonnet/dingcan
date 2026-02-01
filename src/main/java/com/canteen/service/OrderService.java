package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.dto.AggregatedOrderDTO;
import com.canteen.dto.OrderDTO;
import com.canteen.entity.Order;
import jakarta.servlet.http.HttpServletResponse;

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

    // ==================== 管理员订单管理接口 ====================

    /**
     * 获取订单列表（支持分页、筛选、排序）
     * @param page 页码
     * @param pageSize 每页大小
     * @param status 订单状态
     * @param userName 用户姓名
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param sortField 排序字段
     * @param sortOrder 排序方式
     * @return 订单列表和总数
     */
    Map<String, Object> getOrderList(int page, int pageSize, Integer status, String userName,
                                     String startDate, String endDate, String sortField, String sortOrder);

    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单DTO
     */
    OrderDTO getOrderDetail(Long id);

    /**
     * 管理员修改订单
     * @param order 订单信息
     * @return 是否成功
     */
    boolean updateOrderByAdmin(Order order);

    /**
     * 管理员删除订单
     * @param id 订单ID
     * @return 是否成功
     */
    boolean deleteOrderByAdmin(Long id);

    /**
     * 导出订单数据
     * @param status 订单状态
     * @param userName 用户姓名
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param response HttpServletResponse
     */
    void exportOrders(Integer status, String userName, String startDate, String endDate, HttpServletResponse response);

}
