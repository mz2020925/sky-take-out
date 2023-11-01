package com.sky.controller.user;


import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据分类id条件查询套餐
     *
     * @param categoryId
     * @return
     */
    @ApiOperation("根据分类id查询套餐")
    @GetMapping("/list")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    // 1.在方法执行前，spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据；若没有数据，调用方法，并将方法返回值放到缓存中
    // 2.这里的返回值直接就是Result<List>格式的
    // 3.这里缓存的是：键是categoryId，值是该categoryId下的套餐包装成Result<List>格式
    public Result<List> getByCategoryId(@RequestParam Long categoryId) {
        List<Setmeal> setmeals = setmealService.getByCategoryId(categoryId);
        return Result.success(setmeals);
    }

    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    @ApiOperation("根据套餐id查询包含的菜品")
    @GetMapping("/dish/{id}")
    // 如果这里设置缓存的话，应该是：键是setmealId，值是该setmealId下的菜品包装成Result<List>格式
    // 所以如果这里设置缓存菜品的话，那么菜品管理那里凡是涉及到菜品变化的操作都要将缓存清除
    @Cacheable(value = "dishCache", key = "'setmealId'+#id")
    public Result<List> getDishesById(@PathVariable Long id) {
        List<DishItemVO> dishItemVOs = setmealService.getDishesById(id);
        return Result.success(dishItemVOs);
    }

}
