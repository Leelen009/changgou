package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

public interface UserLoginService {

    /**
     * 模拟用户的请求，发送请求，申请令牌，返回
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     * @return
     */
    AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) throws Exception;
}
