package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    @AutoFill(value = OperationType.INSERT)
    int insert(Dish entity);

    @AutoFill(value = OperationType.UPDATE)
    int updateById(Dish entity);
}
