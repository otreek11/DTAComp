package com.dtacomp.dto.conteudo;

import java.util.UUID;

import com.dtacomp.api.schema.classes.Revisao;

public record ParecerResponse(
    UUID id,
    String status,
    String parecer
) {
    public static ParecerResponse from(Revisao revisao) {
        return new ParecerResponse(
                revisao.getId(),
                revisao.getStatus() != null ? revisao.getStatus().getDescription() : null,
                revisao.getParecerHumano()
        );
    }
}
