package org.example.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Value("${app.queue.seat-creation-queue:seat.creation.queue}")
    private String seatCreationQueue;
    
    @Value("${app.queue.booking-queue:booking.queue}")
    private String bookingQueue;
    
    @Bean
    public Queue seatCreationQueue() {
        return QueueBuilder.durable(seatCreationQueue).build();
    }
    
    @Bean
    public Queue bookingQueue() {
        return QueueBuilder.durable(bookingQueue).build();
    }
    
    @Bean
    public TopicExchange seatCreationExchange() {
        return new TopicExchange("seat.creation.exchange");
    }
    
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange("booking.exchange");
    }
    
    @Bean
    public Binding seatCreationBinding() {
        return BindingBuilder
                .bind(seatCreationQueue())
                .to(seatCreationExchange())
                .with("seat.creation.*");
    }
    
    @Bean
    public Binding bookingBinding() {
        return BindingBuilder
                .bind(bookingQueue())
                .to(bookingExchange())
                .with("booking.*");
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
