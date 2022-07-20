package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //初始化根节点
    private TrieNode rootNode = new TrieNode();

    //读取资源文件构造敏感词前缀树
    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //添加到前缀树
                this.addKeyword(keyword);
            }

        } catch (IOException e) {
            logger.error("加载敏感词文件失败", e.getMessage());
        }
    }

    //将每一个敏感词添加到前缀树里面
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;

        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                //初始化子节点
                subNode = new TrieNode();

                tempNode.addSubNode(c, subNode);
            }
            //指针变为子节点，接着循环依次添加子节点
            tempNode = subNode;

            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     *
     * @param text 未过滤的文本
     * @return 已过滤的文本
     */
    public  String  filter(String text) {

        if (StringUtils.isBlank(text)) {
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (begin< text.length()) {
            if (position<text.length()) {
                char c = text.charAt(position);

                //跳过符号比如 ☆开☆票☆，以上文本中情况也是需要过滤的,符号保留
                if (isSynbol(c)) {
                    //若指针1处于根节点，说明才开始过滤或者已经过滤一轮了
                    if (tempNode == rootNode) {
                        sb.append(c);
                        begin++;
                    }
                    //
                    position++;
                    continue;
                }
                //检查下一个节点
                //此时指针1指向根节点，从根节点获取子节点为空说明不是敏感字符
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    //将指针2指向的字符保留
                    sb.append(text.charAt(begin));
                    position = ++begin;
                    //指针1归位重新指向根节点，重新一轮过滤
                    tempNode = rootNode;


                } else if (tempNode.isKeywordEnd()) {
                    //发现一整个敏感词，将begin-position的字符串替换掉
                    sb.append(REPLACEMENT);
                    begin = ++position;
                    tempNode = rootNode;
                }
                //检查下一个字符
                else if (position < text.length() - 1) {

                    position++;

                }

            } // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }

        }
        return sb.toString();
    }

    private  boolean isSynbol(Character c){
        //0x2E780- 0x9FFF是东亚文，之外就是认为是符号
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }

    //定义前缀树
    private class TrieNode {
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
    }


}
