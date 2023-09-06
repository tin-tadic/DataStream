package com.fsre.streamerapplication.consumer.configuration.http;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * A Wrapper around the default Jackson2JsonEncoder that captures the serialized body and supplies it to a consumer
 */
@RequiredArgsConstructor
public class BodyProvidingJsonEncoder extends Jackson2JsonEncoder {
    private final CustomRequestSigner signer;

    @Override
	public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory,
			ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints
    ) {
    	
    	return super.encode(inputStream, bufferFactory, elementType, mimeType, hints).flatMap(db -> Mono.subscriberContext().map(sc -> {
            ClientHttpRequest clientHttpRequest = sc.get(MessageSigningHttpConnector.REQUEST_CONTEXT_KEY);

            try {
                signer.injectHeader( clientHttpRequest, extractBytes(db));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                 e.printStackTrace();
            }
            return db;
        }));
    }

    /**
     * Extracts bytes from the DataBuffer and resets the buffer so that it is ready to be re-read by the regular
     * request sending process.
     * @param data data buffer with encoded data
     * @return copied data as a byte array.
     */
    private byte[] extractBytes(DataBuffer data) {
        byte[] bytes = new byte[data.readableByteCount()];
        data.read(bytes);
        data.readPosition(0);
        return bytes;
    }
}
