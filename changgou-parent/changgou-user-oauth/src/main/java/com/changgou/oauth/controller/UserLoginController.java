package com.changgou.oauth.controller;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import com.changgou.oauth.util.CookieUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserLoginController {

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Autowired
    private UserLoginService userLoginService;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    private static final String GRANT_TYPE = "password";//授权模式 密码模式

    /**
     * 登录方法
     * 参数传递：
     * 1.账号 username=
     * 2.密码 password=
     * 3.授权方式 grant_type=password
     *
     * 请求头传递
     * 4.Basic Base64（客户端ID：客户端秘钥） Authorization Basic
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/login")
    public Result<Map> login(String username, String password) throws Exception {
        //调用userLoginService实现登录
        AuthToken authToken = userLoginService.login(username, password, clientId, clientSecret, GRANT_TYPE);

        //设置到cookie中
        saveCookie(authToken.getAccessToken());
        return new Result<>(true, StatusCode.OK, "令牌生成成功", authToken);
    }

    /**
     * 将令牌存储到cookie
     * @param token
     */
    private void saveCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse();
        CookieUtil.addCookie(response, cookieDomain, "/", "Authorization", token, cookieMaxAge, false);
    }
}
