package com.dtacomp.dto.conteudo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.dtacomp.api.schema.classes.Conteudo;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConteudoDetalheResponse(
    UUID id,
    String titulo,
    String resumo,
    String tipo,
    String status,
    LocalDate data,
    List<String> tags,
    List<AutorResponse> autores,
    TextoBloco texto,
    PdfBloco pdf,
    List<ParecerResponse> revisoes
) {

    public record TextoBloco(String corpoHtml, List<ImagemResponse> imagens) {}

    /** {@code url} é o endpoint de download (respeita a visibilidade do conteúdo). */
    public record PdfBloco(float tamanhoBytes, String url) {}

    public static ConteudoDetalheResponse from(Conteudo conteudo,
                                               List<AutorResponse> autores,
                                               TextoBloco texto,
                                               PdfBloco pdf,
                                               List<ParecerResponse> revisoes) {
        return new ConteudoDetalheResponse(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getResumo(),
                conteudo.getTipo().getDescription(),
                conteudo.getStatus().getDescription(),
                conteudo.getData(),
                ConteudoResumoResponse.nomesDasTags(conteudo),
                autores,
                texto,
                pdf,
                revisoes
        );
    }
}
