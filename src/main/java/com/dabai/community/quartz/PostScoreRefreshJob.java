package com.dabai.community.quartz;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.service.CommentService;
import com.dabai.community.service.DiscussPostService;
import com.dabai.community.service.ElasticSearchService;
import com.dabai.community.service.LikeService;
import com.dabai.community.utils.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author
 * @create 2022-04-15 20:48
 */
public class PostScoreRefreshJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    private static final Date epoch;    // 牛客纪元，社区创建的时间

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0) {
            log.info("[任务取消] 没有需要刷新分数的帖子!");
            return;
        }

        log.info("[任务开始] 正在刷新分数的帖子的数量为：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer)operations.pop());
        }
        log.info("[任务结束] 帖子分数刷新完毕!");
    }
    /** 刷新帖子分数 */
    private void refresh(int postId) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(postId);

        if (discussPost == null) {
            log.error("该帖子不存在： id = " + postId);
            return;
        }

        // 是否精华
        boolean isEssence = discussPost.getStatus() == 1;
        // 评论数量
        int commentCount = discussPost.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(Constants.ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (isEssence ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数=帖子权重+距离天数
        // w可能小于1，因为log存在，所以送入log的最小值应该为0
        // getTime()单位为ms
        double score = Math.log10(Math.max(1, w))
                + (discussPost.getCreateTime().getTime() - epoch.getTime()) / (3600 * 60 * 24);
        // 更新帖子分数
        discussPostService.modifyScore(postId, score);
        // 更新elasticsearch
        discussPost.setScore(score);
        elasticSearchService.saveDiscussPost(discussPost);

    }
}
