package com.fsre.streamerapplication.streamer.application.service;

import com.fsre.streamerapplication.streamer.domain.Data;
import com.fsre.streamerapplication.streamer.domain.DataRepository;
import com.fsre.streamerapplication.streamer.domain.ResumeToken;
import com.fsre.streamerapplication.streamer.domain.ResumeTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSaver {
    @Value("${mongodb.server.cluster-id}")
    private String clusterName;
    private final ResumeTokenRepository mongoResumeTokenRepository;

    private final DataRepository dataRepository;

    public DatabaseSaver(DataRepository dataRepository, ResumeTokenRepository mongoResumeTokenRepository) {
        this.dataRepository = dataRepository;
        this.mongoResumeTokenRepository = mongoResumeTokenRepository;
    }

    @Async
    public void saveToDatabase(Data data, String resumeToken) {
        // message first, resume token later
        dataRepository.save(data);

        mongoResumeTokenRepository.save(new ResumeToken(clusterName, resumeToken));
    }

}
