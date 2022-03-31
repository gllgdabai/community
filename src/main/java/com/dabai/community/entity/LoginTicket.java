package com.dabai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @create 2022-03-30 19:15
 */
@Data
public class LoginTicket {
    private Integer id;
    private Integer userId;
    private String ticket;
    private Integer status;
    private Date expired;
}
