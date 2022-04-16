package com.dabai.community.controller;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.Comment;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.entity.Page;
import com.dabai.community.entity.User;
import com.dabai.community.event.Event;
import com.dabai.community.event.EventProducer;
import com.dabai.community.service.CommentService;
import com.dabai.community.service.DiscussPostService;
import com.dabai.community.service.LikeService;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.HostHolder;
import com.dabai.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author
 * @create 2022-04-04
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJsonString(403, "你还没有登陆哦!");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(Constants.TOPIC_POST)
                .setUserId(user.getId())
                .setEntityType(Constants.ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 帖子刚发布需要计算初始分数，先使用redis缓存帖子，定时处理
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        return CommunityUtil.getJsonString(0,"发布成功!");
    }

    @GetMapping("/detail/{postId}")
    public String getDiscussPost(@PathVariable("postId") int postId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        model.addAttribute("post",post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 点赞相关信息
        long likeCount = likeService.findEntityLikeCount(Constants.ENTITY_TYPE_POST, postId);
        model.addAttribute("likeCount", likeCount);
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), Constants.ENTITY_TYPE_POST, postId);
        model.addAttribute("likeStatus",likeStatus);

        // 评论（分页）
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        page.setRows(post.getCommentCount());

        /* 评论分为两类：
                评论：给帖子的评论
                回复：给评论的评论
         */
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                Constants.ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit()
        );
        // 评论VO列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {   //遍历每条评论，加入该评论的相关信息(map)
                // 评论Vo
                Map<String,Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 发布该评论的用户
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 点赞相关信息
                likeCount = likeService.findEntityLikeCount(Constants.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), Constants.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);

                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        Constants.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE
                );
                // 回复VO列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyVoList != null) {
                    for (Comment reply : replyList) {   //遍历每条回复，加入该回复的相关信息(map)
                        // 回复Vo
                        Map<String,Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复的目标 （targetId）
                        User target = reply.getTargetId()==0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞相关信息
                        likeCount = likeService.findEntityLikeCount(Constants.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), Constants.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys", replyVoList);

                // 回复数
                int replyCount = commentService.findCommentCount(Constants.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }

            model.addAttribute("comments",commentVoList);

        }

       return "/site/discuss-detail";
    }

    // 置顶、取消置顶，异步请求
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(id);
        // 获取置顶状态，1为置顶，0为正常状态，1^1=0 0^1=1
        int type = discussPost.getType()^1;
        discussPostService.modifyType(id, type);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);

        // 由于帖子被修改了，触发了发帖事件
        Event event = new Event()
                .setTopic(Constants.TOPIC_POST)
                .setUserId(hostHolder.getUser().getId())    // 版主
                .setEntityType(Constants.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0, "操作成功!", map);
    }

    // 加精、取消加精，异步请求
    @PostMapping("/refine")
    @ResponseBody
    public String setFine(int id) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(id);
        // 获取加精状态，1为精华，0为正常，1^1=0 0^1=1
        int status = discussPost.getStatus()^1;
        discussPostService.modifyStatus(id, status);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);

        // 由于帖子被修改了，触发了发帖事件
        Event event = new Event()
                .setTopic(Constants.TOPIC_POST)
                .setUserId(hostHolder.getUser().getId())    // 版主
                .setEntityType(Constants.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 帖子加精会影响分数，使用redis缓存需要重新计算分数的帖子
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJsonString(0, "操作成功!", map);
    }

    // 拉黑(删除)，异步请求
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.modifyStatus(id, 2);
        // 触发了删帖事件
        Event event = new Event()
                .setTopic(Constants.TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())    // 版主
                .setEntityType(Constants.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0, "操作成功!");
    }

}
