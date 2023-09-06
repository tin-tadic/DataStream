package com.fsre.streamerapplication.consumer.configuration.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CustomClientConfiguration {

    @Bean
    public WebClient webClient(
            @Value("${custom.secret}") String secret,
            @Value("${custom.access-key}") String accessKey,
            @Value("${custom.uri}") String uri
    ) {
        CustomRequestSigner signer = new CustomRequestSigner(secret, accessKey);
        BodyProvidingJsonEncoder bodyProvidingJsonEncoder = new BodyProvidingJsonEncoder(signer);

        return WebClient.builder()
                .exchangeFunction(ExchangeFunctions.create(
                        new MessageSigningHttpConnector(),
                        ExchangeStrategies
                                .builder()
                                .codecs(clientDefaultCodecsConfigurer -> {
                                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(bodyProvidingJsonEncoder);
                                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(new ObjectMapper(), MediaType.APPLICATION_JSON));
                                })
                                .build()
                ))
                .baseUrl(uri)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(CustomClientWebClientFilter.logRequest())
                .filter(CustomClientWebClientFilter.logResponse())
                .build();
    }
}
