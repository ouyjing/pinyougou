package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 商家控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-03-03<p>
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    // @Autowired spring容器中的bean
    @Reference(timeout = 10000)
    private SellerService sellerService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 商家入驻 */
    @PostMapping("/save")
    public boolean save(@RequestBody Seller seller){
        try{
            // 密码加密
            String password = passwordEncoder.encode(seller.getPassword());
            seller.setPassword(password);
            sellerService.save(seller);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    //显示商家资料
    @GetMapping("/findSeller")
    public Seller findSeller(){
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        return sellerService.findSeller(sellerId);
    }

    //修改商家资料
    @PostMapping("/update")
    public boolean update(@RequestBody Seller seller){
        try{
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            seller.setSellerId(sellerId);
            sellerService.update(seller);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //判断商家原始密码是否正确
    @PostMapping("/judgePassword")
    public boolean judgePassword(@RequestBody String password){
        Seller seller = findSeller();
        String password2 = seller.getPassword();
        boolean matches = passwordEncoder.matches(password, password2);
        return matches;
    }


    //修改商家密码
    @PostMapping("/changePassword")
    public boolean changePassword(@RequestBody String newPassword){
        try{
            Seller seller = findSeller();
            String password = passwordEncoder.encode(newPassword);
            seller.setPassword(password);
            sellerService.changePassword(seller);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
