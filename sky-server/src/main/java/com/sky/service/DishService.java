package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDTO categoryDTO);

    void deleteByIds(List<Long> ids);

    void startOrStop(Integer status, Long id);

    void update(DishDTO categoryDTO);

    List getByType(Integer type);

    PageResult getByPage(DishPageQueryDTO categoryPageQueryDTO);
}
