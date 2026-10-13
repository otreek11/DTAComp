package com.dtacomp.controller;

import java.util.UUID;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

final class TestUsuarios {

    private TestUsuarios() {}

    static Usuario usuario(TipoPerfil perfil) {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Fulano")
                .email(perfil.name().toLowerCase() + "@teste.com")
                .senhaHash("x")
                .perfil(perfil)
                .build();
    }

    static RequestPostProcessor como(Usuario usuario) {
        return user(usuario);
    }

    static RequestPostProcessor como(TipoPerfil perfil) {
        return user(usuario(perfil));
    }
}
