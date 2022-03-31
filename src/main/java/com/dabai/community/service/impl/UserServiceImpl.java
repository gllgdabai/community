package com.dabai.community.service.impl;

import com.dabai.community.dao.LoginTicketMapper;
import com.dabai.community.dao.UserMapper;
import com.dabai.community.entity.LoginTicket;
import com.dabai.community.entity.User;
import com.dabai.community.enums.ActivationEnum;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author
 * @create 2022-03-28 16:20
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;  //域名

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        // 首先进行数据合法性校验
        // 1.空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }
        // 2.验证用户名/邮箱 尚未被注册
        if (userMapper.selectByName(user.getUsername()) != null) {
            map.put("usernameMsg","用户名已存在！");
            return map;
        }
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg","邮箱已被注册！");
            return map;
        }

        // 验证完数据合法性后，注册用户，对传入的user中的数据进行补充
        // 为了保证用户密码不容易被破解，进行MD5加密
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));

        user.setType(0);    // 普通用户
        user.setStatus(0);  // 未激活
        user.setActivationCode(CommunityUtil.generateUUID());   // 激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        // 给注册者发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 设置激活路径：http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() +"/"+ user.getActivationCode();
        context.setVariable("url", url);
        String html = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账户", html);
        return map;
    }

    /**
     * 激活
     * @param userId 用户id
     * @param code  激活码
     * @return  返回激活的类型
     */
    @Override
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {    //已激活
            return ActivationEnum.ACTIVATION_REPEAT.getStatus();
        } else if (code.equals(user.getActivationCode())) {
            userMapper.updateStatus(userId, 1);//更改状态，变为已激活
            return ActivationEnum.ACTIVATION_SUCCESS.getStatus();
        } else {
            return ActivationEnum.ACTIVATION_FAILURE.getStatus();
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 首先进行数据合法性校验
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账户不存在！");
            return map;
        }
        // 验证密码，需要先对用户输入的密码进行MD5加密，然后再与数据库中存的密码进行比对
        password = CommunityUtil.md5(password + user.getSalt()); // MD5加密
        if (!password.equals(user.getPassword())) {  // 进行比对
            map.put("passwordMsg", "密码不正确！");
            return map;
        }
        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        String ticket = CommunityUtil.generateUUID();   // 生成一串随机字符串作为ticket
        loginTicket.setTicket(ticket);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",ticket);
        return map;
    }

    @Override
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    @Override
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

}
