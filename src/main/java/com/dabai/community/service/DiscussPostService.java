package com.dabai.community.service;

import com.dabai.community.entity.DiscussPost;

import java.util.List;

/**
 * @author
 * @create 2022-03-28 16:18
 */
public interface DiscussPostService {
    /**
     *  分页查询相关用户的讨论贴子
     * @param userId 用户Id，如果传入userId=0，则查询所有用户
     * @param offset 该页的起始行号
     * @param limit  该页的行数
     * @param orderMode 排序模式，为了实现热度排序，新添加的形参
     */
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode);

    /**
     * 查询相关用户的讨论帖 数量
     * @param userId 用户Id，如果传入userId=0，则查询所有用户
     */
    int findDiscussPostRows(int userId);

    /**
     * 新增帖子
     * @param post 帖子实体
     */
    int addDiscussPost(DiscussPost post);

    /**
     * 查询帖子详情信息
     * @param id 帖子id
     */
    DiscussPost findDiscussPostById(int id);

    /**
     *  修改帖子的评论数量
     * @param id 帖子id
     * @param commentCount 新的评论数量
     */
    int modifyCommentCount(int id, int commentCount);

    /**
     *  更新帖子的类型
     * @param id 帖子id
     * @param type 新的类型
     */
    int modifyType(int id, int type);

    /**
     * 更新帖子的状态
     * @param id 帖子id
     * @param status 新的类型
     */
    int modifyStatus(int id, int status);

    /**
     *  更新帖子的分数
     * @param id 帖子id
     * @param score 新的分数
     */
    int modifyScore(int id, double score);
}
