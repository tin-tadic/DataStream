package com.fsre.streamerapplication.consumer.configuration.jobs.pollingconsumer;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class PollingMessageScheduler {
    private final PollingMessageConsumer pollingMessageConsumer;

    public PollingMessageScheduler(PollingMessageConsumer pollingMessageConsumer) {
        this.pollingMessageConsumer = pollingMessageConsumer;
    }


    // 40 requests/minute
    @Async("pollingConsumerTaskExecutor")
    @Scheduled(fixedDelay = 1500, initialDelay = 10000)
    public void consumeSingleBatch() {
        pollingMessageConsumer.consumeSingleMessage();
    }
}
