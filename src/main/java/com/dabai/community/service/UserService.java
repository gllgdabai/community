package com.dabai.community.service;

import com.dabai.community.entity.User;

/**
 * @author
 * @create 2022-03-28 16:18
 */
public interface UserService {
    /**
     *  根据用户id查询用户
     */
    User findUserById(int id);

}
