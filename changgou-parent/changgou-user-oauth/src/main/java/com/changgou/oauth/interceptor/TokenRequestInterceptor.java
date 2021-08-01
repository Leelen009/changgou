package com.changgou.oauth.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class TokenRequestInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前，进行拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        /*
         * 从数据库加载查询用户信息
         * 1.没有令牌，Feign调用之前，生成令牌（admin）
         * 2.Feign调用之前，令牌需要携带过去
         * 3.Feign调用之前，令牌需要存放到Header文件中
         * 4.请求->Feign调用->拦截器RequestInterceptor->Feign调用之前执行拦截
         */
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
