package com.canteen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.entity.Role;

/**
 * 角色Mapper接口
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID获取角色名称
     * @param userId 用户ID
     * @return 角色名称
     */
    String selectRoleNameByUserId(Long userId);

}
