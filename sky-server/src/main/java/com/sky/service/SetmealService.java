package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    void save(SetmealDTO setmealDTO);

    void deleteByIds(List<Long> ids);

    void update(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);

    PageResult getByPage(SetmealPageQueryDTO setmealPageQueryDTO);
}
