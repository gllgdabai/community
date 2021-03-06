package com.dabai.community.service.impl;

import com.dabai.community.common.Constants;
import com.dabai.community.dao.UserMapper;
import com.dabai.community.entity.LoginTicket;
import com.dabai.community.entity.User;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.HostHolder;
import com.dabai.community.utils.MailClient;
import com.dabai.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;  //域名

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User findUserById(int id) {
//        return userMapper.selectById(id); // 重构：使用redis缓存用户信息
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    @Override
    public Map<String, Object> register(User user, String confirmPassword) {
        Map<String, Object> map = new HashMap<>();
        // 首先进行数据合法性校验
        // 1.空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","用户名不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        // 补充：密码长度限制
        if (user.getPassword().length() < 8) {
            map.put("passwordMsg","密码长度不能小于8位!");
            return map;
        }
        // 2.验证用户名/邮箱 尚未被注册
        if (userMapper.selectByName(user.getUsername()) != null) {
            map.put("usernameMsg","用户名已存在!");
            return map;
        }
        if(!user.getPassword().equals(confirmPassword)){
            map.put("confirmPasswordMsg","两次输入的密码不一致!");
            return map;
        }
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg","该邮箱已注册!");
            return map;
        }

        // 验证完数据合法性后，注册用户，对传入的user中的数据进行补充
        // 为了保证用户密码不容易被破解，进行MD5加密
        user.setSalt(CommunityUtil.generateUUID().substring(0,5)); //5位的盐值
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));

        user.setType(0);    // 普通用户
        user.setStatus(0);  // 未激活
        user.setActivationCode(CommunityUtil.generateUUID());   // 激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        // 给注册者发送激活邮件
        // 使用thymeleaf创建的对象携带变量
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 设置激活路径：http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() +"/"+ user.getActivationCode();
        context.setVariable("url", url);
        //使用模板引擎，利用thymeleaf，将context放到/mail/activation.html文件中，然后再利用mail包发送给邮箱
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
            return Constants.ACTIVATION_REPEAT;
        } else if (code.equals(user.getActivationCode())) {
            userMapper.updateStatus(userId, 1);//更改状态，变为已激活
            // 由于用户信息被修改了，需要清除用户缓存
            clearCache(userId);
            return Constants.ACTIVATION_SUCCESS;
        } else {
            return Constants.ACTIVATION_FAILURE;
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
        // ms为单位，需要乘以1000L。需要转换为long型，否则会发生数据丢失
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));

//        loginTicketMapper.insertLoginTicket(loginTicket); // 已弃用

        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 将loginTicket对象 序列化 为一个字符串存入redis中
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket",ticket);
        return map;
    }

    @Override
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);    // 已弃用
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    @Override
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    @Override
    public int updateHeader(int userId, String headerUrl) {
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        if (rows > 0) clearCache(userId);
        return rows;
    }

    @Override
    public Map<String, Object> updatePassword(String oldPassword, String newPassword, String confirmPassword) {
        Map<String,Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }
        if (newPassword.length() < 8) {
            map.put("newPasswordMsg", "密码长度不能小于8位!");
            return map;
        }
        if(!newPassword.equals(confirmPassword)){
            map.put("confirmPasswordMsg", "两次输入的密码不一致!");
            return map;
        }

        // 获取当前用户
        User holderUser = hostHolder.getUser();
        // 用户输入的原密码MD5加密后，需要与数据库中保存的密码进行比对
        oldPassword = CommunityUtil.md5(oldPassword + holderUser.getSalt());
        if (!oldPassword.equals(holderUser.getPassword())) {
            map.put("oldPasswordMsg", "原密码不正确，请重新输入!");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + holderUser.getSalt());
        userMapper.updatePassword(holderUser.getId(), newPassword);

        return map;
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }



    // 由于findUserById会被频繁的调用，故使用redis缓存用户信息，提高效率。
    // 1. 优先从redis缓存中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }
    // 2. 当缓存中取不到时，初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }
    // 3. 当用户数据变更时，清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }


    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return Constants.AUTHORITY_ADMIN;
                    case 2:
                        return Constants.AUTHORITY_MODERATOR;
                    default:
                        return Constants.AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
