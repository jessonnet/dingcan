package com.canteen.controller;

import com.canteen.dto.SystemConfigResponseDTO;
import com.canteen.dto.SystemConfigUpdateDTO;
import com.canteen.entity.OperationLog;
import com.canteen.entity.SystemConfig;
import com.canteen.entity.User;
import com.canteen.service.OperationLogService;
import com.canteen.service.SystemConfigService;
import com.canteen.service.UserService;
import com.canteen.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/config")
public class SystemConfigController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SystemConfigController.class);

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserService userService;

    /**
     * 获取订单相关配置（公开接口，无需认证）
     */
    @GetMapping("/order")
    public Map<String, Object> getOrderConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            
            // 获取锁单时间
            SystemConfig lockTimeConfig = systemConfigService.getByConfigKey("lock_time");
            String lockTime = lockTimeConfig != null ? lockTimeConfig.getConfigValue() : "16:00";
            config.put("lockTime", lockTime);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", config);
            return result;
            
        } catch (Exception e) {
            log.error("获取订单配置失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取订单配置失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取微信登录配置（公开接口，无需认证）
     */
    @GetMapping("/wechat")
    public Map<String, Object> getWeChatConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("enabled", systemConfigService.isWeChatLoginEnabled());
            config.put("mode", systemConfigService.getWeChatLoginMode());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", config);
            return result;
            
        } catch (Exception e) {
            log.error("获取微信登录配置失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取微信登录配置失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取所有系统配置（需要管理员权限）
     */
    @GetMapping("/list")
    public Map<String, Object> getAllConfigs(HttpServletRequest request) {
        try {
            // 验证管理员权限
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未授权访问");
                return result;
            }
            
            token = token.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);
            
            // 获取用户信息验证权限
            User user = userService.findByUsername(username);
            
            if (user == null || !"admin".equalsIgnoreCase(user.getUsername())) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }
            
            List<SystemConfig> configs = systemConfigService.list();
            List<SystemConfigResponseDTO> responseDTOs = new ArrayList<>();
            
            for (SystemConfig config : configs) {
                SystemConfigResponseDTO dto = new SystemConfigResponseDTO();
                dto.setConfigKey(config.getConfigKey());
                dto.setConfigValue(config.getConfigValue());
                dto.setDescription(config.getDescription());
                dto.setType(determineConfigType(config.getConfigKey()));
                dto.setGroup(determineConfigGroup(config.getConfigKey()));
                dto.setEditable(isConfigEditable(config.getConfigKey()));
                responseDTOs.add(dto);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", responseDTOs);
            return result;
            
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取系统配置失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 更新系统配置
     */
    @PostMapping("/update")
    public Map<String, Object> updateConfig(@RequestBody SystemConfigUpdateDTO updateDTO, 
                                           HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未授权访问");
                return result;
            }
            
            token = token.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);
            
            if (!isConfigEditable(updateDTO.getConfigKey())) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "该配置不允许编辑");
                return result;
            }
            
            SystemConfig oldConfig = systemConfigService.lambdaQuery()
                    .eq(SystemConfig::getConfigKey, updateDTO.getConfigKey())
                    .one();
            
            if (oldConfig == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "配置项不存在");
                return result;
            }
            
            boolean success = systemConfigService.updateConfig(updateDTO.getConfigKey(), updateDTO.getConfigValue());
            
            if (success) {
                try {
                    User user = userService.findByUsername(username);
                    OperationLog log = new OperationLog();
                    log.setUserId(user != null ? user.getId() : null);
                    log.setUsername(username);
                    log.setOperationType("UPDATE");
                    log.setModule("系统配置");
                    log.setDescription(updateDTO.getDescription() != null ? updateDTO.getDescription() : "更新系统配置");
                    log.setCreatedAt(java.time.LocalDateTime.now());
                    log.setIpAddress(request.getRemoteAddr());
                    log.setUserAgent(request.getHeader("User-Agent"));
                    log.setStatus(1);
                    operationLogService.save(log);
                } catch (Exception e) {
                    System.err.println("记录操作日志失败: " + e.getMessage());
                }
                
                log.info("系统配置更新成功: {} = {}", updateDTO.getConfigKey(), updateDTO.getConfigValue());
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "配置更新成功");
                return result;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "配置更新失败");
                return result;
            }
            
        } catch (Exception e) {
            log.error("更新系统配置失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "更新系统配置失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 批量更新系统配置
     */
    @PostMapping("/batch-update")
    public Map<String, Object> batchUpdateConfig(@RequestBody List<SystemConfigUpdateDTO> updateDTOs, 
                                                 HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未授权访问");
                return result;
            }
            
            token = token.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);
            
            // 获取用户信息验证权限
            User user = userService.findByUsername(username);
            
            if (user == null || !"admin".equalsIgnoreCase(user.getUsername())) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (SystemConfigUpdateDTO updateDTO : updateDTOs) {
                if (!isConfigEditable(updateDTO.getConfigKey())) {
                    failCount++;
                    continue;
                }
                
                SystemConfig oldConfig = systemConfigService.lambdaQuery()
                        .eq(SystemConfig::getConfigKey, updateDTO.getConfigKey())
                        .one();
                
                if (oldConfig == null) {
                    failCount++;
                    continue;
                }
                
                boolean success = systemConfigService.updateConfig(updateDTO.getConfigKey(), updateDTO.getConfigValue());
                
                if (success) {
                    successCount++;
                    
                    try {
                        OperationLog log = new OperationLog();
                        log.setUserId(user.getId());
                        log.setUsername(username);
                        log.setOperationType("UPDATE");
                        log.setModule("系统配置");
                        log.setDescription(updateDTO.getDescription() != null ? updateDTO.getDescription() : "批量更新系统配置");
                        log.setCreatedAt(java.time.LocalDateTime.now());
                        log.setIpAddress(request.getRemoteAddr());
                        log.setUserAgent(request.getHeader("User-Agent"));
                        log.setStatus(1);
                        operationLogService.save(log);
                    } catch (Exception e) {
                        System.err.println("记录操作日志失败: " + e.getMessage());
                    }
                } else {
                    failCount++;
                }
            }
            
            log.info("批量更新系统配置完成: 成功 {} 条，失败 {} 条", successCount, failCount);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", String.format("批量更新完成: 成功 %d 条，失败 %d 条", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            return result;
            
        } catch (Exception e) {
            log.error("批量更新系统配置失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量更新系统配置失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 确定配置类型
     */
    private String determineConfigType(String configKey) {
        if ("wechat_login_enabled".equals(configKey)) {
            return "boolean";
        } else if ("lock_time".equals(configKey)) {
            return "time";
        } else {
            return "string";
        }
    }

    /**
     * 确定配置分组
     */
    private String determineConfigGroup(String configKey) {
        if (configKey.startsWith("wechat_")) {
            return "wechat";
        } else if ("lock_time".equals(configKey)) {
            return "order";
        } else {
            return "system";
        }
    }

    /**
     * 判断配置是否可编辑
     */
    private boolean isConfigEditable(String configKey) {
        return !"system_name".equals(configKey);
    }

}
