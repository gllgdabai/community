package com.dabai.community.service.impl;

import com.dabai.community.dao.DiscussPostMapper;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.service.DiscussPostService;
import com.dabai.community.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author
 * @create 2022-03-28 16:25
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    @Override
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Override
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        String title = post.getTitle();
        String content = post.getContent();
        // 转移html标签：避免用户故意在内容里加入html标签，影响页面
        title = HtmlUtils.htmlEscape(title);
        content = HtmlUtils.htmlEscape(content);
        // 过滤敏感词
        title = sensitiveWordFilter.filter(title);
        content = sensitiveWordFilter.filter(content);
        // 把处理好的内容 再设置回去
        post.setTitle(title);
        post.setContent(content);

        return discussPostMapper.insertDiscussPost(post);
    }

    @Override
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int modifyCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    @Override
    public int modifyType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    @Override
    public int modifyStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    @Override
    public int modifyScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }

}
