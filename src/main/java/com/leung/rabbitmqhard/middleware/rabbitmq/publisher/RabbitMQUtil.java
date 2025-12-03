package com.leung.rabbitmqhard.middleware.rabbitmq.publisher;


import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class RabbitMQUtil {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMsg(Object object) {
        // 生成全局唯一消息ID
        String messageId = UUID.randomUUID().toString();

        // 设置消息属性
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(messageId);
        messageProperties.setTimestamp(new Date());
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

        // 创建消息对象
        Message msg = new Message(JSON.toJSONString(object).getBytes(), messageProperties);

        // 发送消息
        rabbitTemplate.send(msg);
    }
}
