package com.plasencia.ms_seguridad.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Configuraci&#243;n criptogr&#225;fica del Authorization Server.
 *
 * <p>Genera un par de claves RSA al arrancar. La clave privada firma los JWT (RS256)
 * y la clave p&#250;blica se publica en el endpoint JWKS para que los Resource Servers
 * (ms-reserva, api-gateway) puedan validar la firma sin compartir secretos.</p>
 */
@Configuration
public class JwtConfig {

    /**
     * Par de claves RSA empaquetado como JWK (incluye un "kid" &#250;nico por arranque,
     * lo que permite a los Resource Servers refrescar autom&#225;ticamente la clave si
     * el servicio se reinicia).
     */
    @Bean
    public RSAKey rsaKey() {
        KeyPair keyPair = generarParDeClaves();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    /** Fuente de claves que sirve el endpoint JWKS. */
    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /** Codificador: firma los JWT con la clave privada. */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /** Decodificador local: valida los JWT que el propio servicio emite. */
    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    private KeyPair generarParDeClaves() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el par de claves RSA", e);
        }
    }
}
