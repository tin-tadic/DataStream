package com.fsre.streamerapplication.consumer.domain.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonSerialize
public class MessageErrorResponse {
    private String id;
    private Integer httpCode;
    private Integer appCode;
    private String message;
}
