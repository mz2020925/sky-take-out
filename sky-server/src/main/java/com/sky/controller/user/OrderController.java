package com.sky.controller.user;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端-订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("C端-订单提交")  // TODO 2.这个接口就是那个流程图的第2步，下单
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) throws Exception {
        log.info("C端-订单提交");
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);  // TODO 3.对应流程图的第3步，返回订单号
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("C端-订单支付")  // TODO 4.对应流程图的第4步，申请微信支付
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("C端-订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("C端-生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
        // TODO 到这一步，之后的是流程图的第9步、第10步和第11步，是小程序和微信后台之间的交互，并不包括商户系统，所以这里没有写后面几步的接口了
    }
    // TODO 9.对应流程图的第9步，小程序拿到第8步的支付参数后，会调起微信支付程序，并请求微信后台
    // TODO 10.微信后台返回给小程序支付结果
    // TODO 11.小程序把支付结果展示出来
    // 上面过程结束后，支付过程已经完成，接下来就是微信后台在把支付结果返回给商户系统，有一种异步的意思在里面
    // TODO 12.微信后台把支付结果推送给商户系统
    // TODO 13.商户系统去 订单表 中更新 这个用户 的 这个订单 的数据

    /**
     * 历史订单分页查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("C端-历史订单分页查询")
    public Result<PageResult> historyOrders(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize, Integer status){  // 这些是Query参数。如果加上了@RequestParam("status")，那么status不能是null
        log.info("C端-历史订单分页查询");
        PageResult pageResult = orderService.historyOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("C端-查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable("id") Long id){
        log.info("C端-查询订单详情");
        OrderVO orderVO = orderService.orderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("C端-取消订单")
    public Result<String> cancel(@PathVariable("id") Long id) throws Exception{
        log.info("C端-取消订单");
        OrdersCancelDTO ordersCancelDTO = new OrdersCancelDTO(id, "用户取消订单");
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("C端-再来一单")
    public Result<String> repetition(@PathVariable("id") Long id){
        // 再来一单的主要操作是将这个订单中的商品加入到购物车中
        log.info("C端-再来一单");
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 催单
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("C端-催单")
    public Result<String> reminder(@PathVariable("id") Long id){
        log.info("C端-催单");
        orderService.reminder(id);
        return Result.success();
    }






}
