package com.changgou.order.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Exchanger;

/**
 * 延时队列
 * @Author Lee
 * @Date 2021/7/27
 */
@Configuration
public class QueueConfig {

    /**
     * 创建Queue1 延时队列，会过期，过期后将数据发给Queue2
     * @return
     */
    @Bean
    public Queue orderDelayMessage(){
        return QueueBuilder
                .durable("orderDelayMessage")
                .withArgument("x-dead-letter-exchange", "orderListenerExchange") //死信队列数绑定到交换机
                .withArgument("x-dead-letter-routing-key", "orderListenerQueue")
                .build();
    }

    /**
     * 创建Queue2
     * @return
     */
    @Bean
    public Queue orderListenerQueue(){
        return new Queue("orderListenerQueue", true);
    }

    /**
     * 创建交换机
     * @return
     */
    @Bean
    public Exchange orderListenerExchange(){
        return new DirectExchange("orderListenerExchange");
    }

    /**
     * 队列Queue2绑定Exchange
     * @return
     */
    @Bean
    public Binding orderListenerBinding(Queue orderListenerQueue, Exchange orderListenerExchange){
        return BindingBuilder.bind(orderListenerQueue).to(orderListenerExchange)
                .with("orderListenerQueue").noargs();
    }
}
