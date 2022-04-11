package com.dabai.community.dao;

import com.dabai.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author
 * @create 2022-04-04 21:05
 */
@Mapper
public interface CommentMapper {
    /**
     *  根据实体来查询评论，带分页功能
     * @param entityType 实体类型
     * @param entityId 实体Id
     * @param offset 该页的起始行号
     * @param limit 每页最多几条数据
     * @return 评论集合
     */
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     *  查询该实体对应评论的条目数
     * @param entityType 实体类型
     * @param entityId 实体Id
     * @return 条目数
     */
    int selectCountByEntity(int entityType, int entityId);

    /**
     * 新增评论
     * @param comment 评论实体
     * @return 影响的行数
     */
    int insertComment(Comment comment);

    /**
     *  根据id查询评论
     * @param id 评论id
     * @return 查询到的评论
     */
    Comment selectCommentById(int id);

}
