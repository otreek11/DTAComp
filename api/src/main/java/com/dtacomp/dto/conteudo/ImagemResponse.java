package com.dtacomp.dto.conteudo;

import com.dtacomp.api.schema.classes.Imagem;

public record ImagemResponse(
    String url,
    String legenda
) {
    public static final String PREFIXO_URL = "/arquivos/"; // TODO: colocar isso no application.properties (app.arquivos.prefixo-url)

    public static ImagemResponse from(Imagem imagem) {
        return new ImagemResponse(PREFIXO_URL + imagem.getUrlImage(), imagem.getLegenda());
    }
}
