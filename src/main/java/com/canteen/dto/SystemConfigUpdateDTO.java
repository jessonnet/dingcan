package com.canteen.dto;

import lombok.Data;

/**
 * 系统配置更新请求DTO
 */
@Data
public class SystemConfigUpdateDTO {
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 操作说明（用于日志记录）
     */
    private String description;
    
}