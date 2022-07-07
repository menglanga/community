package com.nowcoder.community.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送html邮件
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 内容
     */
    public void sendHtmlMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            //参数2表示是否使用默认内容类型为HTML邮件应用内容类型"text/html"
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
            logger.info("邮件已发送！");
        } catch (MessagingException e) {
            logger.error("发送邮件失败" + e.toString());
        }

    }


}
