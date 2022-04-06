package com.dabai.community.dao;

import com.dabai.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author
 * @create 2022-03-28 15:14
 */
@Mapper
public interface DiscussPostMapper {
    /**
     *  分页选取相关用户讨论的帖子
     * @param userId 用户Id，如果传入userId=0，则查询所有用户
     * @param offset 该页的起始行号
     * @param limit 每页显示多少条数据
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     *  查询相关用户的帖子条数
     *  注意：@param注解用于给参数取别名
     *       如果只有一个参数，并且在<if>拼接使用，则必须使用别名
     * @param userId 用户Id，如果传入userId=0，则查询所有用户
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     *  新增帖子
     * @param discussPost   帖子实体
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     *  根据id查询帖子
     * @param id discuss_post表的主键id
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     *  更新帖子的评论数量
     * @param id    帖子id
     * @param commentCount 新的评论数量
     */
    int updateCommentCount(int id, int commentCount);
}
