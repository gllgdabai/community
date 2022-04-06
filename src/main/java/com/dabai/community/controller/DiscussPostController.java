package com.dabai.community.controller;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.Comment;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.entity.Page;
import com.dabai.community.entity.User;
import com.dabai.community.service.CommentService;
import com.dabai.community.service.DiscussPostService;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
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

}
