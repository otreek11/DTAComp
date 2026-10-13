package com.dtacomp.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;


public record ConteudoFiltro(
    String q,
    String titulo,
    String resumo,
    List<String> tags,
    String autor,
    UUID idAutor,
    UUID excluirIdAutor,
    UUID idInstituicao,
    TipoConteudo tipo,
    StatusConteudo status,
    LocalDate de,
    LocalDate ate
) {}
