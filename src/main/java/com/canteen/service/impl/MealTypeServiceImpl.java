package com.canteen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.MealType;
import com.canteen.mapper.MealTypeMapper;
import com.canteen.service.MealTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 餐食类型服务实现类
 */
@Service
public class MealTypeServiceImpl extends ServiceImpl<MealTypeMapper, MealType> implements MealTypeService {

    /**
     * 获取所有启用的餐食类型
     * @return 餐食类型列表
     */
    @Override
    public List<MealType> getEnabledMealTypes() {
        QueryWrapper<MealType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        return baseMapper.selectList(queryWrapper);
    }

}
