package com.fsre.streamerapplication.streamer.configuration.jobs.queuemessages;

import com.fsre.streamerapplication.streamer.configuration.rmq.RMQSender;
import com.fsre.streamerapplication.streamer.domain.Data;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QueueMessagesItemWriterConfig implements ItemWriter<Data> {
    private final RMQSender rmqSender;

    public QueueMessagesItemWriterConfig(RMQSender rmqSender) {
        this.rmqSender = rmqSender;
    }

    @Override
    public void write(List<? extends Data> messages) {
        rmqSender.sendToQueue((List<Data>) messages);
    }

    @Bean(name = "queueMessagesWriter")
    public ItemWriter<Data> writer() {
        return new QueueMessagesItemWriterConfig(rmqSender);
    }
}
