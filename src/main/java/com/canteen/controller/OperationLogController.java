package com.canteen.controller;

import com.canteen.entity.OperationLog;
import com.canteen.service.OperationLogService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Map<String, Object> getLogList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Map<String, Object> result = new HashMap<>();
        
        List<OperationLog> logs = operationLogService.queryLogs(
            userId, username, operationType, module, 
            startTime, endTime, status, page, pageSize);
        
        Long total = operationLogService.countLogs(
            userId, username, operationType, module, 
            startTime, endTime, status);
        
        result.put("success", true);
        result.put("data", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return result;
    }

    @GetMapping("/export")
    public void exportLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws IOException {
        
        List<OperationLog> logs = operationLogService.queryLogs(
            userId, username, operationType, module, 
            startTime, endTime, status, 1, 10000);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("操作日志");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "用户ID", "用户名", "操作类型", "操作内容", "操作时间", "IP地址"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < logs.size(); i++) {
            OperationLog log = logs.get(i);
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(log.getId());
            row.createCell(1).setCellValue(log.getUserId());
            row.createCell(2).setCellValue(log.getUsername());
            row.createCell(3).setCellValue(log.getOperationType());
            row.createCell(4).setCellValue(log.getDescription());
            
            if (log.getCreatedAt() != null) {
                row.createCell(5).setCellValue(log.getCreatedAt().format(formatter));
            }
            
            row.createCell(6).setCellValue(log.getIpAddress());
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        String fileName = "操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteLog(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = operationLogService.removeById(id);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

    @DeleteMapping("/clear")
    public Map<String, Object> clearLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeDate) {
        Map<String, Object> result = new HashMap<>();
        operationLogService.deleteLogsBeforeDate(beforeDate);
        result.put("success", true);
        result.put("message", "清理成功");
        return result;
    }
}
