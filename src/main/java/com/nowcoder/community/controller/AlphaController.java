package com.nowcoder.community.controller;


import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {


    @Autowired
    private AlphaService alphaService;


    @RequestMapping("/data")
    @ResponseBody
    public  String  getData(){
        return  alphaService.find();
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        Map<String,String> map=new HashMap<>();
        map.put("11","sf");
        return map.toString();
        //return "Hello Springboot!!!";
    }


    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            System.out.println(name+":"+value);

        }

        System.out.println(request.getParameter("code"));

        //返回响应数据

        response.setContentType("text/html;charset=utf-8");
        try(PrintWriter writer = response.getWriter();) {
            writer.write("<h1>Niuko<h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //GET请求
    // /students?current=1?limit=20?

    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@RequestParam(name = "current",required = false,defaultValue = "1") int current,
                              @RequestParam(name="limit",required = false,defaultValue = "10") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    //  /student/12

    @RequestMapping(value = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public  String  getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }

    //post
    @RequestMapping(value = "/student",method = RequestMethod.POST)
    @ResponseBody
    public  String  saveStudent(String name ,int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    //响应HTML数据
    @GetMapping("/teacher")
    public ModelAndView getTeacher(){
        ModelAndView mav= new ModelAndView();
        mav.addObject("name","雷浩");
        mav.addObject("age",26);
        mav.setViewName("/demo/view");
        return mav;
    }

    @GetMapping("/school")
    public String getSchool(Model model){
        model.addAttribute("name","北京大学");
        model.addAttribute("age",480);
        return "/demo/view";
    }



    //响应json数据（异步请求）
    //java对象 -> json字符串 ->   js对象

    @GetMapping("/emp")
    @ResponseBody
    public Map<String,Object> getEmp(){
        Map<String, Object> map = new HashMap<>();
        map.put("name","hh");
        map.put("age",89);
        map.put("salary",8000.00);
        return map;
    }

    @GetMapping("/emps")
    @ResponseBody
    public List<Map<String,Object>> getEmps(){
        List<Map<String,Object>> list=new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name","hh");
        map.put("age",89);
        map.put("salary",8000.00);
        list.add(map);

        map=new HashMap<>();
        map.put("name","累");
        map.put("age",65);
        map.put("salary",9000.00);
        list.add(map);

        map=new HashMap<>();
        map.put("name","袁");
        map.put("age",34);
        map.put("salary",4000.00);
        list.add(map);

        return list;
    }

    //cookie示例
    @GetMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效的范围
        cookie.setPath("/community/alpha");
        //设置cookie存放时间
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);
        return "cookie 已经生成了,在我的响应头里哦！";
    }

    @GetMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return "get cookie";
    }

    //session示例
    @GetMapping("/session/set")
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","test");
        return "set session";
    }

    @GetMapping("/session/get")
    @ResponseBody
    public String getSession(HttpSession session){
        Object id=session.getAttribute("id");
        Object name=session.getAttribute("name");
        System.out.println(id);
        System.out.println(name);
        return "get session";
    }


    //ajax示例
    @PostMapping("/ajax")
    @ResponseBody
    public String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功");
    }
}
