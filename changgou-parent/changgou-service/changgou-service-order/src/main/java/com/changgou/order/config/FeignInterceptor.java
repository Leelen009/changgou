package com.changgou.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 描述
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.order.config *
 * @since 1.0
 */
@Component
public class FeignInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前，进行拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {

        //记录了当前用户请求的所有数据，包含请求头和请求参数
        //用户当前请求的时候对应线程的数据，如果开启了熔断，默认是线程池隔离，会开启新的线程，
        //需要将熔断策略换成信号量隔离，此时不会开启新的线程
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (requestAttributes != null) {
            //1.获取请求对象
            HttpServletRequest request = requestAttributes.getRequest();

            /**
             * 获取请求头中的数据
             * 获取所有头的名字
             */
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                //2.获取请求对象中的所有的头信息(网关传递过来的)
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();//头的名称
                    String value = request.getHeader(name);//头名称对应的值
                    System.out.println("name:" + name + "::::::::value:" + value);

                    //3.将头信息传递给fegin (requestTemplate) 使用Feign调用的时候，会传递给下一个微服务
                    requestTemplate.header(name,value);
                }
            }
        }


    }
}
