package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.entity.SystemConfig;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 检查微信登录功能是否启用
     * @return true表示启用，false表示禁用
     */
    boolean isWeChatLoginEnabled();

    /**
     * 获取微信登录模式
     * @return 登录模式（auto：自动检测，force：强制微信，manual：手动选择）
     */
    String getWeChatLoginMode();

    /**
     * 更新系统配置
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    boolean updateConfig(String configKey, String configValue);

}
