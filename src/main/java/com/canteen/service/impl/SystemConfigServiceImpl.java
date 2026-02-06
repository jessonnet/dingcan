package com.canteen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.SystemConfig;
import com.canteen.mapper.SystemConfigMapper;
import com.canteen.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现类
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值
     */
    @Override
    public String getConfigValue(String configKey) {
        SystemConfig systemConfig = systemConfigMapper.selectByConfigKey(configKey);
        return systemConfig != null ? systemConfig.getConfigValue() : null;
    }

    /**
     * 检查微信登录功能是否启用
     * @return true表示启用，false表示禁用
     */
    @Override
    public boolean isWeChatLoginEnabled() {
        String enabled = getConfigValue("wechat_login_enabled");
        return "1".equals(enabled);
    }

    /**
     * 获取微信登录模式
     * @return 登录模式（auto：自动检测，force：强制微信，manual：手动选择）
     */
    @Override
    public String getWeChatLoginMode() {
        String mode = getConfigValue("wechat_login_mode");
        return mode != null ? mode : "auto";
    }

    /**
     * 更新系统配置
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    @Override
    public boolean updateConfig(String configKey, String configValue) {
        SystemConfig systemConfig = systemConfigMapper.selectByConfigKey(configKey);
        if (systemConfig != null) {
            systemConfig.setConfigValue(configValue);
            return systemConfigMapper.updateById(systemConfig) > 0;
        }
        return false;
    }

}
