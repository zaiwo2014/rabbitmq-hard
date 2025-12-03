package com.leung.rabbitmqhard;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})   //一定要显式屏蔽,不然监听器注解会使用默认的连接工厂
@EnableRabbit       //一定要显式开启
public class RabbitmqHardApplication {
    public static void main(String[] args) {
        SpringApplication.run(RabbitmqHardApplication.class, args);
    }

}
