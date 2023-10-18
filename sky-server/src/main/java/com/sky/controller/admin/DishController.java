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
     * @param dishDTO
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        // 新增菜品及后续操作
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result<String> deleteByIds(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);
        // 判断当前菜品是否能够删除---是否存在起售中的菜品？？
        dishService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 菜品起售、停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售停售：{}，菜品id{}", status, id);
        dishService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dishService.getById(id), dishVO);
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
        log.info("分页查询：{}", dishPageQueryDTO);
        PageResult page = dishService.getByPage(dishPageQueryDTO);
        return Result.success(page);
    }

}
