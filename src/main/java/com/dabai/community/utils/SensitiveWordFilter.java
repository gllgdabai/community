package com.dabai.community.utils;

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

/** 敏感词过滤器
 * @author
 * @create 2022-04-01 22:32
 */
@Component
public class SensitiveWordFilter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-word.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        ){
            String keyword;
            while ((keyword = br.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            log.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    /**
     *  过滤敏感词
     * @param text 待过滤的文本
     * @return  过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode tempNode = root;   // 指针1

        int begin = 0;  //指针2
        int position = 0;  // 指针3
        // 存放过滤后的结果
        StringBuilder sb = new StringBuilder();
        while (begin < text.length()) {
            if (position < text.length()) {
                Character c = text.charAt(position);
                // 跳过符号
                if (isSymbol(c)) {
                    if (tempNode == root) {
                        sb.append(c);
                        begin++;
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getChildNode(c);
                if (tempNode == null) {
                    // 说明以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = root;
                } else if (tempNode.isKeywordEnd){
                    // 发现一个敏感词，将begin~position的字符串 替换掉
                    sb.append(REPLACEMENT);
                    begin = ++position;     // 进入下一个位置
                    tempNode = root;        // 重新指向根节点
                } else {    // 检查下一个字符
                    position++;
                }
            }
            else{   // position遍历越界仍未匹配到敏感词
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = root;
            }
        }
        return sb.toString();
    }
    /** 判断是否为符号 */
    private boolean isSymbol(Character c) {
        // 0x2E80 ~ 0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = root; // 临时指针
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode childNode = tempNode.getChildNode(c);
            if (childNode == null) {
                // 初始化子节点
                childNode = new TrieNode();
                tempNode.addChildNode(c, childNode);
            }
            // 指向子节点，进入下一轮循环
            tempNode = childNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.isKeywordEnd = true;
            }
        }
    }


    // 前缀树节点类
    private class TrieNode {
        // 关键词结束标识
        boolean isKeywordEnd = false;
        // 子节点（key是下级字符，value是下级节点）
        Map<Character, TrieNode> childNodes = new HashMap<>();

        // 添加子节点
        void addChildNode(Character c, TrieNode node) {
            childNodes.put(c, node);
        }

        // 获取子节点
        TrieNode getChildNode(Character c) {
            return childNodes.get(c);
        }
    }
}
