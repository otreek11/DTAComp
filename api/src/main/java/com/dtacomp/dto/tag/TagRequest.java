package com.dtacomp.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    @Pattern(regexp = "[^,\\p{Cntrl}]+",
             message = "Nome não pode conter vírgula nem caracteres de controle")
    String nome
) {}
