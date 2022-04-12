package com.dabai.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.dabai.community.common.Constants;
import com.dabai.community.entity.Message;
import com.dabai.community.entity.Page;
import com.dabai.community.entity.User;
import com.dabai.community.service.MessageService;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author
 * @create 2022-04-07 9:59
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 查询私信列表
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String,Object> map = new HashMap<>();
                map.put("conversation", message);
                // 查询当前会话中，消息的数量
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                // 查询当前会话中，未读消息的数量
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 查找在当前会话中与当前用户通信的 另一位用户
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        // conversations 加入到request共享域中
        model.addAttribute("conversations", conversations);

        // 查询当前用户，所有的未读私信消息的数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 补充：查询当前用户，所有的未读系统消息的数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/letter";
    }


    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        // 私信目标
        model.addAttribute("target",getLetterTarget(conversationId));
        // 设置已读
        List<Integer> ids = getLetterUnreadIds(letterList); // 未读消息id列表
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);  //将这些未读消息设置为已读
        }
        return "/site/letter-detail";
    }

    /** 得到页面中未读的消息的id */
    private List<Integer> getLetterUnreadIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                // 当前用户是消息接收者，并且这条消息属于未读状态
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private  User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @PostMapping("/letter/send")
    @ResponseBody   //采用异步的方式发送请求
    public String sendMessage(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJsonString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message); // 插入数据库


        return CommunityUtil.getJsonString(0);  // 表示操作成功
    }

    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), Constants.TOPIC_COMMENT);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            // 将通知中的content解析出来, content中存了entityType,entityId,postId,userId
            String content = HtmlUtils.htmlUnescape(message.getContent());  // 将转义字符还原回去
            // 还原为map
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            Integer userId = (Integer) data.get("userId");  // 作出评论的用户的Id
            messageVO.put("user", userService.findUserById(userId));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), Constants.TOPIC_COMMENT);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), Constants.TOPIC_COMMENT);
            messageVO.put("unread", unread);

            model.addAttribute("commentNotice", messageVO);
        }

        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), Constants.TOPIC_LIKE);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            Integer userId = (Integer) data.get("userId");
            messageVO.put("user", userService.findUserById(userId));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), Constants.TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), Constants.TOPIC_LIKE);
            messageVO.put("unread", unread);

            model.addAttribute("likeNotice", messageVO);
        }

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), Constants.TOPIC_FOLLOW);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            Integer userId = (Integer) data.get("userId");
            messageVO.put("user", userService.findUserById(userId));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), Constants.TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), Constants.TOPIC_FOLLOW);
            messageVO.put("unread", unread);

            model.addAttribute("followNotice", messageVO);
        }

        // 查询当前用户，所有的未读私信消息的数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 查询当前用户，所有的未读系统消息的数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page) {
        User user = hostHolder.getUser();

        // 分页信息
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知对象
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent()); // 将转义字符还原回去
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class); // 还原为map
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者，其实就是系统用户, 系统用户Id为1
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterUnreadIds(noticeList); // 未读消息id列表
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);  //将这些未读消息设置为已读
        }

        return "/site/notice-detail";
    }


}
