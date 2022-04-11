package com.dabai.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author
 * @create 2022-04-11 11:36
 */
@SpringBootTest
public class KafkaTests {

    @Autowired
    private KafkaProducer producer;

    @Test
    public void testKafka() {
        producer.sendMessage("test", "这是第1条消息：我是大白!");
        producer.sendMessage("test", "这是第2条消息：在吗?");
        producer.sendMessage("test", "这是第3条消息：好久不见!");
        try {
            Thread.sleep(10  * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

@Component
class KafkaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

@Component
class KafkaComsumer {

    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}
