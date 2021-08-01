package com.changgou;

import com.netflix.discovery.EurekaNamespace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableEurekaClient
public class GatewayWebApplication {

    public static void main(String[] args) {
        SpringApplication. run(GatewayWebApplication.class, args);
    }

    /**
     * 创建用户唯一标识 使用ip作为用户唯一标识，用于限流操作
     * @return
     */
    @Bean(name = "ipKeyResolver")
    public KeyResolver userKeyResolver(){
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                String ip = exchange.getRequest().getRemoteAddress().getHostString();
                //return Mono.just("需要使用的用户身份识别唯一标识[IP]");
                return Mono.just(ip);
            }
        };
    }
}
