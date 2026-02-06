package com.canteen.dto;

import lombok.Data;

/**
 * 系统配置响应DTO
 */
@Data
public class SystemConfigResponseDTO {
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 配置类型（boolean：布尔值，string：字符串，number：数字）
     */
    private String type;
    
    /**
     * 配置分组（system：系统配置，login：登录配置，wechat：微信配置）
     */
    private String group;
    
    /**
     * 是否可编辑
     */
    private Boolean editable;
    
}