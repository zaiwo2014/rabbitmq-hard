package com.leung.rabbitmqhard;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static com.leung.rabbitmqhard.middleware.rabbitmq.config.RabbitmqConfig.SIMPLE_ROUTING_KEY;

@SpringBootTest
public class RabbitmqHardApplicationTests {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    @Disabled
    public void testMQ() {

        // 创建消息体
        byte[] body = "Hello World".getBytes();

        // 构建 Message
        Message message = MessageBuilder.withBody(body)
                .build();
        // 创建带ID的CorrelationData
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.send(SIMPLE_ROUTING_KEY, message, correlationData);
    }

}
