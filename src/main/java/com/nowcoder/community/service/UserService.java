package com.nowcoder.community.service;


import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import com.nowcoder.community.util.CommunityUtil;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient client;

    @Autowired
    private TemplateEngine engine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;


    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user==null){
             user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //账号不为空，账号已存在验证
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        //邮箱已被注册验证
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;

        }

        //新用户注册
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);// '0-普通用户; 1-超级管理员; 2-版主;',
        user.setStatus(0);//'0-未激活; 1-已激活;',
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/{id}/{activationCode}
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        //激活邮箱地址
        context.setVariable("url", url);
        String content = engine.process("/mail/activation", context);
        System.out.println(content);
        client.sendHtmlMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * //'0-未激活; 1-已激活;'
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            //清除redis缓存
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }


    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不允许为空！");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能允许为空！");
            return map;
        }

        //验证账号是否注册
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "账号不存在！");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("statusMsg", "该账号未激活！");
            return map;
        }

        //验证密码是否正确，这里要验证加密后的密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setTicket(CommunityUtil.generateUUID());
        ticket.setStatus(0);
        ticket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

//        loginTicketMapper.insertLoginTicket(ticket);

        String redisKey= RedisKeyUtil.getTicketKey(ticket.getTicket());
        //ticket对象存入的形式是json str
        redisTemplate.opsForValue().set(redisKey,ticket);




        map.put("ticket", ticket.getTicket());


        return map;
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket=(LoginTicket)redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        //改完再存回redis
        redisTemplate.opsForValue().set(redisKey,loginTicket);

    }

    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectLoginTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);

    }

    public int updateHeader(int userId, String headerUrl){
//        return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        //修改就得删缓存
        clearCache(userId);
        return rows;
    }

    public Map<String, Object>  updatePassword( int userId, String oldPassword,String newPassword){

        Map<String ,Object> map=new HashMap<>();

        if (StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","原密码不能为空！");
        }
        if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空");
        }

        User user = userMapper.selectById(userId);
        //验证原密码
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!user.getPassword().equals(oldPassword)){
            map.put("oldPasswordMsg","原密码输入有误");
        }

        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId,newPassword);
        return map;
    }


    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(redisKey);
    }
    //2.取不到初始化缓存数据，去数据库查，再存入redis
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;

    }

    //3.数据变更时清除缓存
    private void clearCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);

    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list=new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
