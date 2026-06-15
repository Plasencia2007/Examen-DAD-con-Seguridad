package com.plasencia.ms_reserva.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte el JWT del ms-seguridad en authorities:
 * - claim "roles"   -> ROLE_<rol>
 * - claim "permisos" -> el permiso tal cual
 * Si un claim falta o no es lista, se ignora (lista vacía).
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Roles -> ROLE_<rol>
        for (String rol : leerLista(jwt, "roles")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));
        }
        // Permisos -> nombre tal cual
        for (String permiso : leerLista(jwt, "permisos")) {
            authorities.add(new SimpleGrantedAuthority(permiso));
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    /** Lee un claim como lista de String; tolera claim ausente o de otro tipo. */
    private List<String> leerLista(Jwt jwt, String claim) {
        Object valor = jwt.getClaim(claim);
        List<String> resultado = new ArrayList<>();
        if (valor instanceof List<?> lista) {
            for (Object elemento : lista) {
                if (elemento != null) {
                    resultado.add(elemento.toString());
                }
            }
        }
        return resultado;
    }
}
