package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
            String oldPassword =(String) content.get("oldPassword");
            String md5OldPassword = DigestUtils.md5Hex(oldPassword);
            String password = (String) content.get("password");
            SecurityContext context = SecurityContextHolder.getContext();
            String loginName = context.getAuthentication().getName();
            String okPassword = userService.findByUsername(loginName);
            if (md5OldPassword.equals(okPassword)){
                try {
                    User user = new User();
                    user.setPassword(password);
                    user.setUsername(loginName);
                    userService.update(user);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }else {
                return false;
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
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
}
