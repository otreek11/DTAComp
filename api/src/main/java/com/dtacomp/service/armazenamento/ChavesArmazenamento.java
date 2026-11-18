package com.dtacomp.service.armazenamento;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Regras de formação e validação de chaves de armazenamento ("pasta/uuid.ext"),
 * compartilhadas entre o armazenamento local e o S3.
 */
final class ChavesArmazenamento {

    private static final Pattern PASTA_OU_EXTENSAO = Pattern.compile("[a-z0-9]{1,10}");

    private ChavesArmazenamento() {
    }

    /** Gera uma chave nova e única; rejeita pasta/extensão fora do padrão. */
    static String gerar(String pasta, String extensao) {
        String pastaNorm = pasta == null ? "" : pasta.toLowerCase(Locale.ROOT);
        String extNorm = extensao == null ? "" : extensao.toLowerCase(Locale.ROOT);
        if (!PASTA_OU_EXTENSAO.matcher(pastaNorm).matches()
                || !PASTA_OU_EXTENSAO.matcher(extNorm).matches()) {
            throw new IllegalStateException("Pasta ou extensão de armazenamento inválida");
        }
        return pastaNorm + "/" + UUID.randomUUID() + "." + extNorm;
    }

    /** Chave relativa e sem travessia de diretório (sem "..", "\" ou "/" inicial). */
    static boolean valida(String chave) {
        return chave != null
                && !chave.isBlank()
                && !chave.startsWith("/")
                && !chave.contains("\\")
                && !chave.contains("..");
    }
}
