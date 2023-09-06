package com.fsre.streamerapplication.streamer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("resume-token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeToken {

    @Id
    private String clusterName;
    @Field(name = "resumeToken")
    private String mongoResumeToken;
}
