package com.dabai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @create 2022-03-28 14:06
 */
@Data
public class User {
    /**
     * 主键Id
     */
    private int id;
    /**
     *  用户名
     */
    private String username;
    /**
     *  密码
     */
    private String password;
    /**
     * 盐值：用于对密码进行MD5加密
     */
    private String salt;
    /**
     * 邮箱
     */
    private String email;
    /**
     *  用户类型：0-普通用户; 1-超级管理员; 2-版主
     */
    private int type;
    /**
     * 是否激活：0-未激活; 1-已激活
     */
    private int status;
    /**
     *  激活码
     */
    private String activationCode;
    /**
     *  用户头像的url
     */
    private String headerUrl;
    /**
     * 创建时间
     */
    private Date createTime;

}
