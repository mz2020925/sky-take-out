package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.DishFlavor;
import com.sky.mapper.FlavorMapper;
import com.sky.service.FlavorService;
import org.springframework.stereotype.Service;

@Service
public class FlavorServiceImpl extends ServiceImpl<FlavorMapper, DishFlavor> implements FlavorService {
}
