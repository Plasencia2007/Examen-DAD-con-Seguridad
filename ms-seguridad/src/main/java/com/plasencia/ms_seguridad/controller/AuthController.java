package com.plasencia.ms_seguridad.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.plasencia.ms_seguridad.dto.LoginRequest;
import com.plasencia.ms_seguridad.dto.LoginResponse;
import com.plasencia.ms_seguridad.dto.UsuarioRequest;
import com.plasencia.ms_seguridad.dto.UsuarioResponse;
import com.plasencia.ms_seguridad.entity.Usuario;
import com.plasencia.ms_seguridad.repository.UsuarioRepository;
import com.plasencia.ms_seguridad.service.TokenService;
import com.plasencia.ms_seguridad.service.TokenService.TokenGenerado;
import com.plasencia.ms_seguridad.service.UsuarioService;

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
    private final UsuarioService usuarioService;

    /**
     * Registro P&#218;BLICO de usuarios (no requiere token). Permite indicar el rol
     * entre los existentes: USER, OPERADOR o incluso ADMIN.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse register(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crear(request);
    }

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
