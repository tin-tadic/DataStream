package com.fsre.streamerapplication.streamer.configuration.jobs.queuemessages;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;


@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "5m")
@Component
public class QueueMessagesJobLauncher {
    private final Job sendMessages;
    private final JobLauncher jobLauncher;

    private static final Logger logger = LoggerFactory.getLogger(QueueMessagesJobLauncher.class);

    public QueueMessagesJobLauncher(@Qualifier("queueMessagesJob") Job sendMessages, JobLauncher jobLauncher) {
        this.sendMessages = sendMessages;
        this.jobLauncher = jobLauncher;
    }


    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    @SchedulerLock(name = "Task_Send_Messages_To_Queue-#{'${app.env}'}")
    protected void runJob() {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

        // From 24 hours ago
        Calendar calFrom = Calendar.getInstance();
        calFrom.add(Calendar.HOUR, -24);
        jobParametersBuilder.addDate("from", calFrom.getTime());

        // up to now
        Calendar calTo = Calendar.getInstance();
        jobParametersBuilder.addDate("to", calTo.getTime());

        try {
            jobLauncher.run(sendMessages, jobParametersBuilder.toJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            logger.error("Queue messages job error: {}", e.getMessage());
        }
    }
}
