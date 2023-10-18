package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api("套餐相关接口")
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public void save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}", setmealDTO);
        setmealService.save(setmealDTO);
        Result.success();
    }


    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public void delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐：{}", ids);
        setmealService.deleteByIds(ids);
        Result.success();
    }


    @PutMapping
    @ApiOperation("修改套餐")
    public void update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐：{}",setmealDTO);
        setmealService.update(setmealDTO);
        Result.success();

    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售、停售")
    public void startOrStop(@PathVariable Integer status, @RequestParam Long id){
        log.info("套餐起售、停售：{}，套餐id{}", status, id);
        setmealService.startOrStop(status, id);
        Result.success();

    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询")
    public Result<SetmealVO> selectById(@PathVariable Long id){
        log.info("根据id查询：{}", id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmealService.getById(id), setmealVO);
        return Result.success(setmealVO);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> getByPage(@RequestParam SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.getByPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }









}
