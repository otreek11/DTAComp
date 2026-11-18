package com.dtacomp.dto.revisao;

import java.time.LocalDate;
import java.util.UUID;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Revisao;

/** Revisão registrada pelo revisor autenticado (aba "Concluídas"). */
public record RevisaoResumoResponse(
    UUID id,
    UUID idConteudo,
    String tituloConteudo,
    String tipoConteudo,
    String status,
    String parecer,
    LocalDate data
) {
    public static RevisaoResumoResponse from(Revisao revisao) {
        Conteudo conteudo = revisao.getConteudo();
        return new RevisaoResumoResponse(
                revisao.getId(),
                conteudo == null ? null : conteudo.getId(),
                conteudo == null ? null : conteudo.getTitulo(),
                conteudo == null || conteudo.getTipo() == null ? null : conteudo.getTipo().getDescription(),
                revisao.getStatus() == null ? null : revisao.getStatus().getDescription(),
                revisao.getParecerHumano(),
                revisao.getDataRevisao());
    }
}
