package com.fsre.streamerapplication.streamer.configuration.database;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MongoConfig {

    @Value("${mongodb.server.uri}")
    private String serverUri;

    @Bean(name = "mongoClient")
    public MongoClient serverConnect() {
        return MongoClients.create(new ConnectionString(serverUri));
    }

    @Bean(name = "reactiveMongoClient")
    public com.mongodb.reactivestreams.client.MongoClient reactiveServerConnect() {
        return com.mongodb.reactivestreams.client.MongoClients.create(new ConnectionString(serverUri));
    }
}
