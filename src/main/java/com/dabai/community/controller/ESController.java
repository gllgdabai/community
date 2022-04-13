package com.dabai.community.controller;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.entity.Page;
import com.dabai.community.entity.SearchResult;
import com.dabai.community.service.ElasticSearchService;
import com.dabai.community.service.LikeService;
import com.dabai.community.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @create 2022-04-13 17:17
 */
@Controller
public class ESController {
    private static final Logger log = LoggerFactory.getLogger(ESController.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/search")
    public String search(String keyword, Model model, Page page) {
        page.setLimit(10);
        page.setPath("/search?keyword=" + keyword);
        try {
            SearchResult<DiscussPost> searchResult = elasticSearchService.searchDiscussPost(keyword, page.getOffset(), page.getLimit());
            List<Map<String, Object>> discussPostVoList = new ArrayList<>();
            List<DiscussPost> discussPostList = searchResult.getList();
            if (discussPostList != null) {
                for (DiscussPost post : discussPostList) {
                    Map<String, Object> map = new HashMap<>();
                    // 帖子及其作者
                    map.put("post", post);
                    map.put("user", userService.findUserById(post.getUserId()));
                    // 帖子的点赞数量
                    map.put("likeCount", likeService.findEntityLikeCount(Constants.ENTITY_TYPE_POST, post.getId()));

                    discussPostVoList.add(map);
                }

                model.addAttribute("discussPosts",discussPostVoList);
                model.addAttribute("keyword",keyword);
                // 补充分页信息
                page.setRows(searchResult.getTotal() == 0 ? 0 : (int)searchResult.getTotal());
            }
        } catch (IOException e) {
            log.error("系统出错，没有数据：" + e.getMessage());
        }

        return "/site/search";
    }
}
