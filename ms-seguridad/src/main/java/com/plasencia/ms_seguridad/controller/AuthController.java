package com.plasencia.ms_seguridad.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plasencia.ms_seguridad.dto.LoginRequest;
import com.plasencia.ms_seguridad.dto.LoginResponse;
import com.plasencia.ms_seguridad.entity.Usuario;
import com.plasencia.ms_seguridad.repository.UsuarioRepository;
import com.plasencia.ms_seguridad.service.TokenService;
import com.plasencia.ms_seguridad.service.TokenService.TokenGenerado;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Autenticaci&#243;n: valida credenciales y emite el JWT.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // Lanza AuthenticationException (401) si las credenciales no son v&#225;lidas
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        Usuario usuario = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow();

        TokenGenerado generado = tokenService.generar(usuario);
        return new LoginResponse(
                generado.token(),
                "Bearer",
                generado.expiraEn(),
                usuario.getUsername(),
                generado.roles(),
                generado.permisos());
    }
}
