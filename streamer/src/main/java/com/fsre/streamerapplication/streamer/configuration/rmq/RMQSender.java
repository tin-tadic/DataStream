package com.fsre.streamerapplication.streamer.configuration.rmq;

import com.fsre.streamerapplication.streamer.domain.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RMQSender {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routingKey}")
    private String routingKey;

    private final MongoTemplate mongoTemplate;
    private final RabbitTemplate rabbitTemplate;

    public RMQSender(MongoTemplate mongoTemplate, RabbitTemplate rabbitTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }


    public void sendToQueue(List<Data> messages) {
        // Null check because BulkOps throws an error if it gets an empty list
        if (messages.size() == 0) {
            return;
        }

        // Some batches contain duplicate messages. This is less a proper fix, and more a workaround
        Set<Data> messageSet = new HashSet<>(messages);

        rabbitTemplate.convertAndSend(exchange, routingKey, messageSet);
        saveMessagesAsPublished(messageSet);
    }

    private void saveMessagesAsPublished(Set<Data> messages) {
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Data.class);

        for (Data message : messages) {
            message.setAsPublished();
            bulkOperations.updateMulti(
                    Query.query(Criteria.where("_id").is(message.getId())),
                    new Update().set("publishedAt", message.getPublishedAt())
            );
        }

        bulkOperations.execute();
    }
}
