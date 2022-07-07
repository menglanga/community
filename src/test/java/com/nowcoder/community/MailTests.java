package com.nowcoder.community;


import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes =CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient client;

    @Autowired
    private TemplateEngine engine;

    @Test
    public  void testTextMail(){
        client.sendHtmlMail("615775392@qq.com","testhehiehi","你好，我的小号！！！");
    }

    @Test
    public  void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","雷浩");
        String content = engine.process("/mail/demo", context);
        System.out.println(content);
        client.sendHtmlMail("615775392@qq.com","testhtml",content);

    }
}
