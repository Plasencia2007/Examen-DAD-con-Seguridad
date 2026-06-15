package com.plasencia.ms_seguridad.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.RequiredArgsConstructor;

/**
 * Publica la clave p&#250;blica en formato JWKS. Los Resource Servers (ms-reserva,
 * api-gateway) consumen este endpoint mediante {@code jwk-set-uri} para validar
 * la firma de los JWT. Solo expone la parte p&#250;blica de la clave.
 */
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final RSAKey rsaKey;

    @GetMapping("/oauth2/jwks")
    public Map<String, Object> jwks() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
