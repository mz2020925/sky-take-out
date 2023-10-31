package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    @AutoFill(value = OperationType.INSERT)
    default void insertSetmeal(Setmeal setmeal){
        this.insert(setmeal);
    }

    @AutoFill(value = OperationType.UPDATE)
    default void updateSetmeal(Setmeal setmeal){
        this.updateById(setmeal);
    }

    Page<SetmealVO> getByPage(SetmealPageQueryDTO setmealPageQueryDTO);
}
