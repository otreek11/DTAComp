package com.dtacomp.dto.usuario;

import java.util.UUID;

import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;

public record UsuarioResponse(
    UUID id,
    String nome,
    String email,
    String perfil,
    InstituicaoInfo instituicao
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().getDescricao(),
                InstituicaoInfo.from(usuario.getInstituicao()));
    }

    public record InstituicaoInfo(UUID id, String nome, String sigla) {
        public static InstituicaoInfo from(Instituicao instituicao) {
            if (instituicao == null) {
                return null;
            }
            return new InstituicaoInfo(instituicao.getId(), instituicao.getNome(), instituicao.getSigla());
        }
    }
}
