package com.canteen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单Mapper接口
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据用户ID和日期查询订单
     * @param userId 用户ID
     * @param orderDate 订单日期
     * @return 订单列表
     */
    List<Order> selectByUserIdAndDate(@Param("userId") Long userId, @Param("orderDate") LocalDate orderDate);

    /**
     * 根据日期查询订单
     * @param orderDate 订单日期
     * @return 订单列表
     */
    List<Order> selectByDate(@Param("orderDate") LocalDate orderDate);

}
