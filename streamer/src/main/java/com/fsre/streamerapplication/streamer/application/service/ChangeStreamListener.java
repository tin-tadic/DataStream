package com.fsre.streamerapplication.streamer.application.service;

import com.fsre.streamerapplication.streamer.application.service.mapper.CustomMapper;
import com.fsre.streamerapplication.streamer.domain.Data;
import com.fsre.streamerapplication.streamer.domain.ResumeToken;
import com.fsre.streamerapplication.streamer.domain.ResumeTokenRepository;
import com.fsre.streamerapplication.streamer.util.SubscriberHelpers;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.reactivestreams.client.MongoClient;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.util.BsonUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChangeStreamListener {

    @Value("${mongodb.server.cluster-id}")
    private String clusterName;
    private final DatabaseSaver databaseSaver;
    private final ResumeTokenRepository mongoResumeTokenRepository;
    private final MongoClient mongoClient;
    private final CustomMapper customMapper;

    public ChangeStreamListener(
            DatabaseSaver databaseSaver,
            @Qualifier("reactiveMongoClient") MongoClient mongoClient,
            ResumeTokenRepository mongoResumeTokenRepository,
            CustomMapper customMapper)
    {
        this.databaseSaver = databaseSaver;
        this.mongoResumeTokenRepository = mongoResumeTokenRepository;
        this.mongoClient = mongoClient;
        this.customMapper = customMapper;
    }

    public void watchDeployment() {
        com.mongodb.reactivestreams.client.ChangeStreamPublisher<Document> publisher = ChangeStreamPublisher.create(mongoClient);
        Optional<ResumeToken> token = mongoResumeTokenRepository.findById(clusterName);

        if (token.isPresent()) {
            publisher.resumeAfter(org.bson.BsonDocument.parse(token.orElseThrow().getMongoResumeToken()));
        }

        publisher.subscribe(new ObservableSubscriber());
    }


    public class ObservableSubscriber extends SubscriberHelpers.BaseSubscriber<ChangeStreamDocument<Document>> {
        @Override
        public void onNext(ChangeStreamDocument<Document> document) {

            if (document.getFullDocument() == null) {
                logger.warn("Empty document {}", document);
                return;
            }

            Document doc = BsonUtils.merge(document.getFullDocument());

            try {
                // If a document changes before it is saved from the stream, ChangeStreams will return the new version...
                // ... and not the one it had when it was inserted
                if (document.getOperationType().equals(OperationType.INSERT)) {
                    doc.put("version", 0);
                } else {
                    // Only track new messages
                    return;
                }
            } catch (NullPointerException exception) {
                logger.warn("Null pointer exception {}", document);
            }

            Data data = customMapper.createFromDocument(doc);

            if (data == null) {
                logger.warn("Message empty for document {}", doc);
            }

            databaseSaver.saveToDatabase(data, document.getResumeToken().toString());
        }

        @Override
        public void onComplete() {
            super.onComplete();
            getSubscription().cancel();
            watchDeployment();
        }
    }
}
