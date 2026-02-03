package com.canteen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canteen.entity.Restaurant;
import com.canteen.mapper.RestaurantMapper;
import com.canteen.service.RestaurantService;
import org.springframework.stereotype.Service;

@Service
public class RestaurantServiceImpl extends ServiceImpl<RestaurantMapper, Restaurant> implements RestaurantService {
}
