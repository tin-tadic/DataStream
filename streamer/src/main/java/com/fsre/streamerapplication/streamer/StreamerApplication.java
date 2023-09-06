package com.fsre.streamerapplication.streamer;

import com.fsre.streamerapplication.streamer.application.service.ChangeStreamListener;
import com.github.cloudyrock.spring.v5.EnableMongock;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableMongock
@SpringBootApplication
@EnableAsync
public class StreamerApplication implements CommandLineRunner {
    private final ChangeStreamListener changeStreamListener;

    public StreamerApplication(ChangeStreamListener changeStreamListener) {
        this.changeStreamListener = changeStreamListener;
    }

    public static void main(String[] args) {
        SpringApplication.run(StreamerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        changeStreamListener.watchDeployment();
    }
}
