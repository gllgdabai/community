package com.dabai.community;

import com.dabai.community.entity.DiscussPost;
import com.dabai.community.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * @author
 * @create 2022-04-16 16:37
 */
@SpringBootTest
public class CaffeineTests {

    @Autowired
    private DiscussPostService postService;
    
    @Test
    public void initDataForTest() {
        System.out.println("没问题，可以开始了");
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("互联网求职");
            post.setContent("今年的就业形势，不容乐观");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            postService.addDiscussPost(post);
        }
        System.out.println("没问题，程序结束了");
    }

    @Test
    public void testCache() {

        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 0));
    }

}
