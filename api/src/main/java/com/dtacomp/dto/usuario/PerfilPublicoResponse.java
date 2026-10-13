package com.dtacomp.dto.usuario;

import java.util.UUID;

import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.dto.usuario.UsuarioResponse.InstituicaoInfo;

public record PerfilPublicoResponse(
    UUID id,
    String nome,
    InstituicaoInfo instituicao
) {
    public static PerfilPublicoResponse from(Usuario usuario) {
        return new PerfilPublicoResponse(
                usuario.getId(),
                usuario.getNome(),
                InstituicaoInfo.from(usuario.getInstituicao()));
    }
}
