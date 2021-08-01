package com.changgou.pay.service;

import java.io.IOException;
import java.util.Map;

/**
 * @Author Lee
 * @Date 2021/7/27
 */
public interface WeixinPayService {

    /**
     * 生成二维码
     * @param parameterMap
     * @return
     */
    Map createNative(Map<String, String> parameterMap);

    /**
     * 查询微信支付状态
     * @param outtradeno
     * @return
     */
    Map queryStatus(String outtradeno);
}
