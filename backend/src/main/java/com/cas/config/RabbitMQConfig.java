package com.cas.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "cas.exchange.direct";
    public static final String NOTIFICATION_QUEUE = "cas.queue.notification";
    public static final String ENROLLMENT_LOG_QUEUE = "cas.queue.enrollment.log";
    public static final String NOTIFICATION_KEY = "notification";
    public static final String ENROLLMENT_LOG_KEY = "enrollment.log";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Queue enrollmentLogQueue() {
        return new Queue(ENROLLMENT_LOG_QUEUE, true);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(directExchange()).with(NOTIFICATION_KEY);
    }

    @Bean
    public Binding enrollmentLogBinding() {
        return BindingBuilder.bind(enrollmentLogQueue())
                .to(directExchange()).with(ENROLLMENT_LOG_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}