package com.leung.rabbitmqhard.middleware.rabbitmq.listener;

import com.leung.rabbitmqhard.middleware.rabbitmq.util.RedissonUtil;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.leung.rabbitmqhard.middleware.rabbitmq.config.RabbitmqConfig.SIMPLE_QUEUE;

@Slf4j
@Component
public class QueueConsumer {

    @Resource
    private RedissonUtil redissonUtil;

    private static final String PROCESSED_MESSAGE_PREFIX = "rabbitmq:processed:";
    private static final String LOCK_PREFIX = "rabbitmq:lock:";
    private static final long PROCESS_TIMEOUT = 300L; // 5分钟

    @RabbitListener(queues = SIMPLE_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();

        if (StringUtils.isEmpty(messageId)) {
            log.error("消息缺少唯一标识，无法去重");
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException e) {
                log.error("ACK失败: {}", e.getMessage());
            }
            return;
        }

        String lockKey = LOCK_PREFIX + messageId;
        String processedKey = PROCESSED_MESSAGE_PREFIX + messageId;

        RLock lock = redissonUtil.getLock(lockKey);

        try {
            // 获取分布式锁，防止并发处理同一消息
            if (lock.tryLock(10, PROCESS_TIMEOUT, TimeUnit.SECONDS)) {
                try {
                    // 双重检查消息是否已处理
                    if (redissonUtil.exists(processedKey)) {
                        log.info("消息已处理过，直接确认: {}", messageId);
                        channel.basicAck(deliveryTag, false);
                        return;
                    }

                    // 处理业务逻辑
                    processBusinessLogic(message);

                    // 标记消息已处理
                    redissonUtil.set(processedKey, "1", 7, TimeUnit.DAYS);

                    // 确认消息
                    channel.basicAck(deliveryTag, false);
                    log.info("消息处理完成并确认: {}", messageId);
                } catch (Exception e) {
                    log.error("消息处理异常: {}", e.getMessage(), e);
                    // 拒绝消息并重新入队
                    channel.basicNack(deliveryTag, false, true);
                }
            } else {
                log.warn("未能获取到锁，消息可能正在被其他实例处理: {}", messageId);
                // 不确认消息，让其重新入队
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("线程中断: {}", e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("NACK失败: {}", ioException.getMessage(), ioException);
            }
        } catch (IOException e) {
            log.error("消息操作失败: {}", e.getMessage(), e);
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void processBusinessLogic(Message message) {
        // 实际业务处理逻辑
        try {
            String content = new String(message.getBody());
            log.info("开始处理消息: {}", content);
            // 模拟业务处理耗时
            Thread.sleep(2000);
            log.info("消息处理完毕: {}", content);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("业务处理被中断", e);
        } catch (Exception e) {
            throw new RuntimeException("业务处理失败", e);
        }
    }
}
