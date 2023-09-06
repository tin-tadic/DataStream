package com.fsre.streamerapplication.consumer.configuration.jobs.pollingconsumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PollingConsumerThreadPoolConfiguration {
    @Bean(name = "pollingConsumerTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setThreadNamePrefix("pollingConsumer-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
