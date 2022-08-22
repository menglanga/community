package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user=holder.getUser();
        if (user==null){
            throw  new RuntimeException("你还没登录！");
        }
        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event=new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(holder.getUser().getId())
                .setEntityId(entityId)
                .setEntityType(entityType)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注");

    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user=holder.getUser();
        if (user==null){
            throw  new RuntimeException("你还没登录！");
        }
        followService.unfollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"已取消关注");

    }

    //查询某个用户关注多少个实体 多少个entityId
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw  new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));

        List<Map<String,Object>> userList=followService.
                findFollowees(userId,page.getOffset(),page.getLimit());


        if (userList!=null){
            for (Map<String,Object> map: userList){
                User u= (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }

        model.addAttribute("users",userList);
        return "/site/followee";


    }


    //查询某个实体(用户)有多少个粉丝用户
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw  new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int)followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER,userId));

        List<Map<String,Object>> userList=followService.
                findFollowers(userId,page.getOffset(),page.getLimit());


        if (userList!=null){
            for (Map<String,Object> map: userList){
                User u= (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }

        model.addAttribute("users",userList);
        return "/site/follower";


    }


    private  boolean hasFollowed(int userId){
        if (holder.getUser()==null){
            return false;
        }

        return followService.hasFollowed(holder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
    }

}
