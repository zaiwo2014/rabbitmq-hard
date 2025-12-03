package com.leung.rabbitmqhard.middleware.rabbitmq.listener;


import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.leung.rabbitmqhard.middleware.rabbitmq.config.RabbitmqConfig.SIMPLE_QUEUE;

@Slf4j
@Component
public class QueueConsumer{

    @RabbitListener(queues = SIMPLE_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleMessage(Message message, Channel channel) throws IOException {
        try {
            log.info(">>>>>>>>>>>接收到了消息 : {}", message);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 拒绝消息并决定是否重新排队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            log.error(e.getMessage());
        }
    }
}
