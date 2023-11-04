package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .dishId(dishId)
                .setmealId(setmealId)
                .dishFlavor(dishFlavor)  // 这里的dishFlavor是String类型的字符串
                .userId(userId)
                .build();


        // 构建mysql数据库查询条件
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                .eq(userId != null, ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(lqw);

        // 判断购物车中该订单是否已经存在
        if (shoppingCarts != null && shoppingCarts.size() > 0) {  // 已存在，修改这一行中的“数量”字段+1
            ShoppingCart shoppingCart1 = shoppingCarts.get(0);
            Integer number = shoppingCart1.getNumber();
            shoppingCart1.setNumber(number + 1);
            shoppingCartMapper.updateById(shoppingCart1);
        } else {  // 不存在这一行,难道不可能是 shoppingCarts.size() > 1 吗
            if (dishId != null) {
                // 添加到购物车的是菜品
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                // 添加到购物查的是套餐
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    public List<ShoppingCart> show() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getCurrentId();
        lqw.eq(userId != null, ShoppingCart::getUserId, userId);
        return shoppingCartMapper.selectList(lqw);
    }


    public void clean() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getCurrentId();
        lqw.eq(userId != null, ShoppingCart::getUserId, userId);
        shoppingCartMapper.delete(lqw);
    }


    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long userId = BaseContext.getCurrentId();

        // 构建mysql数据库查询条件
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                .eq(userId != null, ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(lqw);
        // 判断购物车中该订单一定已经存在，下面就是number-1，或者删除这个订单
        if(shoppingCarts != null && shoppingCarts.size() > 0) {
            ShoppingCart shoppingCart1 = shoppingCarts.get(0);
            Integer number = shoppingCart1.getNumber();
            if (number == 1) {
                // 当前商品在购物车中的份数为1，直接删除当前记录
                shoppingCartMapper.deleteById(shoppingCart1);
            } else {
                // number > 1,当前商品在购物车中的份数不为1，修改 份数 - 1
                shoppingCart1.setNumber(number - 1);
                shoppingCartMapper.updateById(shoppingCart1);
            }
        }

    }
}
