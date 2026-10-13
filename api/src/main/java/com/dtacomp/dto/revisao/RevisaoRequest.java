package com.dtacomp.dto.revisao;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RevisaoRequest(
        @NotNull(message = "Campo 'idConteudo' é obrigatório")
        UUID idConteudo,

        @NotNull(message = "Campo 'aprovado' é obrigatório")
        Boolean aprovado,

        String parecer
) {
    @AssertTrue(message = "Parecer obrigatório ao devolver conteúdo")
    public boolean isParecerValido() {
        if (Boolean.FALSE.equals(aprovado)) {
            return parecer != null && !parecer.trim().isEmpty();
        }
        return true;
    }
}