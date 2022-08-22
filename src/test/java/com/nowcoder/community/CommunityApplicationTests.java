package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * spring 容器工作原理
 */
@SpringBootTest
@ContextConfiguration(classes =CommunityApplication.class)
class CommunityApplicationTests  implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     *
     * @param applicationContext  BeanFactory接口 spring容器的顶层接口
     *                            applicationContext 是子接口
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Test
    public void testApplicationContext() {
        //spring容器 org.springframework.web.context.support.GenericWebApplicationContext@5ef6ae06, started on Sat Aug 06 18:56:54 CST 2022
        System.out.println(applicationContext);


        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());//Mybatis

        AlphaDao hibernate = applicationContext.getBean("alphaHibernate", AlphaDao.class);
        System.out.println(hibernate.select());//Hibernate
    }


    @Test
    public void testBeanManagement() {
        AlphaService bean = applicationContext.getBean(AlphaService.class);
        AlphaService bean1 = applicationContext.getBean(AlphaService.class);
        //实例化AlphaService -> 初始化AlphaService ->销毁AlphaService
        System.out.println(bean);//com.nowcoder.community.service.AlphaService@3f3c5ecd
        System.out.println(bean1);//com.nowcoder.community.service.AlphaService@3f3c5ecd
    }


    @Test
    public  void testBeanConfig(){
        SimpleDateFormat simpleDateFormat= applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format((new Date())));
    }

    //依赖注入属性
    @Autowired
    @Qualifier("alphaHibernate")
    private  AlphaDao alphaDao;

    @Autowired
    private  AlphaService alphaService;

    @Autowired
    private  SimpleDateFormat simpleDateFormat;

    @Test
    public  void testDI(){
        System.out.println(alphaDao);//com.nowcoder.community.dao.AlphaDaoHibernateImpl@3f3c5ecd
        System.out.println(alphaService);//com.nowcoder.community.service.AlphaService@1179fc8c
        System.out.println(simpleDateFormat.format(new Date()));//2022-08-06 19:27:58
    }
}
