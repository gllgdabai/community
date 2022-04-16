package com.dabai.community;

import com.dabai.community.dao.DiscussPostMapper;
import com.dabai.community.dao.LoginTicketMapper;
import com.dabai.community.dao.MessageMapper;
import com.dabai.community.dao.UserMapper;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.entity.LoginTicket;
import com.dabai.community.entity.Message;
import com.dabai.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

/**
 * @author
 * @create 2022-03-28 14:45
 */
@SpringBootTest
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println("row:" + rows);
        System.out.println("user.id = " + user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testSelectDiscussPosts() {

        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10, 0);
        for (DiscussPost post : discussPosts) {
            System.out.println(post);
        }
    }

    @Test
    public void testSelectDiscussPostsRows() {

        int rows = discussPostMapper.selectDiscussPostRows(101);
        System.out.println("user_id为101的用户 发帖数量为："+rows);
    }


    @Test
    public void testInsertLoginTicket() {
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(101);
        ticket.setTicket("abc");
        ticket.setStatus(0);
        ticket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(ticket);
    }

    @Test
    public void testSelectByTicket() {
        LoginTicket ticket = loginTicketMapper.selectByTicket("abc");
        System.out.println(ticket);
    }

    @Test
    public void testUpdateStatus() {

        int i = loginTicketMapper.updateStatus("abc", 1);
        System.out.println(i);
    }

    @Test
    public void testSelectConversations() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 100);
        for (Message message : messages) {
            System.out.println(message);
        }
    }

    @Test
    public void testSelectConversationCount() {
        System.out.println(messageMapper.selectConversationCount(111));
    }

    @Test
    public void testSelectLetters() {
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : messages) {
            System.out.println(message);
        }
    }

    @Test
    public void testSelectLetterCount() {
        System.out.println(messageMapper.selectLetterCount("111_112"));
    }

    @Test
    public void testSelectLetterUnreadCount() {
        System.out.println(messageMapper.selectLetterUnreadCount(131, "111_131"));
    }
}
