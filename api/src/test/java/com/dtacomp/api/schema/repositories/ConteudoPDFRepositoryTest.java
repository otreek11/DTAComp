package com.dtacomp.api.schema.repositories;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.ConteudoPDF;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.repositories.ConteudoPDFRepository;
import com.dtacomp.api.schema.repositories.ConteudoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

@DataJpaTest
public class ConteudoPDFRepositoryTest {
    @Autowired 
    private ConteudoPDFRepository conteudoPDFRepository;
    @Autowired 
    private ConteudoRepository conteudoRepository;

    @Test 
    public void deveCompartilharIdComConteudoAssociado() {
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Artigo em PDF").resumo("resumo").tipo(TipoConteudo.PDF).status(StatusConteudo.REVISAO).data(LocalDate.of(2026, 9, 16)).build());
        ConteudoPDF conteudoPDF = conteudoPDFRepository.save(ConteudoPDF.builder().conteudo(conteudo).urlArquivo("https://exemplo.com/arquivo.pdf").tamanhoBytes(2048f).build());
        Optional<ConteudoPDF> encontrado = conteudoPDFRepository.findById(conteudo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getIdConteudo()).isEqualTo(conteudo.getId());
        assertThat(encontrado.get().getConteudo().getId()).isEqualTo(conteudo.getId());
        assertThat(encontrado.get().getUrlArquivo()).isEqualTo("https://exemplo.com/arquivo.pdf");

    }
}
