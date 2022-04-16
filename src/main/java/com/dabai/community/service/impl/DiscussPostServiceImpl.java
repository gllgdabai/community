package com.dabai.community.service.impl;

import com.dabai.community.dao.DiscussPostMapper;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.service.DiscussPostService;
import com.dabai.community.utils.SensitiveWordFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @create 2022-03-28 16:25
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    private static final Logger log = LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口：Cache, LoadingCache, AsyncLoadingCache

    // (热门)帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数的缓存
    private LoadingCache<Integer, Integer> postRowsCahche;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数不能为空!");
                        }

                        String[] params = key.split(":");   // 切出来一定是 offset和limit
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存：这里可以加个Redis缓存，Redis没有再访问DB，功能以后再完成

                        log.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });

        // 初始化帖子总数缓存
        postRowsCahche = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {

                        // 二级缓存：这里可以加个Redis缓存，Redis没有再访问DB，功能以后再完成

                        log.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });

    }

    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        if (userId == 0 && orderMode == 1) {    // 只缓存首页的最热帖子
            return postListCache.get(offset + ":" + limit);
        }

        log.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    @Override
    public int findDiscussPostRows(int userId) {
        if (userId == 0) {  // userId为0，说明是所有用户
            return postRowsCahche.get(userId);
        }

        log.debug("load post rows from DB.");
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
