package com.fsre.streamerapplication.consumer.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AuthorizationKeyGenerator {

    private AuthorizationKeyGenerator() {

    }

    public static String getAuthorizationKey(String secret, String accessKey, int timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String stringToSign = "AccessKey" + " " + accessKey + " " + timestamp;
        String signature = Base64.getEncoder().encodeToString(digest(stringToSign, secret));
        return stringToSign + " " + signature;
    }

    private static byte[] digest(String msg, String keyString) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec key = new SecretKeySpec((keyString).getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);

        byte[] bytes = mac.doFinal(msg.getBytes(StandardCharsets.US_ASCII));

        StringBuilder hash = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }

        return hash.toString().getBytes(StandardCharsets.UTF_8);
    }
}
