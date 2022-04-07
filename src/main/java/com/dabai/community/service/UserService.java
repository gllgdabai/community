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
    Map<String,Object> register(User user, String confirmPassword);

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

    /**
     * 根据ticket 查询 登录凭证login_ticket
     * @param ticket Cookie中存放的ticket
     * @return  登录凭证
     */
    LoginTicket findLoginTicket(String ticket);

    /**
     *  修改用户头像
     * @param userId 用户Id
     * @param headerUrl 新头像的url
     */
    int updateHeader(int userId, String headerUrl);

    /**
     *  修改用户密码
     * @param oldPassword 用户输入的原密码
     * @param newPassword 用户输入的新密码
     * @param confirmPassword 用户输入的确认密码
     * @return 失败消息，说明失败的原因，成功则返回null。
     */
    Map<String, Object> updatePassword(String oldPassword, String newPassword, String confirmPassword);

    /**
     *  根据用户名查询用户
     */
    User findUserByName(String username);
}
