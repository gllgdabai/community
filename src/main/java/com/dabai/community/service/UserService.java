package com.dabai.community.service;

import com.dabai.community.entity.LoginTicket;
import com.dabai.community.entity.User;

import java.util.Map;

/**
 * @author
 * @create 2022-03-28 16:18
 */
public interface UserService {
    /**
     *  根据用户id查询用户
     */
    User findUserById(int id);

    /**
     *  注册用户
     * @return  失败消息，说明失败的原因，成功则返回null。
     */
    Map<String,Object> register(User user);

    /**
     *  激活用户
     * @param userId 用户Id
     * @param code  激活码
     * @return  激活类型，成功/失败
     */
    int activation(int userId, String code);

    /**
     *  用户登录
     * @param username 用户名
     * @param password 密码
     * @param expiredSeconds 失效时间
     * @return 失败消息，说明失败的原因，成功则返回null。
     */
    Map<String, Object> login(String username, String password, int expiredSeconds);

    /**
     *  退出登录
     * @param ticket cookie中存有登录凭证ticket
     */
    void logout(String ticket);

    LoginTicket findLoginTicket(String ticket);

}
