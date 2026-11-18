package com.dtacomp.api.schema.repositories;

import java.time.LocalDate;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;

/**
 * Fixtures que já satisfazem as constraints NOT NULL do schema real (PostgreSQL/Flyway).
 * Os builders retornados podem ter campos sobrescritos em cada teste.
 */
final class RepositoryFixtures {

    private RepositoryFixtures() {
    }

    /** Usuário com senha e uma instituição recém-salva. */
    static Usuario.UsuarioBuilder usuario(InstituicaoRepository instituicaoRepository) {
        Instituicao instituicao = instituicaoRepository.save(
                Instituicao.builder().nome("Instituição de Teste").sigla("IT").build());
        return Usuario.builder().senhaHash("hash-de-teste").instituicao(instituicao);
    }

    /** Conteúdo em revisão com data preenchida. */
    static Conteudo.ConteudoBuilder conteudo(String titulo, TipoConteudo tipo) {
        return Conteudo.builder()
                .titulo(titulo)
                .tipo(tipo)
                .status(StatusConteudo.REVISAO)
                .data(LocalDate.of(2026, 9, 16));
    }
}
