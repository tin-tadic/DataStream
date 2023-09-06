package com.fsre.streamerapplication.streamer.application.service;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.reactivestreams.client.MongoClient;
import org.bson.Document;

import java.util.List;

public class ChangeStreamPublisher {

    public static com.mongodb.reactivestreams.client.ChangeStreamPublisher<Document> create(MongoClient client) {
        String collectionName = "someCollection";

        return client.watch(
                List.of(
                        Aggregates.match(
                                Filters.and(
                                        Filters.in("ns.coll", collectionName),
                                        Filters.eq("fullDocument.origin", "myOrigin"),
                                        Filters.in("fullDocument.version", 0)
                                )
                        )
                )
        ).fullDocument(FullDocument.UPDATE_LOOKUP);
    }
}
