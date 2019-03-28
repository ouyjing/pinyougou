package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;
import com.pinyougou.pojo.User;

import java.util.Date;

/**
 * UserMapper 数据访问接口
 *
 * @version 1.0
 * @date 2019-02-27 09:55:07
 */
public interface UserMapper extends Mapper<User> {

    /**
     * 根据用户名查询信息
     */
    String findByUsername(@Param("clumnName") String clumnName, @Param("loginName")String loginName);

    /**
     * 修改用户信息
     */
    void updateUser(@Param("clumnName") String clumnName,@Param("updated")Date updated,
                    @Param("username")String username,@Param("massage") String massage);
}