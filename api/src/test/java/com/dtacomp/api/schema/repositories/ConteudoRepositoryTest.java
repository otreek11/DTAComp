package com.dtacomp.api.schema.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Tag;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.repositories.ConteudoRepository;
import com.dtacomp.api.schema.repositories.TagRepository;

import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
public class ConteudoRepositoryTest {
    @Autowired 
    private ConteudoRepository conteudoRepository;
    @Autowired
    private TagRepository tagRepository;

    @Test 
    public void deveEncontrarConteudoPorStatus() {
        conteudoRepository.save(Conteudo.builder().titulo("revisaoA").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        conteudoRepository.save(Conteudo.builder().titulo("revisaoB").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        conteudoRepository.save(Conteudo.builder().titulo("devolvido").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.DEVOLVIDO).data(LocalDate.of(2026,9,16)).build());
        
        List<Conteudo> emRevisao = conteudoRepository.findByStatus(StatusConteudo.REVISAO);

        assertThat(emRevisao).hasSize(2).extracting(Conteudo::getTitulo).contains("revisaoA");
    }

    @Test 
    public void deveEncontrarConteudoPorTipo() {
        conteudoRepository.save(Conteudo.builder().titulo("textoA").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        conteudoRepository.save(Conteudo.builder().titulo("textoB").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        conteudoRepository.save(Conteudo.builder().titulo("pdf").resumo("resumo").tipo(TipoConteudo.PDF).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        
        List<Conteudo> textos = conteudoRepository.findByTipo(TipoConteudo.TEXTO);

        assertThat(textos).hasSize(2).extracting(Conteudo::getTitulo).contains("textoA");
    }

    @Test 
    public void deveEncontrarConteudoPorTitulo() {
        Conteudo esperado = conteudoRepository.save(Conteudo.builder().titulo("Teste").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        
        List<Conteudo> encontrado = conteudoRepository.findByTituloContainingIgnoreCase("Teste");

        assertThat(encontrado).contains(esperado);
        assertThat(encontrado.get(0).getTitulo()).isEqualTo("Teste");
    }

    @Test 
    public void deveEncontrarConteudoPorTag() {
        Tag tag = tagRepository.save(Tag.builder().nome("java").build());
        conteudoRepository.save(Conteudo.builder().titulo("Artigo sobre Java").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026, 9, 16)).tags(Set.of(tag)).build());
        conteudoRepository.save(Conteudo.builder().titulo("Artigo sobre Python").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026, 9, 16)).build());

        List<Conteudo> resultado = conteudoRepository.findByTagNome("java");

        assertThat(resultado).hasSize(1).extracting(Conteudo::getTitulo).containsExactly("Artigo sobre Java");

    }
}
