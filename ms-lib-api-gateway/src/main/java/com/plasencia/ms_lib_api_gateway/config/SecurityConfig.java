package com.plasencia.ms_lib_api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad del API Gateway como Resource Server (JWT).
 *
 * El JWT se valida contra el JWKS del ms-seguridad mediante la propiedad
 * spring.security.oauth2.resourceserver.jwt.jwk-set-uri (Spring autoconfigura el JwtDecoder).
 *
 * - POST /auth/login es PUBLICO (permitAll): es el endpoint que emite el token.
 * - /actuator/** publico (health/info).
 * - El resto requiere un JWT valido (authenticated).
 * - El control fino por rol/permiso lo realizan los microservicios destino.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
