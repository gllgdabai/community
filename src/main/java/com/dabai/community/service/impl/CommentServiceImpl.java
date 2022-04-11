package com.dabai.community.service.impl;

import com.dabai.community.common.Constants;
import com.dabai.community.dao.CommentMapper;
import com.dabai.community.entity.Comment;
import com.dabai.community.service.CommentService;
import com.dabai.community.service.DiscussPostService;
import com.dabai.community.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author
 * @create 2022-04-04 21:21
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    private DiscussPostService discussPostService;

    @Override
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论，加入前需要过滤敏感词
        String content = sensitiveWordFilter.filter(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(content);
        int rows = commentMapper.insertComment(comment);

        // 更新贴子的评论数量
        if (comment.getEntityType() == Constants.ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.modifyCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }

    @Override
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
