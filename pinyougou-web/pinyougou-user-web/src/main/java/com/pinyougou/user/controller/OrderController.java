package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import com.pinyougou.service.UserService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Reference(timeout = 10000)
    private UserService userService;
    @Reference(timeout = 10000)
    private OrderService orderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    @Autowired
    private HttpServletRequest request;
    //查询订单
    @GetMapping("/findOrderByUserId")
    public List<Map<String,Object>> findOrderByUserId(){
        String userId = request.getRemoteUser();
        List<Map<String,Object>> orderByUserId = orderService.findOrderByUserId(userId);
        return orderByUserId;
    }

    //生成支付日志
    @GetMapping("/buildPayLog")
    public boolean buildPayLog(String orderId){
        return userService.buildPayLog(orderId);
    }

    /** 生成微信支付的二维码 */
    @GetMapping("/genPayCode")
    public Map<String,Object> genPayCode(String orderId){

        // 根据订单id，从数据库获取支付日志对象
        PayLog payLog = userService.findPayLogFromRedis(orderId);
        BigDecimal totalFee = payLog.getTotalFee();
        // 调用微信支付服务接口
        Map<String, Object> stringObjectMap = weixinPayService.genPayCode(payLog.getOutTradeNo(), String.valueOf(payLog.getTotalFee()));
        return stringObjectMap;
    }

    /** 检测支付状态 */
    @GetMapping("/queryPayStatus")
    public Map<String, Integer> queryPayStatus(String outTradeNo){
        Map<String, Integer> data = new HashMap<>();
        data.put("status", 3);
        try{
            // 调用微信支付服务接口
            Map<String, String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            // 判断支付状态
            if (resMap != null && resMap.size() > 0){
                if ("SUCCESS".equals(resMap.get("trade_state"))){ // 支付成功

                    // 支付成功(业务处理)
                    // 修改支付状态、订单状态、删除支付日志
                    userService.updateStatus(outTradeNo, resMap.get("transaction_id"));

                    data.put("status", 1);
                }
                if ("NOTPAY".equals(resMap.get("trade_state"))){ // 未支付
                    data.put("status", 2);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }

}
