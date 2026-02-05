package com.canteen.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Map<String, Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error("缺少请求参数:", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        String parameterName = e.getParameterName();
        String friendlyMessage = "操作失败：缺少必要参数";
        
        if (parameterName != null) {
            if (parameterName.contains("Date") || parameterName.contains("date")) {
                friendlyMessage = "请选择日期";
            } else if (parameterName.contains("startDate") || parameterName.contains("start_date")) {
                friendlyMessage = "请选择开始日期";
            } else if (parameterName.contains("endDate") || parameterName.contains("end_date")) {
                friendlyMessage = "请选择结束日期";
            } else if (parameterName.contains("name")) {
                friendlyMessage = "请输入名称";
            } else if (parameterName.contains("password")) {
                friendlyMessage = "请输入密码";
            } else if (parameterName.contains("username")) {
                friendlyMessage = "请输入用户名";
            } else {
                friendlyMessage = "操作失败：缺少必要参数";
            }
        }
        
        result.put("message", friendlyMessage);
        result.put("error", e.getClass().getName());
        return result;
    }
    
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Map<String, Object> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        logger.error("数据库约束异常:", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        String errorMessage = e.getMessage();
        String friendlyMessage = "操作失败：数据存在关联记录，无法删除";
        
        if (errorMessage != null) {
            String lowerMessage = errorMessage.toLowerCase();
            
            if (lowerMessage.contains("meal_type") || lowerMessage.contains("餐食类型")) {
                friendlyMessage = "删除失败：该餐食类型已被订单使用，无法删除";
            } else if (lowerMessage.contains("restaurant") || lowerMessage.contains("餐厅")) {
                friendlyMessage = "删除失败：该餐厅已被订单使用，无法删除";
            } else if (lowerMessage.contains("department") || lowerMessage.contains("部门")) {
                friendlyMessage = "删除失败：该部门下还有员工，无法删除";
            } else if (lowerMessage.contains("user") || lowerMessage.contains("员工") || lowerMessage.contains("用户")) {
                friendlyMessage = "删除失败：该用户存在订单记录，无法删除";
            } else if (lowerMessage.contains("role") || lowerMessage.contains("角色")) {
                friendlyMessage = "删除失败：该角色已被分配给用户，无法删除";
            } else if (lowerMessage.contains("duplicate entry") || lowerMessage.contains("重复")) {
                friendlyMessage = "操作失败：数据已存在，请勿重复添加";
            } else if (lowerMessage.contains("data too long") || lowerMessage.contains("数据过长")) {
                friendlyMessage = "操作失败：输入内容过长，请缩短后重试";
            } else if (lowerMessage.contains("column") && lowerMessage.contains("cannot be null")) {
                friendlyMessage = "操作失败：必填字段不能为空";
            }
        }
        
        result.put("message", friendlyMessage);
        result.put("error", e.getClass().getName());
        return result;
    }
    
    @ExceptionHandler(DuplicateKeyException.class)
    public Map<String, Object> handleDuplicateKeyException(DuplicateKeyException e) {
        logger.error("重复键异常:", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "操作失败：数据已存在，请勿重复添加");
        result.put("error", e.getClass().getName());
        return result;
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.error("数据完整性异常:", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        String errorMessage = e.getMessage();
        String friendlyMessage = "操作失败：数据存在关联记录，无法删除";
        
        if (errorMessage != null) {
            String lowerMessage = errorMessage.toLowerCase();
            
            if (lowerMessage.contains("meal_type") || lowerMessage.contains("餐食类型")) {
                friendlyMessage = "删除失败：该餐食类型已被订单使用，无法删除";
            } else if (lowerMessage.contains("restaurant") || lowerMessage.contains("餐厅")) {
                friendlyMessage = "删除失败：该餐厅已被订单使用，无法删除";
            } else if (lowerMessage.contains("department") || lowerMessage.contains("部门")) {
                friendlyMessage = "删除失败：该部门下还有员工，无法删除";
            } else if (lowerMessage.contains("user") || lowerMessage.contains("员工") || lowerMessage.contains("用户")) {
                friendlyMessage = "删除失败：该用户存在订单记录，无法删除";
            } else if (lowerMessage.contains("role") || lowerMessage.contains("角色")) {
                friendlyMessage = "删除失败：该角色已被分配给用户，无法删除";
            } else if (lowerMessage.contains("duplicate entry") || lowerMessage.contains("重复")) {
                friendlyMessage = "操作失败：数据已存在，请勿重复添加";
            } else if (lowerMessage.contains("data too long") || lowerMessage.contains("数据过长")) {
                friendlyMessage = "操作失败：输入内容过长，请缩短后重试";
            } else if (lowerMessage.contains("column") && lowerMessage.contains("cannot be null")) {
                friendlyMessage = "操作失败：必填字段不能为空";
            }
        }
        
        result.put("message", friendlyMessage);
        result.put("error", e.getClass().getName());
        return result;
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("参数异常:", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "操作失败：参数错误，请检查输入");
        result.put("error", e.getClass().getName());
        return result;
    }
    
    @ExceptionHandler(NullPointerException.class)
    public Map<String, Object> handleNullPointerException(NullPointerException e) {
        logger.error("空指针异常:", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "操作失败：系统内部错误，请联系管理员");
        result.put("error", e.getClass().getName());
        return result;
    }
    
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        logger.error("系统异常:", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "系统错误: " + e.getMessage());
        result.put("error", e.getClass().getName());
        return result;
    }
}
