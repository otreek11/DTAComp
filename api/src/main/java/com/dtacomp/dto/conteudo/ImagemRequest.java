package com.dtacomp.dto.conteudo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record ImagemRequest(
    @NotBlank(message = "URL da imagem é obrigatória")
    @Size(max = 500, message = "URL da imagem deve ter no máximo 500 caracteres")
    String url,

    @Size(max = 255, message = "Legenda deve ter no máximo 255 caracteres")
    String legenda
) {}
