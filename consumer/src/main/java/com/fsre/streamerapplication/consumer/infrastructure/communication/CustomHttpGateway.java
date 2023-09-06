package com.fsre.streamerapplication.consumer.infrastructure.communication;

import com.fsre.streamerapplication.consumer.configuration.rmq.RMQSender;
import com.fsre.streamerapplication.consumer.domain.Data;
import com.fsre.streamerapplication.consumer.domain.response.MessageErrorResponse;
import com.fsre.streamerapplication.consumer.domain.response.MessageResponse;
import com.fsre.streamerapplication.consumer.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class CustomHttpGateway {
    private final WebClient webClient;
    private final MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(CustomHttpGateway.class);
    private final String specialKey;
    private final RMQSender rmqSender;


    public CustomHttpGateway(
            WebClient webClient,
            MongoTemplate mongoTemplate,
            @Value("${special.key}") String specialKey,
            RMQSender rmqSender
    ) {
        this.webClient = webClient;
        this.mongoTemplate = mongoTemplate;
        this.specialKey = specialKey;
        this.rmqSender = rmqSender;
    }


    public void sendMessage(List<Data> messages) {

        try {
            webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("MY-CUSTOM-HEADER-KEY", specialKey)
                    .body(BodyInserters.fromValue(messages))
                    .exchangeToMono(r -> r.bodyToMono(MessageResponse.class))
                    .timeout(Duration.ofMillis(15000L))
                    .doOnSuccess(response -> doOnSuccess(response, messages))
                    .doOnError(response -> doOnError(response, messages))
                    .onErrorResume(ex -> Mono.empty())
                    .subscribe();
        } catch (WebClientResponseException we) {
            throw new CustomException(we.getMessage(), we.getRawStatusCode());
        }
    }


    private void handleFailedMessage(
            List<Data> messages,
            List<Data> requeueableMessages,
            MessageErrorResponse returnedErroredMessage
    ) {

        logger.error(
                "Error for message {} is {}, httpCode: {}",
                returnedErroredMessage.getId(),
                returnedErroredMessage.getMessage(),
                returnedErroredMessage.getHttpCode()
        );

        // Find the message that failed to send
        Data failedMessage = messages.stream()
                .filter(message -> message.getId().equals(returnedErroredMessage.getId()))
                .findAny()
                .get();

        if (isErrorRetriable(returnedErroredMessage)) {
            requeueableMessages.add(failedMessage);
        } else {
            // Remove it from the list of sent messages, making that list only contain successfully sent messages at the end of iteration
            messages.remove(failedMessage);

            // Save failed messages to db so we don't try to resend it
            failedMessage.setErrorInfo(returnedErroredMessage.getMessage());
            mongoTemplate.save(failedMessage);
        }

    }

    private void doOnSuccess(MessageResponse response, List<Data> messages) {
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Data.class);

        // A partial success (or even one where all messages fail) still returns 207, so we have to check for errors
        if (response.getErrorCount() != 0) {
            logger.error("There were issues sending some messages: {}", response);

            // Handle all messages that failed to send case by case
            List<Data> requeueableMessages = new ArrayList<>();
            for (MessageErrorResponse returnedMessageErrorResponse : response.getErrors()) {
                handleFailedMessage(messages, requeueableMessages, returnedMessageErrorResponse);
            }

            // Requeue the ones with "safe" errors
            rmqSender.returnToQueue(requeueableMessages);
        }

        // Save all successfully sent messages as such
        for (Data message : messages) {
            message.setAsSent();
            bulkOperations.updateMulti(
                    Query.query(Criteria.where("_id").is(message.getId())),
                    new Update().set("sentAt", message.getSentAt())
            );
        }

        bulkOperations.execute();
    }

    private void doOnError(Throwable response, List<Data> messages) {
        logger.error("ERROR sending messages: {}", response.toString());

        if (response instanceof CustomException) {
            logger.error("Received code: {}", ((CustomException) response).getCode());
        }

        // Resend to queue in case of timeout or rate limiter error
        if (isTimeoutError(response)) {
            logger.error("Timeout or rate limiter error, requeueing messages: {}", response.getMessage());
            rmqSender.returnToQueue(messages);
        } else if (is500Error(response)) {
            logger.error("Received 500 from destination API, retrying send: {}", messages);
            rmqSender.returnToQueue(messages);
        } else {
            logger.error(
                    "Critical error sending messages! HttpCode: {} Message: {} Messages: {}",
                    ((CustomException) response).getCode(),
                    response.getMessage(),
                    messages
            );
            BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Data.class);

            // Prevent resending of messages for other errors
            for (Data message : messages) {
                message.setErrorInfo(response.getMessage());
                bulkOperations.updateMulti(
                        Query.query(Criteria.where("_id").is(message.getId())),
                        new Update().set("errorInfo", message.getErrorInfo())
                );
            }
            bulkOperations.execute();
        }
    }

    private static boolean isTimeoutError(Throwable response) {
        return response instanceof TimeoutException
                || (response instanceof CustomException && ((CustomException) response).getCode() == 429);
    }

    private static boolean is500Error(Throwable response) {
        return response instanceof CustomException
                && ((CustomException) response).getCode() == 500;
    }

    private boolean isErrorRetriable(MessageErrorResponse errorResponse) {
        if ("Connection error, please retry!".equals(errorResponse.getMessage())) {
            logger.error("Third party server connection error, requeueing messages: {}", errorResponse.getMessage());
            return true;
        }

        return false;
    }
}
