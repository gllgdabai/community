package com.dabai.community.dao;

import com.dabai.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author
 * @create 2022-03-28 14:07
 */
@Mapper
public interface UserMapper {
    /**
     *  根据id查询用户
     */
    User selectById(int id);
    /**
     *  根据用户名查询用户
     */
    User selectByName(String username);
    /**
     *  根据邮箱查询用户
     */
    User selectByEmail(String email);
    /**
     *  增加一个用户
     *  @return 插入数据的行数
     */
    int insertUser(User user);
    /**
     *  更新用户的状态
     *  @return 影响的行数
     */
    int updateStatus(int id, int status);

    /**
     *  根据id查询用户，更新该用户的头像
     * @param headerUrl 头像图片的url
     * @return 影响的行数
     */
    int updateHeader(int id, String headerUrl);

    /**
     *  根据id查询用户，更新该用户的密码
     * @param password  新密码
     * @return 影响的行数
     */
    int updatePassword(int id, String password);

}
