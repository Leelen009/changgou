package com.changgou.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;

/**
 * @Author Lee
 * @Date 2021/7/27
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class OrderMessageListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void getMessage(String msg) throws ParseException {
        //1.接收消息(有订单的ID  有transaction_id )
        Map<String, String> resultMap = JSON.parseObject(msg, Map.class);
        System.out.println("监听到的支付结果： " + resultMap);

        //通信标识
        String return_code = resultMap.get("return_code");

        if(return_code.equals("SUCCESS")){
            //业务结果
            String result_code = resultMap.get("result_code");

            //订单号
            String out_trade_no = resultMap.get("out_trade_no");

            //支付成功，修改订单状态
            if(result_code.equals("SUCCESS")){
                //微信支付交易流水号
                orderService.updateStatus(out_trade_no, resultMap.get("time_end"),
                        resultMap.get("transaction_id"));
            } else{
                //支付失败，关闭支付，取消订单，回滚库存
                orderService.deleteOrder(out_trade_no);
            }
        }
    }
}