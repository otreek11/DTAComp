package com.dtacomp.dto.usuario;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Revisao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.dto.usuario.UsuarioResponse.InstituicaoInfo;

public record MeusDadosResponse(
    Conta conta,
    List<ConteudoExportado> conteudos,
    List<RevisaoExportada> revisoes,
    Instant geradoEm
) {

    public record Conta(
        UUID id,
        String nome,
        String email,
        String perfil,
        InstituicaoInfo instituicao,
        String idExterno
    ) {
        public static Conta from(Usuario usuario) {
            return new Conta(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getPerfil().getDescricao(),
                    InstituicaoInfo.from(usuario.getInstituicao()),
                    usuario.getIdExterno());
        }
    }

    public record ConteudoExportado(
        UUID id,
        String titulo,
        String tipo,
        String status,
        LocalDate data
    ) {
        public static ConteudoExportado from(Conteudo conteudo) {
            return new ConteudoExportado(
                    conteudo.getId(),
                    conteudo.getTitulo(),
                    conteudo.getTipo() == null ? null : conteudo.getTipo().getDescription(),
                    conteudo.getStatus() == null ? null : conteudo.getStatus().getDescription(),
                    conteudo.getData());
        }
    }

    public record RevisaoExportada(
        UUID id,
        UUID idConteudo,
        String tituloConteudo,
        String status,
        String parecer,
        LocalDate data
    ) {
        public static RevisaoExportada from(Revisao revisao) {
            Conteudo conteudo = revisao.getConteudo();
            return new RevisaoExportada(
                    revisao.getId(),
                    conteudo == null ? null : conteudo.getId(),
                    conteudo == null ? null : conteudo.getTitulo(),
                    revisao.getStatus() == null ? null : revisao.getStatus().getDescription(),
                    revisao.getParecerHumano(),
                    revisao.getDataRevisao());
        }
    }
}
