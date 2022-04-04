package com.dabai.community;

import com.dabai.community.utils.SensitiveWordFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 测试敏感词过滤功能
 * @author
 * @create 2022-04-04
 */
@SpringBootTest
public class SensitiveWordTests {
    @Autowired
    private SensitiveWordFilter filter;

    @Test
    public void testSensitiveWordFilter() {
        String text = "这里可以嫖娼，可以赌博，可以吸毒，可以开票，哈哈哈";
        text = this.filter.filter(text);
        System.out.println(text);

        text = "这里可以嫖娼，可以赌赌博博，可以吸毒，可以开票";
        text = this.filter.filter(text);
        System.out.println(text);

        text = "这里可以嫖娼赌博吸毒，可以赌赌博博，可以开票";
        text = this.filter.filter(text);
        System.out.println(text);

        text = "这里可以嫖娼赌博吸毒，可以赌赌博博，可以开票";
        text = this.filter.filter(text);
        System.out.println(text);
    }
}
