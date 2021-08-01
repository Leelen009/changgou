package com.changgou.order.controller;

import com.changgou.order.config.TokenDecode;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车操作
 * @package com.changgou.order.controller *
 */
@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
     private TokenDecode tokenDecode;

    /**
     * 加入购物车
     * @param id  要购买的商品的SKU的ID
     * @param num 要购买的数量
     * @return
     */
    @GetMapping("/add")
    public Result add(Long id, Integer num) {
        //springsecurity 获取当前的用户名 传递service
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");

        //String username = "szitheima";
        System.out.println("哇塞::用户名:"+username);

        cartService.add(id, num, username);
        return new Result(true, StatusCode.OK, "加入购物车成功！");
    }

    /**
     * 购物车列表
     * @return
     */
    @RequestMapping("/list")
    public Result<List<OrderItem>> list() {
        //用户的令牌信息->解析令牌信息->username
        Map<String, String> userInfo = tokenDecode.getUserInfo();
        String username = userInfo.get("username");
        //System.out.println("哇塞::用户名:"+username);

        List<OrderItem> orderItemList = cartService.list(username);
        return new Result<List<OrderItem>>(true, StatusCode.OK, "购物车列表查询成功！", orderItemList);
    }
}
