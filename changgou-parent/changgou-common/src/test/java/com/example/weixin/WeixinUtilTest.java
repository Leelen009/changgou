package com.example.weixin;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author Lee
 * @Date 2021/7/27
 */
public class WeixinUtilTest {
    @Test
    public void testDemo() throws Exception {
        //随机字符串
        String str = WXPayUtil.generateNonceStr();
        System.out.println("随机字符串： " + str);

        //将Map转化为XML字符串
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("id", "No.001");
        dataMap.put("title", "畅购商城杯具支付");
        dataMap.put("money", "998");
        String xmlstr = WXPayUtil.generateSignedXml(dataMap, "itcast");
        System.out.println("XML字符串： \n" + xmlstr);
    }
}
