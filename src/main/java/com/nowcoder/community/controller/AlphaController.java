package com.nowcoder.community.controller;


import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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
        return  alphaService.find();//Mybatis
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        Map<String,String> map=new HashMap<>();
        map.put("11","sf");
        //返回json字符串 {"11":"sf"}
        return  JSONObject.toJSONString(map);
        //返回map 格式字符串
       // return map.toString();
        //return "Hello Springboot!!!";
    }


    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println(request.getMethod());// GET
        System.out.println(request.getServletPath());//  /alpha/http
        Enumeration<String> headerNames = request.getHeaderNames();
        //host:localhost:8080
        //connection:keep-alive
        //sec-ch-ua:"Chromium";v="104", " Not A;Brand";v="99", "Google Chrome";v="104"
        //sec-ch-ua-mobile:?0
        //sec-ch-ua-platform:"Windows"
        //upgrade-insecure-requests:1
        //user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36
        //accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
        //sec-fetch-site:none
        //sec-fetch-mode:navigate
        //sec-fetch-user:?1
        //sec-fetch-dest:document
        //accept-encoding:gzip, deflate, br
        //accept-language:zh-CN,zh;q=0.9
        //cookie:ticket=a5ad5a5fd6734e9e8613167690120f87; Idea-16d821b6=9ed5c7a2-0fd6-417f-8e92-225c9bec8575; Pycharm-7ce1921b=26e096e1-7b35-44ab-882b-0f774069968f; jenkins-timestamper-offset=-28800000
        while(headerNames.hasMoreElements()){
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            System.out.println(name+":"+value);

        }
        //get传入参数 http://localhost:8080/community/alpha/http?code=134
        System.out.println(request.getParameter("code"));//134

        //返回响应数据

        response.setContentType("text/html;charset=utf-8");
        try(PrintWriter writer = response.getWriter()) {
            writer.write("<h1>Niuko<h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //GET请求
    // /students?current=1&limit=20
    //http://localhost:8080/community/alpha/students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@RequestParam(name = "current",required = false,defaultValue = "1") int current,
                              @RequestParam(name="limit",required = false,defaultValue = "10") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    //  /student/12
    //http://localhost:8080/community/alpha/student/12
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
        //设置cookie存放时间 单位秒
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
