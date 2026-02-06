package com.canteen.controller;

import com.canteen.dto.SystemConfigResponseDTO;
import com.canteen.dto.SystemConfigUpdateDTO;
import com.canteen.entity.SystemConfig;
import com.canteen.entity.User;
import com.canteen.service.OperationLogService;
import com.canteen.service.SystemConfigService;
import com.canteen.util.JwtUtil;
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

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取所有系统配置
     */
    @GetMapping("/list")
    public Map<String, Object> getAllConfigs() {
        try {
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
     * 获取微信登录配置
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
            String username = jwtUtil.getUsernameFromToken(token);
            
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
                operationLogService.logOperation(
                    username,
                    "UPDATE",
                    "系统配置",
                    updateDTO.getDescription() != null ? updateDTO.getDescription() : "更新系统配置",
                    "POST",
                    "/api/system/config/update",
                    updateDTO.toString(),
                    oldConfig.toString(),
                    updateDTO.getConfigValue(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    true,
                    null,
                    0L
                );
                
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
            String username = jwtUtil.getUsernameFromToken(token);
            
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
                    
                    operationLogService.logOperation(
                        username,
                        "UPDATE",
                        "系统配置",
                        updateDTO.getDescription() != null ? updateDTO.getDescription() : "批量更新系统配置",
                        "POST",
                        "/api/system/config/batch-update",
                        updateDTO.toString(),
                        oldConfig.toString(),
                        updateDTO.getConfigValue(),
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"),
                        true,
                        null,
                        0L
                    );
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