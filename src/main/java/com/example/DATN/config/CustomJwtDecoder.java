package com.example.DATN.config;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.secretKey}")
    private String signerKey;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {

        if (nimbusJwtDecoder == null) {
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(signerKey.getBytes(), "HS512");

            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
