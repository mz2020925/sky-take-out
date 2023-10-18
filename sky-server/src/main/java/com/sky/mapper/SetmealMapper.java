package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    @AutoFill(value = OperationType.INSERT)
    int insert(Setmeal entity);

    @AutoFill(value = OperationType.UPDATE)
    int updateById(Setmeal entity);
}
