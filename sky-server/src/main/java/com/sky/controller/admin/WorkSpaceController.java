package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
@Api(tags = "")
@Slf4j
public class WorkSpaceController {
    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 查询今日运营数据
     *
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("查询今日运营数据")
    public Result<BusinessDataVO> businessData() {
        log.info("查询今日运营数据");
        BusinessDataVO businessDataVO = workSpaceService.businessData(LocalDateTime.of(LocalDate.now(), LocalTime.MIN), LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
        return Result.success(businessDataVO);
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("查询套餐总览")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        log.info("查询套餐总览");
        SetmealOverViewVO setmealOverViewVO = workSpaceService.overviewSetmeals();
        return Result.success(setmealOverViewVO);
    }

    @GetMapping("/overviewDishes")
    @ApiOperation("查询菜品总览")
    public Result<DishOverViewVO> overviewDishes() {
        log.info("查询菜品总览");
        DishOverViewVO dishOverViewVO = workSpaceService.overviewDishes();
        return Result.success(dishOverViewVO);
    }

    @GetMapping("/overviewOrders")
    @ApiOperation("查询订单管理数据")
    public Result overviewOrders() {
        log.info("查询订单管理数据");
        OrderOverViewVO orderOverViewVO = workSpaceService.overviewOrders();

        return Result.success(orderOverViewVO);
    }


}
