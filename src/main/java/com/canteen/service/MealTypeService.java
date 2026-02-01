package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.entity.MealType;

import java.util.List;

/**
 * 餐食类型服务接口
 */
public interface MealTypeService extends IService<MealType> {

    /**
     * 获取所有启用的餐食类型
     * @return 餐食类型列表
     */
    List<MealType> getEnabledMealTypes();

}
