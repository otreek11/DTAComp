package com.dtacomp.dto.conteudo;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;


public record ConteudoTextoRequest(
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    String titulo,

    @Size(max = 5000, message = "Resumo deve ter no máximo 5000 caracteres")
    String resumo,

    @NotBlank(message = "Corpo do texto é obrigatório")
    String corpoHtml,

    @Size(max = 20, message = "No máximo 20 tags")
    List<@NotBlank(message = "Tag não pode ser vazia") String> tags,

    @NotEmpty(message = "Informe ao menos um autor")
    @Size(max = 30, message = "No máximo 30 autores")
    List<@NotBlank(message = "Email de autor é obrigatório")
         @Email(message = "Email de autor deve ser válido") String> autores,

    @Size(max = 100, message = "No máximo 100 imagens")
    List<@Valid ImagemRequest> imagens
) {}
