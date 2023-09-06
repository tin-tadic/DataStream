package com.fsre.streamerapplication.consumer.domain.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonSerialize
public class MessageResponse {
    private Integer errorCount;
    private List<MessageErrorResponse> errors;
}
