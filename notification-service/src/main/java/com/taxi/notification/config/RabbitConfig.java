package com.taxi.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "trip.events";
    public static final String QUEUE = "trip.events.queue";

    @Bean
    public TopicExchange tripEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue tripEventsQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding binding(Queue tripEventsQueue, TopicExchange tripEventsExchange) {
        return BindingBuilder.bind(tripEventsQueue).to(tripEventsExchange).with("#");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }
}
