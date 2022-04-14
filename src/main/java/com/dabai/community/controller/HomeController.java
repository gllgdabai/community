package com.dabai.community.controller;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.DiscussPost;
import com.dabai.community.entity.Page;
import com.dabai.community.entity.User;
import com.dabai.community.service.DiscussPostService;
import com.dabai.community.service.LikeService;
import com.dabai.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @create 2022-03-28 16:33
 */
@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        //  方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入到Model中
        // 所有，在thymeleaf中可以直接访问Page对象中的数据。
        page.setRows(discussPostService.findDiscussPostRows(0));    // 获取总行数
        page.setPath("/index");
        List<DiscussPost> posts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (posts != null) {
            for (DiscussPost post : posts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                // 点赞的数量
                long likeCount = likeService.findEntityLikeCount(Constants.ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        // Model的底层为一个HashMap。
        // Model 中的数据存储在 request 作用域中，SringMVC默认采用转发的方式跳转到视图，本次请求结束，模型中的数据被销毁。
        model.addAttribute("discussPosts",discussPosts);    //使用Model向request域对象共享数据
        return "/index";
    }


    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    // 拒绝访问时的提示页面
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

}
