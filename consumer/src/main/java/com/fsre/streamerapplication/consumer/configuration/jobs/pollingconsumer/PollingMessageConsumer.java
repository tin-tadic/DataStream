package com.fsre.streamerapplication.consumer.configuration.jobs.pollingconsumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsre.streamerapplication.consumer.domain.Data;
import com.fsre.streamerapplication.consumer.infrastructure.communication.CustomHttpGateway;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PollingMessageConsumer {
    @Value("${rabbitmq.queue}")
    public String queueName;
    private final AmqpTemplate template;
    private final CustomHttpGateway gateway;
    private final ObjectMapper mapper = new ObjectMapper();

    public PollingMessageConsumer(AmqpTemplate template, CustomHttpGateway gateway) {
        this.template = template;
        this.gateway = gateway;
    }

    @Async("pollingConsumerTaskExecutor")
    public void consumeSingleMessage() {
        List<Data> messages = mapper.convertValue((template.receiveAndConvert(queueName)), new TypeReference<>() {});

        if (messages != null) {
            gateway.sendMessage(messages);
        }
    }
}
