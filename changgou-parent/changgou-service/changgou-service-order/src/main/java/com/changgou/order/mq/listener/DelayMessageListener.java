package com.changgou.order.mq.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author Lee
 * @Date 2021/7/27
 */
@Component
@RabbitListener(queues = "orderListenerQueue")
public class DelayMessageListener {

    public void getDelayMessage(String message){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.print("监听消息的时间： " + simpleDateFormat.format(new Date()));
        System.out.println(",监听到的信息： " + message);
    }
}
