package com.sky.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class OrderTask {

    @Scheduled(cron = "0****?")
    public void processTimeOutOrder(){
        log.info("处理支付超时的订单：{}",new Date());


    }

    @Scheduled(cron = "001**?")
    public void processDeliveryOrder(){
        log.info("凌晨1点处理仍是派送中的订单：{}",new Date());

    }

}
