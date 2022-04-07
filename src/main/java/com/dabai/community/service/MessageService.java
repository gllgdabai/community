package com.dabai.community.service;

import com.dabai.community.entity.Message;

import java.util.List;

/**
 * @author
 * @create 2022-04-06 22:27
 */
public interface MessageService {

    List<Message> findConversations(int userId, int offset, int limit);

    int findConversationCount(int userId);

    List<Message> findLetters(String conversationId, int offset, int limit);

    int findLetterCount(String conversationId);

    int findLetterUnreadCount(int userId, String conversationId);

    int addMessage(Message message);

    /**
     *  将ids列表中的id对应的消息，设置为已读状态
     * @param ids   id列表
     * @return  影响的行数
     */
    int readMessage(List<Integer> ids);
}
