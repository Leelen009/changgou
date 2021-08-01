package com.changgou.pay.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @Author Lee
 * @Date 2021/7/27
 */
public class MQConfig {
    /**
     * 读取配置文件中的信息的对象
     */
    @Autowired
    private Environment env;

    /**
     * 创建队列
     * @return
     */
    @Bean
    public Queue orderQueue(){
        return new Queue(env.getProperty("mq.pay.queue.order"));
    }

    /**
     * 创建交换机
     * @return
     */
    @Bean
    public Exchange orderExchanger(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"), true, false);
    }

    /**
     * 队列绑定交换机
     * @param orderQueue
     * @param orderExchange
     * @return
     */
    @Bean
    public Binding orderQueueExchange(Queue orderQueue, Exchange orderExchange){
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(env.getProperty("mq.pay.routing.key")).noargs();
    }
}
