package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;


    public BusinessDataVO businessData(LocalDateTime begin, LocalDateTime end) {
        Integer newUsersCount = userMapper.newUsers(begin, end);
        Map map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", null);
        Integer orderCount = orderMapper.ordersStatistics(map);  // 当天订单总数
        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.ordersStatistics(map);  // 有效订单数
        Double turnover = orderMapper.turnoverStatistics(map);  // 营业额

        turnover = turnover == null ? 0.0 : turnover;
        Double unitPrice = 0.0;
        if (validOrderCount != 0) {
            unitPrice = turnover / validOrderCount;  // 平均客单价
        }

        Double orderCompletionRate = 0.0;
        if (orderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / orderCount;  // 订单完成率
        }

        return BusinessDataVO.builder()
                .newUsers(newUsersCount)
                .orderCompletionRate(orderCompletionRate)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .validOrderCount(validOrderCount)
                .build();
    }

    public SetmealOverViewVO overviewSetmeals() {
        Integer discontinued = categoryMapper.overviewSetmeals(0);  // 已停售套餐数量
        Integer sold = categoryMapper.overviewSetmeals(1);  // 已启售套餐数量

        return SetmealOverViewVO.builder()
                .discontinued(discontinued)
                .sold(sold)
                .build();
    }

    public DishOverViewVO overviewDishes() {
        Integer discontinued = dishMapper.overviewDishes(0);  // 已停售菜品数量
        Integer sold = dishMapper.overviewDishes(1);  // 已停售菜品数量
        return DishOverViewVO.builder()
                .discontinued(discontinued)
                .sold(sold)
                .build();
    }

    public OrderOverViewVO overviewOrders() {
        // 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        Integer allOrders = orderMapper.overviewOrders(null);  // 全部订单
        Integer cancelledOrders = orderMapper.overviewOrders(6);  // 已取消数量
        Integer completedOrders = orderMapper.overviewOrders(5);  // 已完成数量
        Integer deliveredOrders = orderMapper.overviewOrders(4);  // 待派送数量
        Integer waitingOrders = orderMapper.overviewOrders(2);  // 待接单数量

        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .build();
    }
}








