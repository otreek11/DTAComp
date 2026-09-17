package com.dtacomp.api.schema.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;

import java.time.LocalDate;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;


public class ConteudoTest {
    @Test 
    public void deveConstruirConteudoComBuilder() {
        Conteudo conteudo = Conteudo.builder().titulo("Teste").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build();
        assertEquals("Teste", conteudo.getTitulo());
        assertEquals("resumo", conteudo.getResumo());
        assertEquals("texto", conteudo.getTipo().getDescription());
        assertEquals("revisao", conteudo.getStatus().getDescription());
        assertEquals(LocalDate.of(2026,9,16), conteudo.getData());
    }

    @ParameterizedTest 
    @MethodSource("proverDadosTipoConteudo") 
    public void tipoConteudoDeveTerNumEDescricaoCorretos(int num_esperado, String description_esperada, TipoConteudo tipo_conteudo) {
        int num = tipo_conteudo.getNum();
        String descricao = tipo_conteudo.getDescription();

        assertThat(num).isEqualTo(num_esperado);
        assertThat(descricao).isEqualTo(description_esperada);        
    }

    private static Stream<Arguments> proverDadosTipoConteudo() {
        return Stream.of(
            Arguments.of(0, "texto", TipoConteudo.TEXTO),
            Arguments.of(1, "pdf", TipoConteudo.PDF)
        );
    }

    @ParameterizedTest 
    @MethodSource ("proverDadosStatusConteudo")
    public void statusConteudoDeveTerNumEDescricaoCorretos(int num_esperado, String description_esperada, StatusConteudo status_conteudo) {
        int num = status_conteudo.getNum();
        String descricao = status_conteudo.getDescription();

        assertThat(num).isEqualTo(num_esperado);
        assertThat(descricao).isEqualTo(description_esperada);        
    }

    private static Stream<Arguments> proverDadosStatusConteudo() {
        return Stream.of(
            Arguments.of(0, "revisao", StatusConteudo.REVISAO),
            Arguments.of(1, "devolvido", StatusConteudo.DEVOLVIDO),
            Arguments.of(2, "publicado", StatusConteudo.PUBLICADO)
        );
    }
}
