package com.dabai.community;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dabai.community.dao.DiscussPostMapper;
import com.dabai.community.dao.es.DiscussPostRepository;
import com.dabai.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author
 * @create 2022-04-13 11:05
 */
@SpringBootTest
public class ElasticSearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testInsert() {
        //把id为241的DiscussPost的对象保存到discusspost索引（es的索引相当于数据库的表）
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(146, 0, 100,0));
    }

    //通过覆盖原内容，来修改一条数据
    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(232);
        post.setContent("发一个水帖, 哈哈哈!");
        discussPostRepository.save(post);
    }

    //修改一条数据
    //覆盖es里的原内容 与 修改es中的内容 的区别：如果把String类型的title设为null，
    // 覆盖的话，会把es里的该对象的title也设为null；
    // UpdateRequest的话，修改后该对象的title保持不变
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("discusspost", "109");
        request.timeout("1s");
        DiscussPost post = discussPostMapper.selectDiscussPostById(230);
        post.setContent("我是新人,使劲灌水.");
        post.setTitle(null);//es中的title会保存原内容不变
        request.doc(JSON.toJSONString(post), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    @Test
    public void testDelete() {
        discussPostRepository.deleteById(231);//删除一条数据
    }

    // 不带高亮的查询
    @Test
    public void noHighLightQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost"); // discusspost是索引名，就是表名

        // 按条件查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // 构建查询条件：在discusspost索引的title和content字段中都查询"互联网寒冬"
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询，searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                // 构造排序的条件
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                .timeout(new TimeValue(60, TimeUnit.SECONDS)) // 一个可选项，用于控制允许搜索的时间
                // 分页，设置查询得起始索引位置和数量:第一条开始返回10条数据
                .from(0)    // 相当于offset, 指定从哪条数据开始查询
                .size(10);  // 相当于limit，每页多少条数据
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
//        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> discussPosts = new LinkedList<>();
        SearchHits hits = searchResponse.getHits(); // 得到命中的数据-hits，这是可迭代的
        for(SearchHit hit : hits) {
            String source = hit.getSourceAsString();    // 这是一个Json字符串，需要转为对象
            DiscussPost discussPost = JSONObject.parseObject(source, DiscussPost.class);
            System.out.println(discussPost);
            discussPosts.add(discussPost);
        }
    }

    // 带高亮的查询
    @Test
    public void highlightQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")
                .field("content")
                .requireFieldMatch(false)
                .preTags("<span style='color:red'>")
                .postTags("</span>");

        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0) // 页面上的第一页等同于在es中的 0
                .size(10)// 每页10条数据
                .highlighter(highlightBuilder);//高亮
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
            System.out.println(discussPost);
        }
    }

}
