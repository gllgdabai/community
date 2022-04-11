package com.dabai.community.controller;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.Page;
import com.dabai.community.entity.User;
import com.dabai.community.event.Event;
import com.dabai.community.event.EventProducer;
import com.dabai.community.service.FollowService;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @create 2022-04-09 10:02
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(Constants.TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0, "已关注");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJsonString(0, "已取消关注");
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, Constants.ENTITY_TYPE_USER));
        // 查询该用户的关注列表,需要补充信息(当前用户是否也有关注该用户关注的用户)
        List<Map<String,Object>> followeeList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if (followeeList != null) {
            for (Map<String,Object> followeeMap : followeeList) {
                User followee = (User) followeeMap.get("user");
                // 查看当前用户是否也关注了该followee
                boolean hasFollowed = false;
                if (hostHolder.getUser() != null) {
                    hasFollowed = followService.hasFollowed(
                            hostHolder.getUser().getId(), Constants.ENTITY_TYPE_USER, followee.getId());

                }
                followeeMap.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("followeeList", followeeList);

        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(Constants.ENTITY_TYPE_USER, userId));
        // 查询该用户的关注列表,需要补充信息(当前用户是否也有关注该用户关注的用户)
        List<Map<String,Object>> followerList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if (followerList != null) {
            for (Map<String,Object> followerMap : followerList) {
                User follower = (User) followerMap.get("user");
                // 查看当前用户是否也关注了该followee
                boolean hasFollowed = false;
                if (hostHolder.getUser() != null) {
                    hasFollowed = followService.hasFollowed(
                            hostHolder.getUser().getId(), Constants.ENTITY_TYPE_USER, follower.getId());

                }
                followerMap.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("followerList", followerList);

        return "/site/follower";
    }




}
