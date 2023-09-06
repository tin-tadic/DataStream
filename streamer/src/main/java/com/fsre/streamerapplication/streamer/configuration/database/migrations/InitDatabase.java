package com.fsre.streamerapplication.streamer.configuration.database.migrations;

import com.fsre.streamerapplication.streamer.domain.Data;
import com.fsre.streamerapplication.streamer.domain.ResumeToken;
import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;

import java.time.Duration;

@ChangeLog
public class InitDatabase {

    @ChangeSet(order = "001", id = "createDatabase", author = "tin")
    public void createDatabase(MongockTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists(ResumeToken.class)) {
            mongoTemplate.createCollection(ResumeToken.class);
        }

        if (!mongoTemplate.collectionExists(Data.class)) {
            mongoTemplate.createCollection(Data.class);
        }
    }

    @ChangeSet(order = "002", id = "createIndexes", author = "tin")
    public void createIndexes(MongockTemplate mongoTemplate) {
        mongoTemplate.indexOps(Data.class).ensureIndex(
                new CompoundIndexDefinition(Document.parse("{ 'createdAt': -1, 'publishedAt': 1 }"))
        );

        mongoTemplate.indexOps(Data.class).ensureIndex(
                (new Index().on("sentAt", Sort.Direction.DESC)).expire(Duration.ofDays(30))
        );
    }
}
