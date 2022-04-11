package com.dabai.community.service;

import com.dabai.community.entity.Comment;

import java.util.List;

/**
 * @author
 * @create 2022-04-04 21:19
 */
public interface CommentService {
    /**
     *  根据实体来查询评论，带分页功能
     * @param entityType 实体类型
     * @param entityId 实体Id
     * @param offset 该页的起始行号
     * @param limit 每页最多几条数据
     * @return 评论集合
     */
    List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     *  查询该实体对应评论的条目数
     * @param entityType 实体类型
     * @param entityId 实体Id
     * @return 条目数
     */
    int findCommentCount(int entityType, int entityId);

    /**
     *  新增评论
     * @param comment 评论实体
     * @return 影响的行数
     */
    int addComment(Comment comment);

    /**
     *  根据id查询评论
     * @param id 评论id
     * @return 查询到的评论
     */
    Comment findCommentById(int id);
}
