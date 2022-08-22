package com.nowcoder.community.controller;


import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 分页查询。显示首页帖子信息
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        //方法调用前，springMVC自动实例化,MODEL 和page 并将page注入model
        //所有，在thymeleaf中可以直接访问page对象中的数据
        //设置分页属性

        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        //查询所有的帖子，每页10条

        List<DiscussPost> list = discussPostService.
                findDiscussPosts(0, page.getOffset(), page.getLimit());

        //map的数组列表，每个map又存储这帖子信息，作者信息，帖子的点赞数（redis中存着，value为点赞的userId）

        //帖子VO ，返回视图层的对象
        List<Map<String,Object>> discussPosts=new ArrayList<>();
        //查询出来的全部帖子不为空
        if (list!=null){
            for (DiscussPost post : list){
                //存帖子实体
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);

                //查询帖子的作者 存发布人
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, post.getId());

                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }


    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }


    @GetMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }
}
