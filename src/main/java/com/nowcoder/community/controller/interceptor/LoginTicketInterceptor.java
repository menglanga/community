package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor  implements HandlerInterceptor {


    @Autowired
    private  UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 这个方法在HandlerMapping找到对应的处理对象,但还没有请求这个对象时处理
     * 也就是在Controller的方法之前执行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        //登录状态
        if (ticket!=null){
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //凭证有效
            if (loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户信息
                //System.out.println(ticket);
                User user = userService.findUserById(loginTicket.getUserId());
                //本线程存储用户信息
                hostHolder.setUser(user);
//                //构建用户认证的结果，并存入SecurityContext,以便于security进行授权
//                Authentication authentication = new UsernamePasswordAuthenticationToken(user,
//                        user.getPassword(),userService.getAuthorities(user.getId()));
//                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
                //构建用户认证结果，存入SecurityContext，以便Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        // principal: 主要信息; credentials: 证书; authorities: 权限;
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }

        return true;
    }


    /**
     * 在Handler/controller的方法处理完逻辑,渲染视图之前执行
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user!=null&&modelAndView!=null){
            //System.out.println(user.toString());
        modelAndView.addObject("loginUser",user);
        }
    }


    /**
     * 在视图渲染完之后执行,常用于资源清理等等
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //System.out.println(1);
        hostHolder.clear();
        //User user = hostHolder.getUser();
        //System.out.println(user);
        //SecurityContextHolder.clearContext();
    }
}
