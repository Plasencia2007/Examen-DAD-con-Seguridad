package com.plasencia.ms_seguridad.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Traduce los claims del JWT a authorities de Spring Security:
 * <ul>
 *   <li>cada valor del claim {@code roles} se expone como {@code ROLE_<rol>} (para hasRole)</li>
 *   <li>cada valor del claim {@code permisos} se expone tal cual (para hasAuthority)</li>
 * </ul>
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        for (String rol : leerListaClaim(jwt, "roles")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));
        }
        for (String permiso : leerListaClaim(jwt, "permisos")) {
            authorities.add(new SimpleGrantedAuthority(permiso));
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    @SuppressWarnings("unchecked")
    private List<String> leerListaClaim(Jwt jwt, String claim) {
        Object valor = jwt.getClaim(claim);
        if (valor instanceof List<?> lista) {
            return lista.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
