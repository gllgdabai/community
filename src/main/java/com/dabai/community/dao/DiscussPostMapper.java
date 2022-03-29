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
}
