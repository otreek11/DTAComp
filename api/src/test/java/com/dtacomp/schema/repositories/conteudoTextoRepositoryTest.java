package com.dtacomp.schema.repositories;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dtacomp.schema.classes.Conteudo;
import com.dtacomp.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.schema.classes.ConteudoTexto;
import com.dtacomp.schema.classes.Conteudo.StatusConteudo;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

@DataJpaTest
public class conteudoTextoRepositoryTest {
    @Autowired 
    private ConteudoTextRepository conteudoTextoRepository;
    @Autowired 
    private ConteudoRepository conteudoRepository;

    @Test 
    public void deveCompartilharIdComConteudoAssociado() {
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Artigo em Texto").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026, 9, 16)).build());
        ConteudoTexto conteudoTexto = conteudoTextoRepository.save(ConteudoTexto.builder().conteudo(conteudo).corpoHtml("Lorem Ipsum Dolor").build());
        Optional<ConteudoTexto> encontrado = conteudoTextoRepository.findById(conteudo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getIdConteudo()).isEqualTo(conteudo.getId());
        assertThat(encontrado.get().getConteudo().getId()).isEqualTo(conteudo.getId());
        assertThat(encontrado.get().getCorpoHtml()).isEqualTo("Lorem Ipsum Dolor");
    }

}