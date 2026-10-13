package com.dtacomp.dto.conteudo;

import java.util.UUID;

import com.dtacomp.api.schema.classes.Usuario;

public record AutorResponse(
    UUID id,
    String nome,
    String siglaInstituicao
) {
    public static AutorResponse from(Usuario usuario) {
        String sigla = usuario.getInstituicao() != null ? usuario.getInstituicao().getSigla() : null;
        return new AutorResponse(usuario.getId(), usuario.getNome(), sigla);
    }
}
