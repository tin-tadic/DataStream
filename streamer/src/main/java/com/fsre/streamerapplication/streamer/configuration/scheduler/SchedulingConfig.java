package com.fsre.streamerapplication.streamer.configuration.scheduler;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class SchedulingConfig {

    @Bean
    public LockProvider lockProvider(MongoTemplate mongoTemplate) {
        return new MongoLockProvider(mongoTemplate.getDb());
    }
}
