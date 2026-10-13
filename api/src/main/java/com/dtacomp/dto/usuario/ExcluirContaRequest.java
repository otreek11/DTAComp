package com.dtacomp.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record ExcluirContaRequest(
    @NotBlank(message = "Senha é obrigatória para confirmar a exclusão")
    String senha
) {}
