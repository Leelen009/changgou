package com.changgou.oauth.service.Impl;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    //实现请求发送
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 登录实现
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     * 参数传递：
     * 1.账号 username=
     * 2.密码 password=
     * 3.授权方式 grant_type=password
     * 请求头传递
     * 4.Basic Base64（客户端ID：客户端秘钥） Authorization Basic
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) throws Exception {
        //获取指定服务器的注册数据
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");

        //调用的请求地址 http://localhost:9001/oauth/token
        String url = //serviceInstance.getUri().toString() + "/oauth/token";
                     "http://localhost:9001/oauth/token";
        System.out.println(url);
        //请求提交的数据封装 1.2.3
        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<> ();
        parameterMap.add("username", username);
        parameterMap.add("password", password);
        parameterMap.add("grant_type", grant_type);

        //4.请求头封装
        String Authorization = httpbasic(clientId, clientSecret);
        MultiValueMap headerMap = new LinkedMultiValueMap();
        headerMap.add("Authorization", Authorization);

        //HttpEntity->创建该对象，封装了请求头和请求体
        HttpEntity httpEntity = new HttpEntity(parameterMap, headerMap);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException{
                if(response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401){
                    super.handleError(response);
                }
            }
        });

        /**
         * exchange参数
         * 1.请求地址
         * 2.提交方式
         * 3.requestEntity：请求提交的数据信息封装，请求体|请求头
         * 4.responseType：返回数据需要转换的类型
         */
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        System.out.println(responseEntity.getBody());
        //接收到返回的响应（令牌的信息）
        Map<String, String> body = responseEntity.getBody();

        //将响应数据封装成AuthToken对象
        AuthToken authToken = new AuthToken();
        //访问令牌
        String accessToken = (String) body.get("access_token");
        //刷新令牌
        String refreshToken = (String) body.get("refresh_token");
        //jti，作为用户的身份标识
        String jwtToken = (String) body.get("jti");

        authToken.setJti(jwtToken);
        authToken.setAccessToken(accessToken);
        authToken.setRefreshToken(refreshToken);

        return authToken;
    }

    /**
     * base64编码
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String httpbasic(String clientId, String clientSecret) throws Exception {
        //将客户端id和客户端密码拼接，按“客户端id：客户端密码”
        String string = clientId + ":" + clientSecret;
        //进行base64编码
        byte[] encode = Base64.getEncoder().encode(string.getBytes());
        return "Basic " + new String(encode, "UTF-8");
    }

    public static void main(String[] args) {
        byte[] decode = Base64.getDecoder().decode(new String("Y2hhbmdnb3U6Y2hhbmdnb3U=").getBytes());
        System.out.println(new String(decode));
    }
}
