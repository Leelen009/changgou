package com.changgou.oauth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌的创建和解析
 */
public class CreateJwtTestDemo {

    /**
     * 创建令牌
     */
    @Test
    public void testCreateToken(){
        //加载证书 读取类路径中的文件
        ClassPathResource resource = new ClassPathResource("changgou1.jks");
        //System.out.println(resource);
        //读取证书数据 加载读取证书数据
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, "changgou1".toCharArray());
        //获取证书的一对私钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou1", "changgou1".toCharArray());
        //获取私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //创建令牌，需要私钥加盐（RSA算法）
        Map<String, Object> payload = new HashMap<>();
        payload.put("nikename", "tomcat");
        payload.put("address", "gz");
        payload.put("role", "admin,user");

        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        //获取令牌数据
        String token = jwt.getEncoded();
        System.out.println(token);
    }

    /**
     * 解析令牌
     */
    @Test
    public void testParseToken(){
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoiZ3oiLCJyb2xlIjoiYWRtaW4sdXNlciIsIm5pa2VuYW1lIjoidG9tY2F0In0.nHWSaqMcAwnF4FILSbBqxFJfILPrD2q7BNhA2faiWOR4JzRmZ2Q2RsmGnZZaRoT0a37JAxGKzdzJUqZemML0ysogeLk06DtIBCSCYdr0B0uBq4JcJ1Aw7uO2HmccJSOb9jg4zScF_bEntSPAGgLKL9cFOZ-Gf7D9F2bpb3D-2c79Oy7cyvQjwmibTK6vpS5ZigLVVbD3rm3A86KPRHxZCuraGWh-08rE9enE7dnfI7HEQxOGaMISSL0r_2zovl-BTqpsu_WHNauUoo8q-nGPYmS-Z0kfNMi4f_fykP5u3DU9usQWBcEqZOA4uOZ4489cdHdvl8uOiC2LYdiy46AM0Q";
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApW72zpWrax/RW+Xx2PWXp9fnYSnYCIJ73G+hiie8ZuXjD8bVZ85vAKaRV/V0elaSQWUFq1pdMcJcErPZc/WAhfySQhKevGDdgJG5l8qsY0d4+w7SgdCDQAyrYvl7k/dC+urpPwnB0uLIZTq73mBBCqQB67vlIqhAXAcYO66vJQHjaBiiCnKyBpC+u/dNznAVrvid9sQ5zHUjsuHo21aaOY1mhI74XCgWRJVOTDknxr/tdPw5jo6a7a6Shkaze51EFcKpAHi4rrbxa+9qLs6NUdShm8PINi3xMWxyNOMzHt8o/pFrcTDMqtEf82XtOpp6E3zaikkG+UwbTykZGQTB/wIDAQAB-----END PUBLIC KEY-----";

        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
        String claims = jwt.getClaims();
        System.out.println(claims);
    }
}
