package com.fsre.streamerapplication.consumer.configuration.rmq;

import com.fsre.streamerapplication.consumer.domain.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RMQSender {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routingKey}")
    private String routingKey;

    public RMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    // These messages are presumed to be already marked as published in the database
    public void returnToQueue(List<Data> messages) {
        rabbitTemplate.convertAndSend(exchange, routingKey, messages);
    }
}
