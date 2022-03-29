package com.dabai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @create 2022-03-28 14:06
 */
@Data
public class User {

    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;

}
