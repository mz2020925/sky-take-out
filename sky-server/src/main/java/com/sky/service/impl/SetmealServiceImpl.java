package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        setmealMapper.insertSetmeal(setmeal);  // 更新套餐表

        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishList) {
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishMapper.insert(setmealDish);  // 更新套餐菜品关系表
        }
    }

    @Transactional  // 设置本方法是事务模式
    public void deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<SetmealDish>();
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);  // 如果在起售，不能删除
            }
        }

        for (Long id : ids) {
            setmealMapper.deleteById(id);  // 删除套餐
            lqw.eq(SetmealDish::getSetmealId, id);
            setmealDishMapper.delete(lqw);  // 删除套餐菜品关系表中对应的行
        }
    }


    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 更新套餐表
        setmealMapper.updateSetmeal(setmeal);

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
        setmealMapper.updateSetmeal(setmeal);
    }

    public PageResult getByPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 使用MyBatis和Mapper代理开发实现这里的分页查询，因为这里设涉及多表查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.getByPage(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());

    }
}
