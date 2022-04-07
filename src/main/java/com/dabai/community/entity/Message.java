package com.dabai.community.entity;

import lombok.Data;

import java.util.Date;

/** 私信实体类
 * @author
 * @create 2022-04-06 20:43
 */
@Data
public class Message {
    private int id;
    /** fromId为1，则是系统用户，说明是通知 */
    private int fromId;

    private int toId;

    private String conversationId;

    private String content;
    /** 0-未读;1-已读;2-删除; */
    private int status;

    private Date createTime;

}
