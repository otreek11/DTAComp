package com.dtacomp.dto.auth;

import java.util.UUID;

import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.dto.usuario.UsuarioResponse.InstituicaoInfo;

public record AuthResponse(
    String token,
    String email,
    String perfil,
    UUID id,
    String nome,
    InstituicaoInfo instituicao
) {
    public static AuthResponse from(Usuario usuario, String token) {
        return new AuthResponse(
                token,
                usuario.getEmail(),
                usuario.getPerfil().getDescricao(),
                usuario.getId(),
                usuario.getNome(),
                InstituicaoInfo.from(usuario.getInstituicao()));
    }
}
