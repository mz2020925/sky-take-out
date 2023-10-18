package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);  // 更新套餐表

        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishList) {
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishMapper.insert(setmealDish);  // 更新套餐菜品关系表
        }
    }

    public void deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<SetmealDish>();
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);  // 如果在起售，不能删除
            }

            setmealMapper.deleteById(id);  // 删除套餐
            lqw.eq(SetmealDish::getSetmealId, id);
            setmealDishMapper.delete(lqw);  // 删除套餐菜品关系表中对应的行
        }
    }


    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 更新套餐表
        setmealMapper.updateById(setmeal);

        // 删除和该套餐关联的套餐菜品关系表中的所有行，然后再把这一次请求中的关系插入到套餐菜品关系表中
        LambdaQueryWrapper<SetmealDish> lwq = new LambdaQueryWrapper<SetmealDish>();
        lwq.eq(SetmealDish::getSetmealId, setmealDTO.getId());
        setmealDishMapper.delete(lwq);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDTO.getId());
            setmealDishMapper.insert(setmealDish);
        }
    }

    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.updateById(setmeal);
    }

    public PageResult getByPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 获取起始页码，和每页行数
        IPage<Setmeal> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());


        String name = setmealPageQueryDTO.getName();
        Integer categoryId = setmealPageQueryDTO.getCategoryId();
        Integer status = setmealPageQueryDTO.getStatus();

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null, Setmeal::getName, name)
                .eq(categoryId!=null, Setmeal::getCategoryId, categoryId)
                .eq(status!=null, Setmeal::getStatus, status)
                .orderByDesc(Setmeal::getUpdateTime);
        IPage<Setmeal> iPage = setmealMapper.selectPage(page, lqw);


        return new PageResult(iPage.getTotal(), iPage.getRecords());

    }
}
