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
     *  分页选取相关用户讨论的帖子 (重构：添加了排序模式)
     * @param userId 用户Id，如果传入userId=0，则查询所有用户
     * @param offset 该页的起始行号
     * @param limit 每页显示多少条数据
     * @param orderMode 排序模式，为了实现热度排序，新添加的形参
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

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

    /**
     *  更新帖子的类型
     * @param id 帖子id
     * @param type 新的类型
     */
    int updateType(int id, int type);

    /**
     * 更新帖子的状态
     * @param id 帖子id
     * @param status 新的类型
     */
    int updateStatus(int id, int status);

    /**
     *  更新帖子的分数
     * @param id 帖子id
     * @param score 新的分数
     */
    int updateScore(int id, double score);
}
