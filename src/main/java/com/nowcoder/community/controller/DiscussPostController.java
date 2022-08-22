package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;


    @Autowired
    private HostHolder holder;


    @Autowired
    private UserService userService;


    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 发布帖子 利用ajax
     * @param title
     * @param content
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = holder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没登录！");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        //触发发帖事件
        Event event=new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());

        eventProducer.fireEvent(event);


        //报错的情况，将来统一处理
        return CommunityUtil.getJSONString(0, "操作成功");

    }

    /**
     * 查看帖子详情
     * @param discussPostId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/details/{discussPostId}")
    public String getDiscussPos(@PathVariable("discussPostId") int discussPostId, Model model
            , Page page) {

        //帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        //作者
        User user = userService.findUserById(post.getUserId());

        model.addAttribute("user", user);

        //点赞
        long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPostId);
        //model.addAttribute("likeCount", likeCount);

        int likeStatus = holder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("likeStatus", likeStatus);


        //评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/details/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评轮的评论

        //数据库查询评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(),
                page.getOffset(), page.getLimit());


        //评论VO列表，用于返回模板的数据
        List<Map<String, Object>> commentVoList = new ArrayList<>();


        //评论
        if (commentList != null) {
            for (Comment comment : commentList) {
                //每条评论VO
                Map<String, Object> commentVo = new HashMap<>();
                //存每一条帖子的评论
                commentVo.put("comment", comment);
                //帖子的评论的作者用户
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //点赞
                likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                likeStatus = holder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);


                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(),
                        0, Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        //回复人
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        //回复的目标,也就是被回复的对象，被回复人
                        User target = reply.getTargetId() == 0 ? null :
                                userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        //点赞
                        likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        likeStatus = holder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(holder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);


                        replyVoList.add(replyVo);
                    }
                }
                //帖子的评论VO，一条评论有多条回复
                commentVo.put("replys", replyVoList);
                //帖子下每条评论的回复（评论的评论）数
                int replyCount = commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);


                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";


    }
    //置顶帖子
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);

        //触发发帖事件
        Event event=new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);

        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }


    //加精帖子
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);

        //触发发帖事件
        Event event=new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);

        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    //拉黑帖子
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);

        //触发删帖事件
        Event event=new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);

        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }


}
