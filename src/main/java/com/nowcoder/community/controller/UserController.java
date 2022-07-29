package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;


    @Value("${community.path.domain}")
    private String domain;


    @Value("${server.servlet.context-path}")
    private String contextPath;


    @Autowired
    private UserService userService;


    @Autowired
    private HostHolder holder;


    @Autowired
    private LikeService likeService;


    @Autowired
    private FollowService followService;


    //访问账号设置页面
    @LoginRequired
    @GetMapping("/setting")
    public String getSetting() {
        return "/site/setting";
    }


    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile image, Model model) {
        if (image == null) {
            model.addAttribute("error", "您还没选择图片！");
            return "site/setting";
        }

        String fileName = image.getOriginalFilename();
        //截取.后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (suffix == null) {
            model.addAttribute("error", "文件格式不正确");
            return "site/setting";
        }
        fileName = CommunityUtil.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储图片
            image.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传图片失败", e.getMessage());
            throw new RuntimeException("上传图片失败，服务器发生异常");
        }

        //更新登录用户的头像（web访问路径）
        //http://localhost:8080/community/user/header/xxx(uuid).png
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        User user = holder.getUser();
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";

    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        response.setContentType("image/" + suffix);
        try (OutputStream out = response.getOutputStream();
             FileInputStream fis = new FileInputStream(fileName)
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                out.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败！", e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param oldPassword
     * @param newPassword
     * @return
     */

    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = holder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }


    }

    //个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {

        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        //用户
        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注的数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);


        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);



        //是否已关注
        boolean hasFollowed=false;
        if (holder.getUser()!=null){
            hasFollowed=followService.hasFollowed(holder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);



        return "/site/profile";

    }


}
