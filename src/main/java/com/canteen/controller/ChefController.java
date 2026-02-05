package com.canteen.controller;

import com.canteen.entity.Order;
import com.canteen.service.OrderService;
import com.canteen.service.MealTypeService;
import com.canteen.entity.MealType;
import com.canteen.entity.User;
import com.canteen.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 厨师控制器
 */
@RestController
@RequestMapping("/api/chef")
public class ChefController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MealTypeService mealTypeService;

    @Autowired
    private UserService userService;

    /**
     * 获取指定日期的订餐情况
     * @param orderDate 订单日期
     * @return 结果
     */
    @GetMapping("/order-status")
    public Map<String, Object> getOrderStatus(@RequestParam(required = false) String orderDate) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查日期参数
            if (orderDate == null || orderDate.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择日期");
                return result;
            }

            // 获取当前用户
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 解析日期
            LocalDate date = LocalDate.parse(orderDate);

            // 查询订单
            List<Order> orders = orderService.getOrdersByDate(date);

            // 过滤：只显示与厨师关联食堂相关的订单
            if (currentUser.getRestaurantId() != null) {
                orders = orders.stream()
                        .filter(order -> order.getRestaurantId() != null && order.getRestaurantId().equals(currentUser.getRestaurantId()))
                        .collect(Collectors.toList());
            }

            // 获取餐食类型
            List<MealType> mealTypes = mealTypeService.getEnabledMealTypes();

            // 按餐食类型分组
            Map<Long, List<Order>> ordersByMealType = orders.stream()
                    .collect(Collectors.groupingBy(Order::getMealTypeId));

            // 构建结果
            Map<Long, Map<String, Object>> mealTypeOrders = new HashMap<>();
            for (MealType mealType : mealTypes) {
                List<Order> mealTypeOrderList = ordersByMealType.getOrDefault(mealType.getId(), List.of());
                List<Map<String, Object>> userOrders = mealTypeOrderList.stream().map(order -> {
                    User user = userService.getById(order.getUserId());
                    Map<String, Object> userOrder = new HashMap<>();
                    userOrder.put("userId", user.getId());
                    userOrder.put("username", user.getUsername());
                    userOrder.put("name", user.getName());
                    userOrder.put("department", user.getDepartmentName());
                    return userOrder;
                }).collect(Collectors.toList());

                Map<String, Object> mealTypeOrder = new HashMap<>();
                mealTypeOrder.put("mealType", mealType);
                mealTypeOrder.put("count", mealTypeOrderList.size());
                mealTypeOrder.put("orders", userOrders);
                mealTypeOrders.put(mealType.getId(), mealTypeOrder);
            }

            result.put("success", true);
            result.put("data", mealTypeOrders);
            result.put("totalCount", orders.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "日期格式错误，请使用 YYYY-MM-DD 格式");
        }

        return result;
    }

    /**
     * 获取订餐统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 结果
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查日期参数
            if (startDate == null || startDate.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择开始日期");
                return result;
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择结束日期");
                return result;
            }

            // 获取当前用户
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 解析日期
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 获取餐食类型
            List<MealType> mealTypes = mealTypeService.getEnabledMealTypes();

            // 统计数据
            Map<LocalDate, Map<Long, Integer>> dailyStatistics = new HashMap<>();
            int totalCount = 0;

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                List<Order> orders = orderService.getOrdersByDate(date);

                // 过滤：只显示与厨师关联食堂相关的订单
                if (currentUser.getRestaurantId() != null) {
                    orders = orders.stream()
                            .filter(order -> order.getRestaurantId() != null && order.getRestaurantId().equals(currentUser.getRestaurantId()))
                            .collect(Collectors.toList());
                }

                Map<Long, Integer> mealTypeCount = new HashMap<>();
                for (MealType mealType : mealTypes) {
                    int count = (int) orders.stream().filter(order -> order.getMealTypeId().equals(mealType.getId())).count();
                    mealTypeCount.put(mealType.getId(), count);
                    totalCount += count;
                }
                dailyStatistics.put(date, mealTypeCount);
            }

            result.put("success", true);
            result.put("data", dailyStatistics);
            result.put("mealTypes", mealTypes);
            result.put("totalCount", totalCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "日期格式错误，请使用 YYYY-MM-DD 格式");
        }

        return result;
    }

    /**
     * 导出Excel
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param response HttpServletResponse
     * @throws IOException 异常
     */
    @GetMapping("/export")
    public void exportExcel(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, HttpServletResponse response) throws IOException {
        try {
            // 检查日期参数
            if (startDate == null || startDate.trim().isEmpty()) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"请选择开始日期\"}");
                return;
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"请选择结束日期\"}");
                return;
            }

            // 解析日期
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("订餐统计");

            // 获取餐食类型
            List<MealType> mealTypes = mealTypeService.getEnabledMealTypes();

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("日期");
            for (int i = 0; i < mealTypes.size(); i++) {
                headerRow.createCell(i + 1).setCellValue(mealTypes.get(i).getName());
            }
            headerRow.createCell(mealTypes.size() + 1).setCellValue("总计");

            // 填充数据
            int rowNum = 1;
            int totalCount = 0;
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                List<Order> orders = orderService.getOrdersByDate(date);
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                int dailyCount = 0;
                for (int i = 0; i < mealTypes.size(); i++) {
                    MealType mealType = mealTypes.get(i);
                    int count = (int) orders.stream().filter(order -> order.getMealTypeId().equals(mealType.getId())).count();
                    row.createCell(i + 1).setCellValue(count);
                    dailyCount += count;
                }
                row.createCell(mealTypes.size() + 1).setCellValue(dailyCount);
                totalCount += dailyCount;
            }

            // 添加总计行
            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(0).setCellValue("总计");
            for (int i = 0; i < mealTypes.size(); i++) {
                MealType mealType = mealTypes.get(i);
                int mealTypeTotal = 0;
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    List<Order> orders = orderService.getOrdersByDate(date);
                    mealTypeTotal += orders.stream().filter(order -> order.getMealTypeId().equals(mealType.getId())).count();
                }
                totalRow.createCell(i + 1).setCellValue(mealTypeTotal);
            }
            totalRow.createCell(mealTypes.size() + 1).setCellValue(totalCount);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("订餐统计.xlsx", "UTF-8"));

            // 输出流
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                workbook.close();
            }
        } catch (Exception e) {
            // 处理异常
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"日期格式错误，请使用 YYYY-MM-DD 格式\"}");
        }
    }

}
