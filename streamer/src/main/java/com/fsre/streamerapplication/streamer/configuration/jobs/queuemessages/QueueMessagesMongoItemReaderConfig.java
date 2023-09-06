package com.fsre.streamerapplication.streamer.configuration.jobs.queuemessages;

import com.fsre.streamerapplication.streamer.domain.Data;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class QueueMessagesMongoItemReaderConfig {

    private final MongoTemplate mongoTemplate;

    public QueueMessagesMongoItemReaderConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Bean(name = "queueMessagesReader")
    @StepScope
    public MongoItemReader<Data> reader(
            @Value("#{jobParameters['from']}") Date from,
            @Value("#{jobParameters['to']}") Date to
    ) {
        Query query = new Query();

        query.addCriteria(Criteria
                .where("createdAt")
                .gte(from)
                .lte(to)
        );
        query.addCriteria(Criteria.where("errorInfo").exists(false));
        query.addCriteria(Criteria.where("publishedAt").exists(false));

        query.noCursorTimeout();
        query.cursorBatchSize(1000);

        MongoItemReader<Data> reader = new MongoItemReader<>();
        reader.setTemplate(mongoTemplate);
        reader.setMaxItemCount(1000);
        reader.setSort(Map.of("createdAt", Sort.Direction.ASC));
        reader.setTargetType(Data.class);
        reader.setSaveState(false);
        reader.setQuery(query);

        return reader;
    }
}
