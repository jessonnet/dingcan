package com.canteen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.dto.AggregatedOrderDTO;
import com.canteen.dto.OrderDTO;
import com.canteen.entity.OperationLog;
import com.canteen.entity.Order;
import com.canteen.entity.SystemConfig;
import com.canteen.mapper.OrderMapper;
import com.canteen.mapper.OperationLogMapper;
import com.canteen.mapper.SystemConfigMapper;
import com.canteen.service.OrderService;
import com.canteen.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
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
    public Long createOrder(Order order, Long userId, String ipAddress) {
        // 检查是否可以预订
        if (!canOrder(order.getOrderDate())) {
            return null;
        }

        // 检查是否已经预订同一种餐食类型
        List<Order> existingOrders = orderMapper.selectByUserIdAndDate(userId, order.getOrderDate());
        for (Order existingOrder : existingOrders) {
            if (existingOrder.getMealTypeId().equals(order.getMealTypeId())) {
                return null;
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
            return order.getId();
        }

        return null;
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
     * 批量创建订单（带餐厅ID）
     */
    @Override
    public Map<String, Object> batchCreateOrders(Long mealTypeId, List<LocalDate> orderDates, Long userId, String ipAddress, Long restaurantId) {
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
            order.setRestaurantId(restaurantId);
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
     * 根据用户ID和日期范围查询订单
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.between("order_date", startDate, endDate);
        queryWrapper.orderByAsc("order_date");
        return orderMapper.selectList(queryWrapper);
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

    // ==================== 管理员订单管理实现 ====================

    @Autowired
    private com.canteen.mapper.UserMapper userMapper;

    @Autowired
    private com.canteen.mapper.MealTypeMapper mealTypeMapper;

    @Autowired
    private com.canteen.mapper.DepartmentMapper departmentMapper;

    @Autowired
    private com.canteen.mapper.RestaurantMapper restaurantMapper;

    /**
     * 获取订单列表（支持分页、筛选、排序）
     */
    @Override
    public Map<String, Object> getOrderList(int page, int pageSize, Integer status, String userName,
                                            String startDate, String endDate, String sortField, String sortOrder) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        // 状态筛选
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        // 日期范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("order_date", LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("order_date", LocalDate.parse(endDate));
        }
        
        // 用户姓名筛选
        List<Long> userIds = null;
        if (userName != null && !userName.isEmpty()) {
            QueryWrapper<com.canteen.entity.User> userQuery = new QueryWrapper<>();
            userQuery.like("name", userName).or().like("username", userName);
            List<com.canteen.entity.User> users = userMapper.selectList(userQuery);
            userIds = users.stream().map(com.canteen.entity.User::getId).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                result.put("list", new ArrayList<>());
                result.put("total", 0);
                return result;
            }
            queryWrapper.in("user_id", userIds);
        }
        
        // 排序 - 将驼峰命名转换为下划线命名
        String dbSortField = camelToUnderline(sortField);
        if ("asc".equalsIgnoreCase(sortOrder)) {
            queryWrapper.orderByAsc(dbSortField);
        } else {
            queryWrapper.orderByDesc(dbSortField);
        }
        
        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> pageParam = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> pageResult = orderMapper.selectPage(pageParam, queryWrapper);
        
        // 转换为DTO
        List<OrderDTO> dtoList = new ArrayList<>();
        for (Order order : pageResult.getRecords()) {
            OrderDTO dto = convertToDTO(order);
            dtoList.add(dto);
        }
        
        result.put("list", dtoList);
        result.put("total", pageResult.getTotal());
        
        return result;
    }

    /**
     * 获取订单详情
     */
    @Override
    public OrderDTO getOrderDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }
        return convertToDTO(order);
    }

    /**
     * 管理员修改订单
     */
    @Override
    public boolean updateOrderByAdmin(Order order) {
        Order existingOrder = orderMapper.selectById(order.getId());
        if (existingOrder == null) {
            return false;
        }
        
        // 更新字段
        if (order.getMealTypeId() != null) {
            existingOrder.setMealTypeId(order.getMealTypeId());
        }
        if (order.getOrderDate() != null) {
            existingOrder.setOrderDate(order.getOrderDate());
        }
        if (order.getStatus() != null) {
            existingOrder.setStatus(order.getStatus());
        }
        existingOrder.setUpdatedAt(LocalDateTime.now());
        
        return orderMapper.updateById(existingOrder) > 0;
    }

    /**
     * 管理员删除订单
     */
    @Override
    public boolean deleteOrderByAdmin(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return false;
        }
        
        // 物理删除订单记录
        return orderMapper.deleteById(id) > 0;
    }

    /**
     * 导出订单数据
     */
    @Override
    public void exportOrders(Integer status, String userName, String startDate, String endDate, 
                            HttpServletResponse response) {
        try {
            // 查询所有符合条件的订单（不分页）
            Map<String, Object> data = getOrderList(1, Integer.MAX_VALUE, status, userName, 
                    startDate, endDate, "createdAt", "desc");
            @SuppressWarnings("unchecked")
            List<OrderDTO> orders = (List<OrderDTO>) data.get("list");
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = "订单数据_" + LocalDate.now().toString() + ".xlsx";
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                    java.net.URLEncoder.encode(fileName, "UTF-8"));
            
            // 创建工作簿
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("订单数据");
            
            // 创建表头
            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"订单编号", "用户名", "真实姓名", "部门", "餐食类型", "价格", "订单日期", "状态", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            int rowNum = 1;
            for (OrderDTO order : orders) {
                org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getUsername());
                row.createCell(2).setCellValue(order.getRealName());
                row.createCell(3).setCellValue(order.getDepartmentName());
                row.createCell(4).setCellValue(order.getMealTypeName());
                row.createCell(5).setCellValue(order.getMealPrice() != null ? order.getMealPrice().doubleValue() : 0);
                row.createCell(6).setCellValue(order.getOrderDate().toString());
                row.createCell(7).setCellValue(order.getStatusDesc());
                row.createCell(8).setCellValue(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入响应
            workbook.write(response.getOutputStream());
            workbook.close();
            
        } catch (Exception e) {
            throw new RuntimeException("导出订单数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将驼峰命名转换为下划线命名
     */
    private String camelToUnderline(String camel) {
        if (camel == null || camel.isEmpty()) {
            return camel;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将Order转换为OrderDTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setMealTypeId(order.getMealTypeId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // 状态描述
        dto.setStatusDesc(order.getStatus() == 1 ? "有效" : "无效");
        
        // 查询用户信息
        com.canteen.entity.User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setRealName(user.getName());
            dto.setDepartmentId(user.getDepartmentId());
            
            // 查询部门信息
            if (user.getDepartmentId() != null) {
                com.canteen.entity.Department department = departmentMapper.selectById(user.getDepartmentId());
                dto.setDepartmentName(department != null ? department.getName() : "");
            }
        }
        
        // 查询餐食类型信息
        com.canteen.entity.MealType mealType = mealTypeMapper.selectById(order.getMealTypeId());
        if (mealType != null) {
            dto.setMealTypeName(mealType.getName());
            dto.setMealPrice(mealType.getPrice());
        }
        
        // 查询餐厅信息
        if (order.getRestaurantId() != null) {
            com.canteen.entity.Restaurant restaurant = restaurantMapper.selectById(order.getRestaurantId());
            if (restaurant != null) {
                dto.setRestaurantId(restaurant.getId());
                dto.setRestaurantName(restaurant.getName());
            }
        }
        
        return dto;
    }

}
