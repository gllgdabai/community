package com.dabai.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @create 2022-04-04 20:57
 */
@Data
public class Comment {
    /** 主键Id */
    private int id;
    /** 用户Id */
    private int userId;
    /** 实体类型，评论目标的类型 */
    private int entityType;
    /** 实体Id，评论目标的Id */
    private int entityId;
    /**  */
    private int targetId;
    /** 评论的内容 */
    private String content;
    /** 评论的状态 */
    private int status;
    /** 创建时间 */
    private Date createTime;
}
