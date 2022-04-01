package com.dabai.community.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author
 * @create 2022-03-29 14:18
 */
@Component
public class MailClient {
    private static final Logger log = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     *  发送邮件
     * @param to 收件人
     * @param subject 邮件标题
     * @param context 邮件内容
     */
    public void sendMail(String to, String subject, String context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(context,true);   // 支持html文本
            mailSender.send(helper.getMimeMessage());   //发送邮件
        } catch (MessagingException e) {
            log.info("发送邮件失败：" + e.getMessage());
        }
    }
}
