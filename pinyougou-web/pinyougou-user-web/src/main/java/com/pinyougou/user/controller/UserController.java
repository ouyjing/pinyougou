package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-03-16<p>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Reference(timeout = 10000)
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/save")
    public boolean save(@RequestBody User user, String code) {
        try {
            // 检验短信验证码
            boolean flag = userService.checkSmsCode(user.getPhone(), code);
            if (flag) {
                userService.save(user);
            }
            return flag;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 更改密码
     */
    @PostMapping("/updatePassword")
    public boolean updatePassword(@RequestBody Map<String, Object> content) {
        try {
            String oldPassword = (String) content.get("oldPassword");
            String md5OldPassword = DigestUtils.md5Hex(oldPassword);
            String password = (String) content.get("password");
            String loginName = this.getLoginName();
            String okPassword = userService.findByUsername("password", loginName);
            if (md5OldPassword.equals(okPassword)) {
                try {
                    User user = new User();
                    // 密码需要MD5加密 commons-codec-xxx.jar
                    String md5Password = DigestUtils.md5Hex(password);
                    user.setPassword(md5Password);
                    user.setUsername(loginName);

                    userService.update("password", user, md5Password);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * 获取原手机号
     */
    @GetMapping("/findPhoneNumber")
    public Map<String, Object> findPhoneNumber() {
        String loginName = this.getLoginName();
        String phone = userService.findByUsername("phone", loginName);
        String num1 = phone.substring(0, 3);
        String num2 = phone.substring(7);
        String showNumber = num1 + "****" + num2;
        Map<String, Object> map = new HashMap<>();
        map.put("phone", phone);
        map.put("showNumber", showNumber);
        return map;
    }

    /**
     * 检验身份
     */
    @PostMapping("/checkPhoneNumber")
    public boolean checkPhoneNumber(@RequestBody User user, String code, String phoneCode, HttpServletRequest request) {
        // 判断请求方式
        if ("post".equalsIgnoreCase(request.getMethod())) {
            // 判断验证码
            String oldCode = (String) request.getSession().getAttribute(VerifyController.VERIFY_CODE);
            if (code != null && code.equalsIgnoreCase(oldCode)) {
                try {
                    // 检验短信验证码
                    return userService.checkSmsCode(user.getPhone(), phoneCode);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 发送短信验证码
     */
    @GetMapping("/sendSmsCode")
    public boolean sendSmsCode(String phone) {
        try {
            return userService.sendSmsCode(phone);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 更新手机号
     */
    @PostMapping("/updatePhoneNumber")
    public boolean updatePhoneNumber(@RequestBody User user, String code, String phoneCode, HttpServletRequest request) {
        // 判断请求方式
        if ("post".equalsIgnoreCase(request.getMethod())) {
            // 判断验证码
            String oldCode = (String) request.getSession().getAttribute(VerifyController.VERIFY_CODE);
            if (code != null && code.equalsIgnoreCase(oldCode)) {
                try {
                    // 检验短信验证码
                    boolean flag = userService.checkSmsCode(user.getPhone(), phoneCode);
                    if (flag) {
                        String loginName = this.getLoginName();
                        user.setUsername(loginName);
                        userService.update("phone", user, user.getPhone());
                    }
                    return flag;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    public String getLoginName() {
        SecurityContext context = SecurityContextHolder.getContext();

        return context.getAuthentication().getName();
    }
}
