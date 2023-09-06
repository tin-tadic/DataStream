package com.fsre.streamerapplication.consumer.configuration.http;

import com.fsre.streamerapplication.consumer.util.AuthorizationKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpRequest;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Slf4j
public class CustomRequestSigner {

    private final String secret;
    private final String accessKey;

    public CustomRequestSigner(String secret, String accessKey) {
        this.secret = secret;
        this.accessKey = accessKey;
    }

    public void injectHeader(ClientHttpRequest clientRequest, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException  {
        String authHeader = AuthorizationKeyGenerator.getAuthorizationKey(secret,accessKey,(int) (Instant.now().getEpochSecond()));
        log.debug("injectHeader for: [" + clientRequest.getURI() +"], data=" + new String(data) + " sign " + authHeader );
        clientRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
    }
}
