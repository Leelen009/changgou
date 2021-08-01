package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @Author Lee
 * @Date 2021/7/27
 */
@RestController
@RequestMapping(value = "/weixin/pay")
public class WeixinPayController {
    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建支付二维码
     * @param parameterMap
     * @return
     */
    @RequestMapping("/create/native")
    public Result createNative(@RequestParam Map<String, String> parameterMap){
        Map<String, String> resultMap = weixinPayService.createNative(parameterMap);
        return new Result(true, StatusCode.OK, "创建二维码预付订单成功!", resultMap);
    }

    /**
     * 查询支付状态
     * @param outtradeno
     * @return
     */
    @RequestMapping("/status/query")
    public Result queryStatus(@RequestParam String outtradeno){
        //查询支付状态
        Map map = weixinPayService.queryStatus(outtradeno);
        return new Result(true, StatusCode.OK, "查询支付状态成功！", map);
    }

    /**
     * 支付结果通知回调方法
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/notify/url")
    public String notifyurl(HttpServletRequest request) throws Exception{
        //获取网络输入流
        ServletInputStream is = request.getInputStream();
        //缓冲区byte[] buffer = new byte[1024]
        byte[] buffer = new byte[1024];
        //创建一个OutputStream -> 输入文件中
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int len = 0;
        while((len = is.read(buffer)) != -1){
            baos.write(buffer, 0, len);
        }

        //支付结果的字节数据
        byte[] bytes = baos.toByteArray();
        String xmlresult = new String(bytes, "UTF-8");

        //XML字符串 -> Map
        Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlresult);

        //发送支付结果给MQ
        rabbitTemplate.convertAndSend("exchange.order", "queue.order",
                JSON.toJSONString(resultMap));

        String result = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        return result;
    }
}
