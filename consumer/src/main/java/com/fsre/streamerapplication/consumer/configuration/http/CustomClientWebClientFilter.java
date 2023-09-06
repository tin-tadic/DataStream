package com.fsre.streamerapplication.consumer.configuration.http;

import com.fsre.streamerapplication.consumer.exception.CustomInvalidRequest;
import com.fsre.streamerapplication.consumer.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class CustomClientWebClientFilter {

    private static final Logger logger = LoggerFactory.getLogger(CustomClientWebClientFilter.class);

    private CustomClientWebClientFilter() {

    }

    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            logMethodAndUrl(request);
            logHeaders(request);

            return Mono.just(request);
        });
    }


    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            logStatus(response);
            logHeaders(response);

            return logBody(response);
        });
    }


    private static void logStatus(ClientResponse response) {
        HttpStatus status = response.statusCode();
        logger.debug("Returned status code {} ({})", status.value(), status.getReasonPhrase());
    }

    private static Mono<ClientResponse> logBody(ClientResponse response) {
        if (response.statusCode().equals(HttpStatus.CONFLICT)) {
            logger.warn("Duplicate message  {}", response.bodyToMono(String.class));
        } else {
            if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {

                return response.bodyToMono(String.class)
                        .flatMap(body -> {
                            logger.error("Error body is {}", body);
                            CustomException exception = response.statusCode().is4xxClientError() ? new CustomInvalidRequest(body,response.rawStatusCode()) : new CustomException(body, response.rawStatusCode());
                            return Mono.error(exception);
                        });
            }

            logger.debug("Response {}", response.bodyToMono(String.class));
        }
        return Mono.just(response);
    }

    private static void logHeaders(ClientResponse response) {
        response.headers().asHttpHeaders()
                .forEach(
                        (name, values) -> values.forEach(value -> logNameAndValuePair(name, value))
                );
    }


    private static void logHeaders(ClientRequest request) {
        request.headers().forEach((name, values) -> values.forEach(value -> logNameAndValuePair(name, value)));
    }


    private static void logNameAndValuePair(String name, String value) {
        logger.debug("{}={}", name, value);
    }


    private static void logMethodAndUrl(ClientRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.method().name());
        sb.append(" to ");
        sb.append(request.url());
        sb.append(" body ");
        sb.append(request.body());
    }
}
