package com.sky.controller.admin;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.ReusableMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品分类管理
 */

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类管理相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类接口")
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result<String> deleteById(Long id){
        log.info("根据id删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 启用、禁用分类
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用、禁用分类")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        log.info("启用禁用：{}，分类id{}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 修改分类
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类：{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     */
    @GetMapping("/list")
    @ApiOperation("根据分类类型(套餐分类|菜品分类)查询分类")
    public Result<List> getByType(Integer type){
        log.info("根据类型查询分类：{}", type);
        List list = categoryService.getByType(type);
        return Result.success(list);
    }

    /**
     * 分类分页查询
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> getByPage(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询：{}", categoryPageQueryDTO);
        PageResult data = categoryService.getByPage(categoryPageQueryDTO);
        return Result.success(data);
    }


}
