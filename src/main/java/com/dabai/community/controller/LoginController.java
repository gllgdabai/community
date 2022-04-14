package com.dabai.community.controller;

import com.dabai.community.common.Constants;
import com.dabai.community.entity.User;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @create 2022-03-29 14:57
 */
@Controller
public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("server.servlet.context-path")
    private String contextPath;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @PostMapping("/register")
    /*
        1.前端input的name属性需要和bean的变量名对应才能成功取到值，即表单参数名与实体属性名对应。
        2.由于表单传入的数据较多，通过实体接收比较方便，同样也要保证表单参数名与这个对象的属性同名，
        并且springmvc会自动把user实体封装到model对象里，页面直接可以用。
        3.confirmpassword从request里可以取到，用param即可。
     */
    public String register(Model model, User user, String confirmPassword) {
        Map<String, Object> map = userService.register(user, confirmPassword);
        if (map == null || map.isEmpty()) { //说明注册成功
            model.addAttribute("msg","注册成功，我们已经向您的邮件发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target","/index");  // 跳转页面
            return "/site/operate-result";
        } else{
            // 注册失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("confirmPasswordMsg",map.get("confirmPasswordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }
    //          http://localhost:8080/community/activation/15287c265e2ad8049bcb1f23c3b282db30f
    // 激活路径：http://localhost:8080/community/activation/101/code
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model,
                             @PathVariable("userId") int userId,
                             @PathVariable("code") String code)
    {
        int result = userService.activation(userId, code);
        if (result == Constants.ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功，您的账户可以正常使用了！");
            model.addAttribute("target","/login");
        } else if (result == Constants.ACTIVATION_REPEAT) {
            model.addAttribute("msg","无效操作，该账户已经激活过了！");
            model.addAttribute("target","/index");
        } else {
            model.addAttribute("msg","激活失败，您提供的激活码不正确！");
            model.addAttribute("target","/index");
        }

        return "/site/operate-result";
    }

    // 已弃用。获取验证码，存入session
//    @GetMapping("/kaptcha")
//    public void getKaptcha(HttpServletResponse response, HttpSession session) {
//        // 生成验证码
//        String text = kaptchaProducer.createText();
//        BufferedImage image = kaptchaProducer.createImage(text);
//
//        // 将验证码存入session
//        session.setAttribute("kaptcha",text);
//        // 将验证码图片输出给浏览器
//        response.setContentType("image/png");
//        try {
//            OutputStream os = response.getOutputStream();
//            ImageIO.write(image, "png", os);
//        } catch (IOException e) {
//            log.error("响应验证码失败：" + e.getMessage());
//        }
//    }

    // 重构：获取验证码，存入redis中
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 验证码的归属：给客户端临时的凭证，一个随机生成的字符串
        String kaptchaOwner = CommunityUtil.generateUUID();
        // 需要发送给客户端，因此需要存入Cookie中
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);   // 设置cookie有效时间为60秒
        cookie.setPath(contextPath);  // 设置cookie有效路径为整个项目
        response.addCookie(cookie);

        // 将验证码存入redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将验证码图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            log.error("响应验证码失败：" + e.getMessage());
        }
    }

    /**
     *  已弃用，处理登录请求，从session中取验证码
     * @param model
     * @param username 用户输入的账户
     * @param password 用户输入的密码
     * @param verifycode  用户输入的验证码
     * @param rememberMe 是否勾选"记住我"
     * @param session   从session中取出登录界面的验证码
     * @param response  cookie
     * @return
     */
//    @PostMapping("/login")
//    public String login(Model model, String username, String password,
//                        String verifycode, boolean rememberMe,
//                        HttpSession session, HttpServletResponse response) {
//        // 验证 验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");
//        if (StringUtils.isBlank(verifycode) || StringUtils.isBlank(kaptcha)
//                || !verifycode.equalsIgnoreCase(kaptcha)) {
//            model.addAttribute("codeMsg", "输入的验证码不正确!");
//            return "/site/login";
//        }
//
//        // 验证账户名和密码
//        int expiredSeconds = rememberMe ? Constants.REMEMBER_EXPIRED_SECONDS :
//                Constants.DEFAULT_EXPIRED_SECONDS;
//        Map<String, Object> loginMap = userService.login(username, password, expiredSeconds);
//        if (loginMap.containsKey("ticket")) {   // 登录成功
//            // 把登录凭证放入cookie中，传给客户端(浏览器)
//            Cookie cookie = new Cookie("ticket", loginMap.get("ticket").toString());
//            cookie.setPath(contextPath);   // 设置cookie的有效路径
//            cookie.setMaxAge(expiredSeconds);   // 设置cookie的有效时间
//            response.addCookie(cookie);
//            return "redirect:/index";   // 重定向到首页
//        } else {    // 登录失败
//            model.addAttribute("usernameMsg", loginMap.get("usernameMsg"));
//            model.addAttribute("passwordMsg", loginMap.get("passwordMsg"));
//            return "/site/login";
//        }
//    }

    /**
     *  重构，处理登录请求，从redis中取验证码
     * @param model
     * @param username 用户输入的账户
     * @param password 用户输入的密码
     * @param verifycode  用户输入的验证码
     * @param rememberMe 是否勾选"记住我"
     * @param response  cookie
     * @param kaptchaOwner 从cookie中取出验证码凭证
     * @return
     */
    @PostMapping("/login")
    public String login(Model model, String username, String password,
                        String verifycode, boolean rememberMe, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 验证 验证码
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(verifycode) || StringUtils.isBlank(kaptcha)
                || !verifycode.equalsIgnoreCase(kaptcha)) {
            model.addAttribute("codeMsg", "输入的验证码不正确!");
            return "/site/login";
        }

        // 验证账户名和密码
        int expiredSeconds = rememberMe ? Constants.REMEMBER_EXPIRED_SECONDS :
                Constants.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> loginMap = userService.login(username, password, expiredSeconds);
        if (loginMap.containsKey("ticket")) {   // 登录成功
            // 把登录凭证放入cookie中，传给客户端(浏览器)
            Cookie cookie = new Cookie("ticket", loginMap.get("ticket").toString());
            cookie.setPath(contextPath);   // 设置cookie的有效路径
            cookie.setMaxAge(expiredSeconds);   // 设置cookie的有效时间
            response.addCookie(cookie);
            return "redirect:/index";   // 重定向到首页
        } else {    // 登录失败
            model.addAttribute("usernameMsg", loginMap.get("usernameMsg"));
            model.addAttribute("passwordMsg", loginMap.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
