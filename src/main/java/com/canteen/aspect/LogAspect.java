package com.canteen.aspect;

import com.alibaba.fastjson2.JSON;
import com.canteen.annotation.Log;
import com.canteen.entity.OperationLog;
import com.canteen.entity.User;
import com.canteen.service.OperationLogService;
import com.canteen.service.UserService;
import com.canteen.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Pointcut("@annotation(com.canteen.annotation.Log)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        OperationLog operationLog = new OperationLog();
        Object result = null;
        Exception exception = null;

        try {
            result = point.proceed();
            operationLog.setStatus(1);
            return result;
        } catch (Exception e) {
            exception = e;
            operationLog.setStatus(0);
            operationLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - beginTime;
            operationLog.setExecutionTime(costTime);
            handleLog(point, operationLog, result);
        }
    }

    private void handleLog(ProceedingJoinPoint joinPoint, OperationLog operationLog, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Log logAnnotation = signature.getMethod().getAnnotation(Log.class);
            if (logAnnotation == null) {
                return;
            }

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = signature.getName();
            operationLog.setModule(logAnnotation.module());
            operationLog.setDescription(logAnnotation.description());
            operationLog.setOperationType(logAnnotation.operationType());

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setIpAddress(getIpAddress(request));
                operationLog.setUserAgent(request.getHeader("User-Agent"));

                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    try {
                        String username = jwtUtils.getUsernameFromToken(token);
                        operationLog.setUsername(username);
                        
                        // 获取用户ID
                        User user = userService.findByUsername(username);
                        if (user != null) {
                            operationLog.setUserId(user.getId());
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to get username from token: {}", e.getMessage());
                    }
                }
            }

            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                Object arg = args[0];
                // 如果是User对象，只记录必要的字段，避免包含不存在的数据库字段
                if (arg instanceof User) {
                    User user = (User) arg;
                    java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("name", user.getName());
                    userMap.put("roleId", user.getRoleId());
                    operationLog.setRequestParams(JSON.toJSONString(userMap));
                } else {
                    String params = JSON.toJSONString(arg);
                    operationLog.setRequestParams(params);
                }
            }

            if (result != null) {
                operationLog.setNewData(JSON.toJSONString(result));
            }

            operationLog.setCreatedAt(LocalDateTime.now());
            operationLogService.saveLog(operationLog);

        } catch (Exception e) {
            logger.error("Failed to save operation log", e);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
