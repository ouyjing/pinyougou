package com.pinyougou.service;

import com.pinyougou.pojo.PayLog;
import com.pinyougou.pojo.User;
import java.util.List;
import java.io.Serializable;


/**
 * UserService 服务接口
 * @date 2019-02-27 10:03:32
 * @version 1.0
 */
public interface UserService {

	/** 添加方法 */
	void save(User user);

    /**
     * 修改方法
     */
    void update(String clumnName,User user,String massage);


	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	User findOne(Serializable id);

	/** 查询全部 */
	List<User> findAll();

	/** 多条件分页查询 */
	List<User> findByPage(User user, int page, int rows);

	/** 发送短信验证码 */
	boolean sendSmsCode(String phone);

    /**
     * 检验短信验证码
     */
    boolean checkSmsCode(String phone, String code);

    //生成支付日志
    boolean buildPayLog(String orderId);
    // 根据订单id，从数据库获取支付日志对象
    PayLog findPayLogFromRedis(String orderId);
    // 修改订单状态、更新支付日志
    void updateStatus(String outTradeNo, String transaction_id);

    /**
     * 根据用户名查询信息
     */
    String findByUsername(String clumnName,String loginName);
}