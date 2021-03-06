package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-03-16<p>
 */
@Service(interfaceName = "com.pinyougou.service.UserService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${sms.url}")
    private String smsUrl;
    @Value("${sms.signName}")
    private String signName;
    @Value("${sms.templateCode}")
    private String templateCode;

    @Override
    public void save(User user) {
        try{
            // 密码需要MD5加密 commons-codec-xxx.jar
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            // 创建时间
            user.setCreated(new Date());
            // 修改时间
            user.setUpdated(user.getCreated());
            userMapper.insertSelective(user);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(String clumnName,User user,String massage) {
        try{
            // 修改时间
            user.setUpdated(new Date());
           userMapper.updateUser(clumnName,user.getUpdated(),user.getUsername(),massage);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public User findOne(Serializable id) {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public List<User> findByPage(User user, int page, int rows) {
        return null;
    }

    /** 发送短信验证码 */
    public boolean sendSmsCode(String phone){
        try{
            // 1. 生成6位随机数字的验证码
            String code = UUID.randomUUID().toString().replaceAll("-","")
                    .replaceAll("[a-zA-Z]","").substring(0,6);
            System.out.println("code: " + code);

            // 2. 调用短信发送接口
            // 创建 HttpClientUtils对象
            HttpClientUtils httpClientUtils = new HttpClientUtils(false);
            // 定义Map集合封装请求参数
            Map<String, String> params = new HashMap<>();
            params.put("phone", phone);
            params.put("signName", signName);
            params.put("templateCode", templateCode);
            params.put("templateParam", "{\"code\":\""+ code +"\"}");
            // 调用短信接口
            String content = httpClientUtils.sendPost(smsUrl, params);
            System.out.println(content);

            // 3. 如果发送成功，把验证码存储到Redis数据库
            Map map = JSON.parseObject(content, Map.class);
            boolean success = (boolean)map.get("success");
            if (success){
                // 把验证码存储到Redis数据库，有效期90秒
                redisTemplate.boundValueOps(phone).set(code, 90, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 检验短信验证码 */
    public boolean checkSmsCode(String phone, String code){
        try{
            // 从Redis数据库获取短信验证码
            String oldCode = (String)redisTemplate.boundValueOps(phone).get();
            return oldCode != null && oldCode.equals(code);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Autowired
    private PayLogMapper payLogMapper;
    @Autowired
    private OrderMapper orderMapper;



    @Override
    public boolean buildPayLog(String orderId ) {
        try {
            IdWorker idWorker = new IdWorker();
            Order order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
            payLog.setCreateTime(new Date());
            payLog.setPayTime(new Date());
            BigDecimal payment = order.getPayment();
            Double payMoney =new Double(String.valueOf(payment)) ;
            Double payMoneys = payMoney*100;
            payLog.setTotalFee(BigDecimal.valueOf(payMoneys));
            payLog.setUserId(order.getUserId());
            payLog.setTradeState("1");
            payLog.setOrderList(orderId);
            payLog.setPayType("1");
            payLogMapper.insertSelective(payLog);
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    // 根据订单id，从数据库获取支付日志对象
    @Override
    public PayLog findPayLogFromRedis(String orderId) {
        Example example = new Example(PayLog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLike("orderList",orderId);
        List<PayLog> payLogs = payLogMapper.selectByExample(example);
        return payLogs.get(0);
    }
    // 修改订单支付状态、修改支付日志的支付状态
    @Override
    public void updateStatus(String outTradeNo, String transaction_id) {
        try {
            PayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
            //更新订单表
            String orders = payLog.getOrderList();
            String s = "[" + orders + "]";
            List<Long> orderList = JSON.parseObject(s, List.class);
            for (Long orderId : orderList) {
                Order order = orderMapper.selectByPrimaryKey(orderId);
                order.setStatus("2");
                order.setUpdateTime(new Date());
                order.setPaymentTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
            }
            //更新支付日志表
            payLog.setPayTime(new Date());
            payLog.setTradeState("2");
            payLogMapper.updateByPrimaryKeySelective(payLog);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    /**
     * 根据用户名查询信息
     */
    public String findByUsername(String clumnName,String loginName){
        try {
            return userMapper.findByUsername(clumnName,loginName);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
