package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.BaiduProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.BaiduUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private BaiduUtil baiduUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 支付成功，修改订单状态
     * 工具函数
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.updateOrders(orders);
        //////////////////
        Map map = new HashMap<>();
        map.put("type", 1);  // 消息类型，1表示来单提醒
        map.put("orderId", orders.getId());
        map.put("content", "订单号："+outTradeNo);

        webSocketServer.sendToAllClient(JSON.toJSONString(map));
        //////////////////
    }


    /**
     * 用户下单（提交订单）
     *
     * @param ordersSubmitDTO
     * @return
     * @throws Exception
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) throws Exception {
        // 编写一个业务Controller都要首先判断是否抛出异常：这里是 收货地址为空、超出配送范围、购物车为空
        // 异常情况的处理：收货地址为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());  // 逻辑外键，address_book_id是收货地址表的id列
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ORDER_ADDRESS_BOOK_IS_NULL);
        }
        // 异常情况的处理：超出配送范围
        checkOutOfRange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        // 异常情况的处理：购物车为空
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(lqw);
        if (shoppingCarts == null) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 用户提交订单时，需要往订单表orders中插入一条记录，并且需要往订单详情表order_detail中插入一条或多条记录。
        // 构造并插入订单数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());  // 收货人
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);  // 逻辑外键，当前的用户id
        order.setStatus(Orders.PENDING_PAYMENT);  // 1：待付款
        order.setPayStatus(Orders.UN_PAID);  // 0：未付款
        order.setOrderTime(LocalDateTime.now());  // 下单时间
        orderMapper.insert(order);

        // 构造并插入订单详情数据
        Long ordersId = order.getId();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(ordersId);
            orderDetailMapper.insert(orderDetail);
        }

        // 购物车已经转换成订单，需要删除购物车中的信息
        shoppingCartMapper.delete(lqw);

        // 返回OrderSubmitVO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(ordersId)
                .orderAmount(order.getAmount())
                .orderNumber(order.getNumber())
                .orderTime(order.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);

        // TODO 5.对应流程图的第5步 ~
        // JSONObject jsonObject = weChatPayUtil.pay(
        //         ordersPaymentDTO.getOrderNumber(), // 商户订单号
        //         new BigDecimal(0.01), // 支付金额，单位 元
        //         "苍穹外卖订单", // 商品描述
        //         user.getOpenid() // 微信用户的openid
        // );
        // jsonObject包含着《返回参数》
        // if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {  // TODO 这个jsonObject哪里来的"code"这个键
        //     throw new OrderBusinessException("该订单已支付");
        // }
        /**
         * 因为小程序申请的是个人，所以没有收钱功能，也就是说订单支付功能无法实现验证，
         * 现在想绕过和微信后台的交互,也就是上面注释掉的部分就是第5步到7步。
         * 除此之外，还有第10、11、13、14步需要绕过：在前端小程序代码中将第10步注释掉，直接重定向到第12步，
         * 那么微信后台也不会再有第11步，第13步，没有第13步，就不会有第14步，所以我们需要自己修改订单状态是已支付状态，模拟第14步
         */
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");  // 这里设置订单已经支付成功？？？
        paySuccess(ordersPaymentDTO.getOrderNumber());  // 模拟第14步，把这个订单号对应的订单修改为已支付状态

        // TODO 8.对应流程图的第8步，jsonObject中就存放着 待返回的支付参数
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);  // json对象转换为java对象
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;  // 这里返回的是第8步的支付参数
    }

    /**
     * 查询历史订单
     *
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult historyOrders(int page, int pageSize, Integer status) {
        // 获取起始页码，每页记录数
        IPage<Orders> page1 = new Page<>(page, pageSize);
        // 创建查询条件，并进行分页查询
        LambdaQueryWrapper<Orders> lqw1 = new LambdaQueryWrapper<>();
        Long currentId = BaseContext.getCurrentId();
        lqw1.eq(currentId != null, Orders::getUserId, currentId)
                .eq(status != null, Orders::getStatus, status)
                .orderByDesc(Orders::getOrderTime);
        IPage<Orders> iPage = orderMapper.selectPage(page1, lqw1);

        // 根据数据库查询结果，封装成返回给前端的格式（返回格式在Api文档中写的很详细）
        List<Orders> records = iPage.getRecords();
        List<OrderVO> orderVOS = new ArrayList<>();
        LambdaQueryWrapper<OrderDetail> lqw2 = new LambdaQueryWrapper<>();
        for (Orders record : records) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(record, orderVO);  // 完成一条数据的Orders转换为OrderVO

            // 设置orderVO中orderDetailList属性的值
            Long orderId = orderVO.getId();
            lqw2.eq(orderId != null, OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(lqw2);  // 根据订单id 查询 orderDetailList
            orderVO.setOrderDetailList(orderDetails);
            orderVOS.add(orderVO);  // 返回给前端的格式是{"code": 0,"msg": null,"data": {"total": 0,"records": []}}，我们封装的orderVOS就是"records"
            lqw2.clear();
        }

        // 返回PageResult对象
        return new PageResult(iPage.getTotal(), orderVOS);
    }

    public OrderVO orderDetail(Long id) {
        // 根据订单id查询订单，并将相同信息封装到orderVO
        Orders order = orderMapper.selectById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);

        // 根据订单id查询订单详情，并封装到orderVO
        LambdaQueryWrapper<OrderDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(lqw);

        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Long id = ordersCancelDTO.getId();
        String cancelReason = ordersCancelDTO.getCancelReason();

        Orders orderOrigin = orderMapper.selectById(id);
        if (orderOrigin == null) {  // 这种情况我觉得不可能发生，因为前端是通过点击一个订单来取消订单，那么这个订单怎么可能不存在呢
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        Integer status = orderOrigin.getStatus();
        if (status.equals(Orders.COMPLETED) || status.equals(Orders.CANCELLED)) {  // 订单状态是5、6，不需要
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        if (status.equals(Orders.CONFIRMED) || status.equals(Orders.DELIVERY_IN_PROGRESS)) {  // 商家已接单状态下，用户取消订单需电话沟通商家
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_WARN);
        }

        Orders orderCanceled = new Orders();
        orderCanceled.setId(id);
        if (status.equals(Orders.TO_BE_CONFIRMED)) {  // 如果在待接单状态下取消订单，用户可直接取消订单，需要给用户退款
            // 调用微信支付退款接口
            // weChatPayUtil.refund(
            //         orderOrigin.getNumber(), //商户订单号
            //         orderOrigin.getNumber(), //商户退款单号
            //         new BigDecimal(0.01),//退款金额，单位 元
            //         new BigDecimal(0.01));//原订单金额

            // 支付状态修改为 退款
            orderCanceled.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orderCanceled.setStatus(Orders.CANCELLED);
        orderCanceled.setCancelReason(cancelReason);
        orderCanceled.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(orderCanceled);
    }

    public void repetition(Long id) {
        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(id);
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 将订单详情对象列表 转换为 购物车对象列表，可以使用流程编程。
        // 什么情况下使用流式编程呢？就是最后需要总结返回结果的遍历。
        // 如果只需要遍历不用总结返回结果，就用for循环；列表.forEach；for(~:~)。
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart, "id");  // 将原订单详情里面的菜品信息重新拷贝到购物车对象中，除了"id"属性
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());


        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    public void reminder(Long id) {

    }

    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 获取起始页码，每页记录数
        IPage<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        // 创建查询条件，并进行分页查询
        LambdaQueryWrapper<Orders> lqw1 = new LambdaQueryWrapper<>();
        LocalDateTime beginTime = ordersPageQueryDTO.getBeginTime();
        LocalDateTime endTime = ordersPageQueryDTO.getEndTime();
        String number = ordersPageQueryDTO.getNumber();
        String phone = ordersPageQueryDTO.getPhone();
        Integer status = ordersPageQueryDTO.getStatus();
        lqw1.gt(beginTime != null, Orders::getOrderTime, beginTime)
                .lt(endTime != null, Orders::getOrderTime, endTime)
                .like(number != null, Orders::getNumber, number)
                .like(phone != null, Orders::getPhone, phone)
                .eq(status != null, Orders::getStatus, status)
                .orderByDesc(Orders::getOrderTime);
        IPage<Orders> iPage = orderMapper.selectPage(page, lqw1);
        // 根据数据库查询结果，封装成返回给前端的格式（返回格式在Api文档中写的很详细）
        List<Orders> records = iPage.getRecords();
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        LambdaQueryWrapper<OrderDetail> lqw2 = new LambdaQueryWrapper<>();
        // 将分页查询结果封装成返回体格式
        for (Orders record : records) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(record, orderVO);
            // 设置orderVO的orderDishes属性的值
            lqw2.eq(OrderDetail::getOrderId, record.getId());
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(lqw2);
            List<String> orderDishesList = orderDetails.stream().map(x -> {
                return x.getName() + "*" + x.getNumber() + ";";
            }).collect(Collectors.toList());
            String orderDishes = String.join("", orderDishesList);
            orderVO.setOrderDishes(orderDishes);
            orderVOS.add(orderVO);
            lqw2.clear();
        }

        return new PageResult(iPage.getTotal(), orderVOS);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.statistics(Orders.TO_BE_CONFIRMED);  // 待接单
        Integer confirmed = orderMapper.statistics(Orders.CONFIRMED);  // 已接单（带派送）
        Integer deliveryInProgress = orderMapper.statistics(Orders.DELIVERY_IN_PROGRESS);  // 派送中
        return new OrderStatisticsVO(toBeConfirmed, confirmed, deliveryInProgress);
    }

    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Long id = ordersConfirmDTO.getId();
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getId, id);
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.updateById(orders);
    }

    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Long id = ordersRejectionDTO.getId();
        String rejectionReason = ordersRejectionDTO.getRejectionReason();

        // 查出待修改的订单记录，并取出 状态属性、和支付状态属性
        Orders orderOrigin = orderMapper.selectById(id);
        if (orderOrigin == null) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        Integer status = orderOrigin.getStatus();
        Integer payStatus = orderOrigin.getPayStatus();

        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        // 只有订单处于“待接单”状态时可以执行拒单操作,其他状态抛出异常
        if (!status.equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 下面是订单处于“待接单”状态时，此时订单支付状态一定是已付款
        Orders orderRejected = new Orders();
        orderRejected.setId(id);
        orderRejected.setRejectionReason(rejectionReason);
        // 商家拒单其实就是将订单状态修改为“已取消”
        orderRejected.setStatus(Orders.CANCELLED);
        orderRejected.setCancelTime(LocalDateTime.now());

        if (payStatus.equals(Orders.PAID)) {
            // 调用微信支付退款接口
            // weChatPayUtil.refund(
            //         order.getNumber(), //商户订单号
            //         order.getNumber(), //商户退款单号
            //         new BigDecimal(0.01),//退款金额，单位 元
            //         new BigDecimal(0.01));//原订单金额

            // 支付状态修改为 退款
            orderRejected.setPayStatus(Orders.REFUND);
        }
        orderMapper.updateById(orderRejected);
    }

    public void delivery(Long id) {
        // 查出待修改的订单记录，并取出 状态属性、和支付状态属性
        Orders orderOrigin = orderMapper.selectById(id);
        if (orderOrigin == null) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        Integer status = orderOrigin.getStatus();
        // 订单状态 1待付款 2待接单 3已接单(待派送) 4派送中 5已完成 6已取消
        // 只有状态为“待派送”的订单可以执行派送订单操作
        if (!status.equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orderConfirmed = new Orders();
        orderConfirmed.setId(id);
        // 派送订单其实就是将订单状态修改为“派送中”
        orderConfirmed.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateById(orderConfirmed);
    }

    public void complete(Long id) {
        // 查出待修改的订单记录，并取出 状态属性、和支付状态属性
        Orders orderOrigin = orderMapper.selectById(id);
        if (orderOrigin == null) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        Integer status = orderOrigin.getStatus();
        // 订单状态 1待付款 2待接单 3已接单(待派送) 4派送中 5已完成 6已取消
        // 只有状态为“派送中”的订单可以执行订单完成操作
        if (!status.equals(Orders.DELIVERY_IN_PROGRESS))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        Orders orderComplete = new Orders();
        orderComplete.setId(id);
        // 完成订单其实就是将订单状态修改为“已完成”
        orderComplete.setStatus(Orders.COMPLETED);
        orderMapper.updateById(orderComplete);
    }

    /**
     * 检查客户的收货地址是否超出配送范围
     *
     * @param address
     */
    public void checkOutOfRange(String address) throws Exception {
        String shopAddress = baiduUtil.getBaiduProperties().getShopAddress();
        // 转换经纬度
        String origin = baiduUtil.getCoordinate(shopAddress);
        String destination = baiduUtil.getCoordinate(address);
        // 计算距离
        double distance = baiduUtil.getDistance(origin, destination);
        if (distance>5000) throw new OrderBusinessException(MessageConstant.ORDER_OUT_OF_RANGE);  // 这个地方前端没有给弹窗提示，什么提示也没有。
    }
}

