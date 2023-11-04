package com.sky.controller.user;


import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "C端-菜品浏览接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    @Cacheable(cacheNames = "dishCache", key = "'categoryId_'+#categoryId")
    public Result<List<DishVO>> getDishVOByCategoryId(@RequestParam Long categoryId) {
        log.info("C端-根据分类id查询菜品：{}", categoryId);
        /*// 构造redis中的key，构造规则是dish_分类id，然后查询redis中是否存在 dish_分类id
        String key = "dish_" + categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);  //TODO 这里是如何转换的？？？
        if (list != null && list.size() > 0) {
            // 如果存在，直接返回，无须查询数据库
            return Result.success(list);
        }*/

        // redis中没有，从数据库中查询，查完之后先放到redis中，然后再返回
        List<DishVO> list = dishService.getDishVOByCategoryId(categoryId);
        /*redisTemplate.opsForValue().set(key, list);*/
        return Result.success(list);
    }
}
