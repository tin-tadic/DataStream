package com.fsre.streamerapplication.streamer.configuration.jobs;

import org.springframework.batch.core.scope.StepScope;
import org.springframework.context.annotation.Bean;

public class SpringBatchStepConfig {

    @Bean
    public StepScope stepScope() {
        StepScope stepScope = new StepScope();
        stepScope.setAutoProxy(true);
        return stepScope;
    }
}
