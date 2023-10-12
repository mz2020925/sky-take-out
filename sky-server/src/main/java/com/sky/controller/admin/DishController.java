package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;


    /**
     * 新增菜品
     *
     * @param dishDTO
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishService.save(dish);
        return Result.success();
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result<String> deleteByIds(String ids) {
        log.info("批量删除菜品：{}", ids);
        dishService.removeByIds(Arrays.asList(ids.split(",").clone()));
        return Result.success();
    }

    /**
     * 菜品起售、停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售停售：{}，菜品id{}", status, id);
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishService.updateById(dish);
        return Result.success();
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishService.updateById(dish);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> selectById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        Dish dish = dishService.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        return Result.success(dishVO);
    }


    /**
     * 根据分类id查询菜品
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List> getByType(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<Dish>();
        lqw.eq(Dish::getCategoryId, categoryId);
        List list = dishService.list(lqw);
        return Result.success(list);
    }

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> getByPage(DishPageQueryDTO dishPageQueryDTO) {
        // 获取起始页码，每页页码数
        IPage<Dish> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        // 获取name参数，type参数
        String name = dishPageQueryDTO.getName();
        Integer categoryId = dishPageQueryDTO.getCategoryId();
        Integer status = dishPageQueryDTO.getStatus();
        // 构建条件
        LambdaQueryWrapper<Dish> lwq = new LambdaQueryWrapper<Dish>();
        lwq.like(name != null, Dish::getName, name)
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(status != null, Dish::getStatus, status)
                .orderByDesc(Dish::getUpdateTime);

        IPage<Dish> iPage = dishService.page(page, lwq);
        PageResult pageResult = new PageResult(iPage.getTotal(), iPage.getRecords());
        return Result.success(pageResult);
    }


}
