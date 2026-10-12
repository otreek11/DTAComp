package com.dtacomp.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.config.security.JwtTokenProvider;
import com.dtacomp.dto.auth.AuthResponse;
import com.dtacomp.dto.auth.LoginRequest;
import com.dtacomp.dto.auth.RegisterRequest;
import com.dtacomp.dto.error.ErrorResponse;
import com.dtacomp.service.InstituicaoService;
import com.dtacomp.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioService usuarioService;
    private final InstituicaoService instituicaoService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           UsuarioService usuarioService,
                           InstituicaoService instituicaoService,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioService = usuarioService;
        this.instituicaoService = instituicaoService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.senha()));

        Usuario usuario = (Usuario) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(usuario);

        return ResponseEntity.ok(new AuthResponse(
                token,
                usuario.getEmail(),
                usuario.getPerfil().getDescricao(),
                usuario.getId()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioService.emailExiste(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                    HttpStatus.CONFLICT.value(),
                    "Conflito",
                    "Email já cadastrado",
                    Instant.now().toString()
            ));
        }

        Instituicao instituicao = instituicaoService.buscarPorId(request.idInstituicao());

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(Usuario.TipoPerfil.MEMBRO)
                .instituicao(instituicao)
                .build();

        usuario = usuarioService.salvar(usuario);

        String token = jwtTokenProvider.generateToken(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                token,
                usuario.getEmail(),
                usuario.getPerfil().getDescricao(),
                usuario.getId()
        ));
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    public ResponseEntity<AuthResponse> me(org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new AuthResponse(
                null,
                usuario.getEmail(),
                usuario.getPerfil().getDescricao(),
                usuario.getId()
        ));
    }
}
