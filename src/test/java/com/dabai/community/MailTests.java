package com.dabai.community;

import com.dabai.community.utils.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author
 * @create 2022-03-29 14:29
 */
@SpringBootTest
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("952710554@qq.com","TEST","测试springboot发送邮件功能");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username","廖大白");

        String html = templateEngine.process("/mail/demo", context);
        System.out.println(html);

        mailClient.sendMail("952710554@qq.com","HTML",html);
    }
}
