package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    @AutoFill(value = OperationType.INSERT)
    int insert(Dish entity);

    @AutoFill(value = OperationType.UPDATE)
    int updateById(Dish entity);

    Page<DishVO> getByPage(DishPageQueryDTO dishPageQueryDTO);
}
