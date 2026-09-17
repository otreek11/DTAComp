package com.dtacomp.api.schema.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dtacomp.api.schema.classes.Tag;

@DataJpaTest 
public class TagRepositoryTest {
    @Autowired 
    private TagRepository tagRepository;
    
    @Test 
    public void deveEncontrarTagPorNome() {
        Tag tag = tagRepository.save(Tag.builder().nome("Teste").build());

        Optional<Tag> encontrada = tagRepository.findByNome("Teste");

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNome()).isEqualTo("Teste");
    }

    @Test 
    public void verificaExistenciaPorNome() {
        tagRepository.save(Tag.builder().nome("Teste").build());

        Boolean existe = tagRepository.existsByNome("Teste");

        assertThat(existe).isTrue();
    }
}
