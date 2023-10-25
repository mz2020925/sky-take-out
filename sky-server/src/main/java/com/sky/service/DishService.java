package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDTO categoryDTO);

    void deleteByIds(List<Long> ids);

    void update(DishDTO dishDTO);

    DishVO getById(Long id);


    PageResult getByPage(DishPageQueryDTO dishPageQueryDTO);

    void startOrStop(Integer status, Long id);

}
