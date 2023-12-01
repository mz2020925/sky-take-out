package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface DishMapper extends BaseMapper<Dish> {


    @AutoFill(value = OperationType.INSERT)
    default void insertDish(Dish dish){
        this.insert(dish);
    }

    // int insert(Dish dish);

    @AutoFill(value = OperationType.UPDATE)
    default void updateDish(Dish dish){
        this.updateById(dish);
    }

    Page<DishVO> getByPage(DishPageQueryDTO dishPageQueryDTO);

    @Select("select count(id) from dish where status=#{status}")
    Integer overviewDishes(Integer status);
}
