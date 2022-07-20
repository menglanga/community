package com.nowcoder.community;


import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes =CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;


    @Autowired
    private DiscussPostMapper discussPostMapper;


    @Autowired
    private LoginTicketMapper loginTicketMapper;


    @Test
    public  void testSelectUser(){
        User user=userMapper.selectById(101);
        System.out.println(user);

        user=userMapper.selectByName("liubei");
        System.out.println(user);

        user=userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }


    @Test
    public  void testInsertUser(){
        User user=new User();
        user.setUsername("test111");
        user.setPassword("123456");
        user.setSalt("yuu");
        user.setEmail("test@163.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows= userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }


    @Test
    public void updateUser(){
        int rows = userMapper.updateStatus(150,1);
        System.out.println(rows);

        rows= userMapper.updateHeader(150,"http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows= userMapper.updatePassword(150,"xixi");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost discussPost :list){
            System.out.println(discussPost);
        }

        int rows=discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public  void testInsertLoginTicket(){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abvv");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+ 1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);

    }



    @Test
    public  void testSelectLoginTicket(){
        LoginTicket ticket = loginTicketMapper.selectLoginTicket("abvv");
        System.out.println(ticket);

        loginTicketMapper.updateStatus("abvv",1);
        ticket=loginTicketMapper.selectLoginTicket("abvv");
        System.out.println(ticket);
    }

}
