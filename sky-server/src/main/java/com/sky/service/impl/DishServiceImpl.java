package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    public void saveWithFlavor(DishDTO dishDTO) {
        // 新增菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        // 新增口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(item -> item.setDishId(dishDTO.getId()));
            for (DishFlavor flavor : flavors) {
                dishFlavorMapper.insert(flavor);
            }
        }
    }

    public void deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<SetmealDish> lqw1 = new LambdaQueryWrapper<SetmealDish>();
        for (Long id : ids) {
            // 判断当前菜品是否能够删除---是否存在起售中的菜品？？
            Dish oneDish = dishMapper.selectById(id);
            if (oneDish.getStatus().equals(StatusConstant.ENABLE)) {
                // 当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

            // 判断当前菜品是否能够删除---是否被套餐关联了？？
            lqw1.eq(SetmealDish::getDishId, id);
            List<SetmealDish> setmealDishes = setmealDishMapper.selectList(lqw1);
            if (setmealDishes != null && setmealDishes.size() > 0) {
                // 当前菜品被套餐关联了，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            lqw1.clear();
        }


        // 删除菜品表中的菜品数据
        // 删除菜品关联的口味表中的数据
        LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<DishFlavor>();
        for (Long id : ids) {
            dishMapper.deleteById(id);
            lqw2.eq(DishFlavor::getDishId, id);
            dishFlavorMapper.delete(lqw2);
            lqw2.clear();
        }
    }

    public void startOrStop(Integer status, Long id) {

    }

    public void update(DishDTO categoryDTO) {

    }

    public List getByType(Integer type) {
        return null;
    }

    public PageResult getByPage(DishPageQueryDTO categoryPageQueryDTO) {
        return null;
    }
}
