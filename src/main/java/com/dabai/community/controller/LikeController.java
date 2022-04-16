package com.dabai.community.controller;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.User;
import com.dabai.community.event.Event;
import com.dabai.community.event.EventProducer;
import com.dabai.community.service.LikeService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.HostHolder;
import com.dabai.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @create 2022-04-08 19:18
 */
@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/like")
    @ResponseBody
    // 重构：实现触发点赞事件，并且形参多传入了一个 postId
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 将数量和状态封装成一个map，作为返回的结果
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 创建点赞事件对象
        if (likeStatus == 1) {  // 确定是点赞，取消点赞就不通知了
            // 创建点赞事件对象
            Event event = new Event()
                    .setTopic(Constants.TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            // 触发点赞事件
            eventProducer.fireEvent(event);
        }

        if (entityType == Constants.ENTITY_TYPE_POST) {
            // 点赞帖子会影响分数，使用redis缓存需要重新计算分数的帖子
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJsonString(0, "操作成功", map);
    }
}
