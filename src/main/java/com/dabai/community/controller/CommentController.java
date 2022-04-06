package com.dabai.community.controller;

import com.dabai.community.entity.Comment;
import com.dabai.community.service.CommentService;
import com.dabai.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
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
    private HostHolder hostHolder;

    @PostMapping("/add/{postId}")
    public String addComment(@PathVariable("postId") int postId, Comment comment) {
        // 从前台接受到的comment不完整，需要补充一些数据
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + postId;
    }

}
