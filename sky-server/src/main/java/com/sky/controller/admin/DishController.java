package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDTO
     */
    @PostMapping
    @ApiOperation("新增菜品")
    @CacheEvict(cacheNames = "dishCache", key = "'categoryId_'+#dishDTO.categoryId")
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        // 新增菜品及后续操作
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存
        /*String key = "dish_"+dishDTO.getCategoryId();
        cleanCache(key);*/
        return Result.success();
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<String> deleteByIds(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);
        // 判断当前菜品是否能够删除---是否存在起售中的菜品？？
        dishService.deleteByIds(ids);

        // 将所有的菜品缓存数据清理掉，因为我们无法直接根据ids具体找到批量删除的菜品，在用户端存储redis的时候，设置的键是"dish_"+category
        // 所有以dish_开头的key对应的键值对都要删除
        // cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 菜品起售、停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售停售：{}，菜品id：{}", status, id);
        dishService.startOrStop(status, id);

        // 将所有的菜品缓存数据清理掉，无法直接根据函数参数 status, id 找到具体要清理的菜品
        // cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.update(dishDTO);
        // 将所有的菜品缓存数据清理掉，无法直接根据 函数参数dishDTO 找到具体要清理的菜品
        // cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getById(id);
        // System.out.println(dishVO);
        return Result.success(dishVO);
    }

    /**
     * 根据分类id查询菜品
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List> getByType(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        List<Dish> list = dishService.getByCategoryId(categoryId);
        return Result.success(list);
    }

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> getByPage(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询：{}", dishPageQueryDTO);
        PageResult page = dishService.getByPage(dishPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 清理redis缓存的方法,不用这个，用Spring Cache
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
