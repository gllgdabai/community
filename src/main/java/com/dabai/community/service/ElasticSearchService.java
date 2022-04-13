package com.dabai.community.service;

import com.dabai.community.entity.DiscussPost;
import com.dabai.community.entity.SearchResult;

import java.io.IOException;

/**
 * @author
 * @create 2022-04-13 16:36
 */
public interface ElasticSearchService {
    /**
     * 将帖子discussPost 保存至ElasticSearch服务器
     */
    void saveDiscussPost(DiscussPost discussPost);

    /**
     * 从ElasticSearch服务器中按Id删除帖子
     */
    void deleteDiscussPost(int id);

    /**
     *  (分页)查询，从ElasticSearch服务器搜索帖子
     * @param keyword  查询关键字
     * @param offset   当前页的起始行
     * @param limit    每页最多几条数据
     * @return  自定义实体SearchResult 来接收 查询的结果
     */
    SearchResult<DiscussPost> searchDiscussPost(String keyword, int offset, int limit) throws IOException;

}
