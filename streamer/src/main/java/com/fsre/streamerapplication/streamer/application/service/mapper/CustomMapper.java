package com.fsre.streamerapplication.streamer.application.service.mapper;

import com.fsre.streamerapplication.streamer.domain.Data;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class CustomMapper {

    public Data createFromDocument(Document document) {
        Data data = new Data();

        data.setId(document.get("_id", ObjectId.class).toString());
        data.setName(document.getString("name"));
        data.setValue(document.getDouble("value"));
        data.setText(document.getString("text"));
        data.setOrigin(document.getString("origin"));
        data.setTimestamp(document.getInteger("timestamp"));
        data.setVersion(document.getInteger("version"));
        data.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        return data;
    }
}
