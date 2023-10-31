package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FlavorMapper flavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    public void saveWithFlavor(DishDTO dishDTO) {
        // 新增菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insertDish(dish);

        // 新增口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(item -> item.setDishId(dish.getId()));
            for (DishFlavor flavor : flavors) {
                flavorMapper.insert(flavor);
            }
        }
    }

    @Transactional  // 设置本方法是事务模式
    public void deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<SetmealDish> lqw1 = new LambdaQueryWrapper<SetmealDish>();
        LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<DishFlavor>();
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

        for (Long id : ids) {
            // 删除菜品表中的菜品数据
            // 删除菜品关联的口味表中的数据
            dishMapper.deleteById(id);
            lqw2.eq(DishFlavor::getDishId, id);
            flavorMapper.delete(lqw2);
            lqw2.clear();
        }


    }

    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 更新菜品表
        dishMapper.updateDish(dish);

        // 删除原来关联的口味表的行数据，将这一次携带过来的口味数据插入到口味表中
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<DishFlavor>();
        lqw.eq(DishFlavor::getDishId, dishDTO.getId());
        flavorMapper.delete(lqw);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 向口味表中插入数据
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
                flavorMapper.insert(flavor);
            }
        }
    }

    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.updateDish(dish);
    }


    public PageResult getByPage(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.getByPage(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    public DishVO getById(Long id) {
        DishVO dishVO = new DishVO();
        Dish dish = dishMapper.selectById(id);

        // 设置DishVO的categoryName属性
        LambdaQueryWrapper<Category> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(Category::getId, dish.getCategoryId())
                .select(Category::getName);
        String name = categoryMapper.selectOne(lqw1).getName();
        dishVO.setCategoryName(name);

        // 设置DishVO的flavors属性
        LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavors = flavorMapper.selectList(lqw2);
        dishVO.setFlavors(dishFlavors);

        BeanUtils.copyProperties(dish, dishVO);
        return dishVO;
    }

    public List<Dish> getByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<Dish>();
        lqw.eq(Dish::getCategoryId, categoryId);
        return dishMapper.selectList(lqw);
    }

    public List<DishVO> getDishVOByCategoryId(Long categoryId) {
        // 根据categoryId查询菜品，不起售就不查了
        LambdaQueryWrapper<Dish> lqw1 = new LambdaQueryWrapper<Dish>();
        lqw1.eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, StatusConstant.ENABLE);
        List<Dish> dishes = dishMapper.selectList(lqw1);

        // 创建dishVOs作为返回值
        List<DishVO> dishVOs = new ArrayList<>();
        LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<>();
        for (Dish dish : dishes) {
            // 将dish的相同属性拷贝给dishVO
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);

            // 给dishVO的flavors赋值，得先查询当前dish的Flavor列表
            Long id = dish.getId();
            lqw2.eq(id != null, DishFlavor::getDishId, id);
            List<DishFlavor> dishFlavors = flavorMapper.selectList(lqw2);
            dishVO.setFlavors(dishFlavors);
            dishVOs.add(dishVO);
            lqw2.clear();
        }
        return dishVOs;
    }
}
