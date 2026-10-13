package com.dtacomp.dto.conteudo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Tag;

public record ConteudoResumoResponse(
    UUID id,
    String titulo,
    String resumo,
    String tipo,
    String status,
    LocalDate data,
    List<String> tags,
    List<AutorResponse> autores
) {
    public static ConteudoResumoResponse from(Conteudo conteudo, List<AutorResponse> autores) {
        return new ConteudoResumoResponse(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getResumo(),
                conteudo.getTipo().getDescription(),
                conteudo.getStatus().getDescription(),
                conteudo.getData(),
                nomesDasTags(conteudo),
                autores
        );
    }

    static List<String> nomesDasTags(Conteudo conteudo) {
        if (conteudo.getTags() == null) {
            return List.of();
        }
        return conteudo.getTags().stream()
                .map(Tag::getNome)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
