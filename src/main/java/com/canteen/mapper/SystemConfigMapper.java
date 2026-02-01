package com.canteen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.entity.SystemConfig;

/**
 * 系统配置Mapper接口
 */
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询配置
     * @param configKey 配置键
     * @return 系统配置
     */
    SystemConfig selectByConfigKey(String configKey);

}
