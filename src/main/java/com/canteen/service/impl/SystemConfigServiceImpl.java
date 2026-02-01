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

}
