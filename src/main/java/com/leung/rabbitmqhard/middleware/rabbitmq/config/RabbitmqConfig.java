package com.leung.rabbitmqhard.middleware.rabbitmq.config;


import com.leung.rabbitmqhard.middleware.rabbitmq.util.RedissonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitmqConfig {

    @Value("${rabbitmq.host:localhost}")
    private String host;

    @Value("${rabbitmq.port:5672}")
    private int port;

    @Value("${rabbitmq.username:admin}")
    private String username;

    @Value("${rabbitmq.password:admin}")
    private String password;

    @Value("${rabbitmq.virtual-host:/}")
    private String virtualHost;

    @Resource
    private RedissonUtil redissonUtil;



    // 队列名称常量
    public static final String SIMPLE_QUEUE = "simple.queue";

    // 交换机名称常量
    public static final String SIMPLE_EXCHANGE = "simple.exchange";

    // 路由键常量
    public static final String SIMPLE_ROUTING_KEY = "simple.routing.key";


    @Bean("connectionFactory")
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);

        // 连接池配置
        connectionFactory.setChannelCacheSize(25);

        //### 可靠性配置 ###

        // 启用发布确认机制
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);

        // 启用发布返回机制
        connectionFactory.setPublisherReturns(true);

        return connectionFactory;
    }

    @Bean("rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // 设置并发消费者数量
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);

        // 设置预取计数，控制每个消费者一次处理的消息数量
        factory.setPrefetchCount(10);

        // 设置确认模式（根据业务需要选择）
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 手动 ack


        return factory;
    }


    /**
     * 声明一个简单队列
     * @return Queue对象
     */
    @Bean("simpleQueue")
    public Queue simpleQueue() {
        return QueueBuilder.durable(SIMPLE_QUEUE).build();
    }

    /**
     * 声明一个简单交换机
     * @return DirectExchange对象
     */
    @Bean("simpleExchange")
    public DirectExchange simpleExchange() {
        return new DirectExchange(SIMPLE_EXCHANGE);
    }

    /**
     * 绑定队列和交换机
     * @param simpleQueue 队列
     * @param simpleExchange 交换机
     * @return Binding对象
     */
    @Bean("simpleBinding")
    public Binding simpleBinding(Queue simpleQueue, DirectExchange simpleExchange) {
        return BindingBuilder.bind(simpleQueue)
                .to(simpleExchange)
                .with(SIMPLE_ROUTING_KEY);
    }

    @Bean("rabbitAdmin")
    public RabbitAdmin rabbitAdmin(@Qualifier("simpleQueue") Queue simpleQueue,@Qualifier("simpleExchange") DirectExchange simpleExchange,@Qualifier("simpleBinding") Binding simpleBinding,@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareQueue(simpleQueue);
        rabbitAdmin.declareExchange(simpleExchange);
        rabbitAdmin.declareBinding(simpleBinding);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 设置消息确认回调 ---> 发布者确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                // 消息成功投递到Broker
                log.info("消息投递成功: {}", correlationData != null ? correlationData.getId() : "unknown");
            } else {
                // 消息投递失败
                log.error("消息投递失败: {}，原因: {}",
                        correlationData != null ? correlationData.getId() : "unknown", cause);
            }
        });

        // 设置消息返回回调 ---> 处理无法路由到队列的情况
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("消息返回: exchange={}, routingKey={}, message={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getMessage());
        });

        // 设置默认交换机和路由键
        rabbitTemplate.setExchange(SIMPLE_EXCHANGE);
        rabbitTemplate.setRoutingKey(SIMPLE_ROUTING_KEY);

        // 设置消息序列化方式
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        // 启用强制返回模式
        rabbitTemplate.setMandatory(true);

        return rabbitTemplate;
    }



}
