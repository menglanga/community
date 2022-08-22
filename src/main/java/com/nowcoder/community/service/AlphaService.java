package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {
    private static final Logger logger=LoggerFactory.getLogger(AlphaService.class);

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public  String find(){
        return  alphaDao.select();
    }

    public  AlphaService(){
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public  void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public  void destroy(){
        System.out.println("销毁AlphaService");
    }


    //required 方法A调用B,B上加上required注解，如果A没有事务，则创建一个新事务，如果A有事务，加入A事务
    //requires_new 方法A调用B,B上加上required_new注解，则创建一个新事务，不然A有没有事务,作用在B事务中去
    //nested  方法A调用B,B上加上nested注解，嵌套在A事务中，但B中的事务有自己的提交和回滚，A事务不存在就和Reuired一样
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save1(){
        //新增用户
        User user=new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //新增帖子
        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("HELLO");
        post.setContent("新人报大");
        post.setCreateTime(new Date());
        discussPostMapper.InsertDiscussPost(post);

        Integer.valueOf("abc");

        return "ok";
    }


    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user=new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/911t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post=new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("nihao");
                post.setContent("ni1大");
                post.setCreateTime(new Date());
                discussPostMapper.InsertDiscussPost(post);

                Integer.valueOf("abc");
                return  "ok";
            }
        });

    }


    //该方法在多线程的环境下异步调用
    @Async
    public void execute1(){
        logger.debug("execute1");
    }

    @Scheduled(initialDelay = 10000,fixedRate = 1000)
    public void execute2(){
        logger.debug("execute2");
    }

}
