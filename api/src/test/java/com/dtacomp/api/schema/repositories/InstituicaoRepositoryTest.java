package com.dtacomp.api.schema.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.dtacomp.api.schema.classes.Instituicao;

@DataJpaTest 
public class InstituicaoRepositoryTest {
    @Autowired 
    private InstituicaoRepository instituicaoRepository;

    @Test
    public void deveSalvarERecuperarInstituicao() {
        Instituicao salva = instituicaoRepository.save(Instituicao.builder().nome("Instituicao Teste").sigla("IT").build());

        Optional<Instituicao> encontrada = instituicaoRepository.findById(salva.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNome()).isEqualTo("Instituicao Teste");
        assertThat(encontrada.get().getSigla()).isEqualTo("IT");
    }
    
}
