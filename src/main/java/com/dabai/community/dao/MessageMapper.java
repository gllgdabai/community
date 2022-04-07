package com.dabai.community.dao;

import com.dabai.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author
 * @create 2022-04-06 20:44
 */
@Mapper
public interface MessageMapper {
    /**
     *  (分页)查询当前用户的会话列表，针对每个会话只返回一个最新(即Id最大)的私信
     * @param userId 当前用户的Id
     * @param offset 该页数据起始行号
     * @param limit  每页最多几条数据
     * @return 会话列表
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     *  查询当前用户的会话数量
     * @param userId 当前用户的Id
     * @return 会话数量
     */
    int selectConversationCount(int userId);

    /**
     *  (分页)查询某个会话所包含的私信列表
     * @param conversationId 该会话的Id
     * @param offset 该页数据起始行号
     * @param limit 每页最多几条数据
     * @return 私信列表
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     *  查询某个会话所包含的私信数量
     * @param conversationId 该会话的Id
     * @return 私信数量
     */
    int selectLetterCount(String conversationId);

    /**
     *  查询当前用户的某一会话中未读私信的数量，动态拼接conversationId，默认就是所有会话
     * @param userId 当前用户的Id,作为to_id
     * @param conversationId 该会话的Id，动态拼接
     * @return 未读私信的数量
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     *  新增消息
     * @param message   消息实体
     * @return 插入影响的行数，成功则为1
     */
    int insertMessage(Message message);

    /**
     *  修改消息的状态
     * @param ids   要修改状态的消息的id列表
     * @param status  新的状态
     * @return  修改影响的行数
     */
    int updateStatus(List<Integer> ids, int status);
}
