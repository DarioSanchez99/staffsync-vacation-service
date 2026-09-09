package com.staffsync.vacation.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "staffsync.notifications";

    @Bean
    public TopicExchange staffsyncNotificationsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
}
