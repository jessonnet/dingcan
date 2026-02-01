package com.canteen.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
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
