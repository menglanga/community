package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //MD5加密,只能加密不能解密  hello->985f998fd,,每次加密的结果都是这一个值
    //hello（明文密码）+一个盐值（随机字符串） -> md5密文
    public static String md5(String salt) {
        if (StringUtils.isBlank(salt)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(salt.getBytes());
    }


    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg,null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code,null,null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "nihao");
        map.put("age", 45);
        System.out.println(getJSONString(0, "ok", map));
    }

}
