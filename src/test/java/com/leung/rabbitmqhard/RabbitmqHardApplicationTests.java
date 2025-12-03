package com.leung.rabbitmqhard;

import com.leung.rabbitmqhard.middleware.rabbitmq.publisher.RabbitMQUtil;
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
    private RabbitMQUtil rabbitMQUtil;

    @Test
    public void testMQ() {
        rabbitMQUtil.sendMsg("hello world");
    }

}
