package com.dabai.community.controller;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.Comment;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.event.Event;
import com.dabai.community.event.EventProducer;
import com.dabai.community.service.CommentService;
import com.dabai.community.service.DiscussPostService;
import com.dabai.community.utils.HostHolder;
import com.dabai.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author
 * @create 2022-04-06 10:51
 */
@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/add/{postId}")
    public String addComment(@PathVariable("postId") int postId, Comment comment) {
        // 从前台接受到的comment不完整，需要补充一些数据
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 创建评论事件对象
        Event event = new Event()
                .setTopic(Constants.TOPIC_COMMENT)
                .setUserId(comment.getUserId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", postId);
        if (comment.getEntityType() == Constants.ENTITY_TYPE_POST) {
            // 评论的实体是帖子
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == Constants.ENTITY_TYPE_COMMENT) {
            // 评论的实体是评论
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        // 触发评论事件
        eventProducer.fireEvent(event);

        // 如果评论了帖子，该帖子的属性-评论数量会被修改，相当于修改了帖子，因此也会触发发帖事件
        if (comment.getEntityType() == Constants.ENTITY_TYPE_POST) {
            // 触发发帖事件
            event = new Event()
                    .setTopic(Constants.TOPIC_POST)
                    .setUserId(comment.getUserId())
                    .setEntityType(Constants.ENTITY_TYPE_POST)
                    .setEntityId(postId);
            eventProducer.fireEvent(event);

            // 评论帖子会影响分数，使用redis缓存需要重新计算分数的帖子
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }


        return "redirect:/discuss/detail/" + postId;
    }

}
