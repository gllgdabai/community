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
     */
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);

    /**
     *  查询相关用户的讨论帖 数量
     * @param userId 用户Id，如果传入userId=0，则查询所有用户
     */
    int findDiscussPostRows(int userId);
}
