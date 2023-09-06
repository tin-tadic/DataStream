package com.fsre.streamerapplication.streamer.configuration.jobs.queuemessages;

import com.fsre.streamerapplication.streamer.domain.Data;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableBatchProcessing
@Configuration
public class QueueMessagesJobConfig
{
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private static final String JOB_NAME = "queueMessagesJob";

    public QueueMessagesJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }


    @Bean(name = "queueMessagesTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.initialize();

        return executor;
    }

    @Bean(name = "queueMessagesJob")
    public Job queueMessages(@Qualifier("queueMessagesStep") Step step1) {
        return jobBuilderFactory
                .get(JOB_NAME)
                .start(step1)
                .build();
    }

    @Bean(name = "queueMessagesStep")
    public Step step1(
            @Qualifier("queueMessagesReader")  MongoItemReader<Data> queueMessagesReader,
            @Qualifier("queueMessagesWriter") ItemWriter<? super Data> queueMessagesWriter,
            @Qualifier("queueMessagesTaskExecutor") ThreadPoolTaskExecutor queueMeessagesTaskExecutor
    ) {
        return stepBuilderFactory.get("step1")
                .allowStartIfComplete(true)
                .<Data, Data>chunk(100)
                .reader(queueMessagesReader)
                .writer(queueMessagesWriter)
                .taskExecutor(queueMeessagesTaskExecutor)
                .throttleLimit(10)
                .build();
    }
}
