package com.dabai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @create 2022-03-30 19:15
 */
@Data
public class LoginTicket {
    /** 主键Id */
    private Integer id;
    /** 用户Id */
    private Integer userId;

    private String ticket;
    /** 状态，0-有效; 1-无效 */
    private Integer status;
    /** 过期时间 */
    private Date expired;
}
