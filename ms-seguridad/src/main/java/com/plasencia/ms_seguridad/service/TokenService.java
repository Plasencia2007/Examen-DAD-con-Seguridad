package com.plasencia.ms_seguridad.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.plasencia.ms_seguridad.entity.Permiso;
import com.plasencia.ms_seguridad.entity.Usuario;

import lombok.RequiredArgsConstructor;

/**
 * Construye y firma el JWT (RS256) que se devuelve tras un login correcto.
 * El token incluye el rol y los permisos del usuario para que los Resource
 * Servers apliquen el control de acceso (RBAC). El claim "roles" se emite
 * como lista (con el único rol) para mantener el contrato con los Resource Servers.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.expiration-seconds}")
    private long expirationSeconds;

    public TokenGenerado generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expirationSeconds, ChronoUnit.SECONDS);

        List<String> roles = List.of(usuario.getRol().getNombre());

        List<String> permisos = usuario.getRol().getPermisos().stream()
                .map(Permiso::getNombre)
                .distinct()
                .sorted()
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(ahora)
                .expiresAt(expira)
                .subject(usuario.getUsername())
                .claim("roles", roles)
                .claim("permisos", permisos)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new TokenGenerado(token, expira, roles, permisos);
    }

    /** Resultado de la generación del token (valor + metadatos útiles para la respuesta). */
    public record TokenGenerado(String token, Instant expiraEn, List<String> roles, List<String> permisos) {
    }
}
