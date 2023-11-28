package com.sky.task;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder() {
        log.info("定时处理 - 是否存在支付超时的订单：{}", new Date());
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .lt(Orders::getOrderTime, LocalDateTime.now().plusMinutes(-15));
        List<Orders> orders = orderMapper.selectList(lqw);
        if (orders != null && orders.size() > 0) {

            orders.forEach(x -> {
                x.setStatus(Orders.CANCELLED);
                x.setCancelTime(LocalDateTime.now());
                x.setCancelReason(MessageConstant.ORDER_TIME_OUT);
                orderMapper.updateById(x);
            });
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理 - 是否存在仍是派送中的订单：{}", new Date());
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                .lt(Orders::getOrderTime, LocalDateTime.now().plusMinutes(-60));  // 凌晨1点的时候查询昨天还有没有派送中的订单，如果有，就修改为已完成
        List<Orders> orders = orderMapper.selectList(lqw);
        if (orders != null && orders.size() > 0) {

            orders.forEach(x -> {
                x.setStatus(Orders.COMPLETED);
                orderMapper.updateById(x);
            });
        }

    }

}
