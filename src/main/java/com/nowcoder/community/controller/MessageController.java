package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder holder;

    @Autowired
    private UserService userService;


    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {

        User user = holder.getUser();

        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //查询会话列表

        List<Message> conversationList =
                messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());


        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                //每条会话的信息
                map.put("conversation", message);
                //未读私信的数量
                map.put("unreadCount", messageService.
                        findLetterUnreadCount(user.getId(), message.getConversationId()));
                //每条会话的私信消息的数量
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));

                //定义每条会话的用户头像，这取决于最新的一条消息是谁发的？
                // 如果是自己发的就是fromID对应的user,是别人发的就是to_id对应的user
                //
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                //存会话对方的用户信息，在模板里面输出头像
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);


        //查询未读消息数量（整个用户所有的未读消息，不带具体的会话id）
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        return "/site/letter";
    }


    //查看会话详情
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId")
                                          String conversationId, Page page, Model model) {

        //设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId,
                page.getOffset(), page.getLimit());

        List<Map<String, Object>> letters = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }

        model.addAttribute("letters", letters);

        //私信的目标,也就是模板上 的 “来自 落基山脉下的闲人 的私信”
        model.addAttribute("target", getLetterTarget(conversationId));

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";

    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (holder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (holder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(holder.getUser().getId());
        message.setToId(target.getId());

        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }

        message.setContent(content);
        message.setCreateTime(new Date());

        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

}
