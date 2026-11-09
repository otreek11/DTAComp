package com.dtacomp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.repositories.InstituicaoRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final InstituicaoRepository instituicaoRepository;

    public DataSeeder(InstituicaoRepository instituicaoRepository) {
        this.instituicaoRepository = instituicaoRepository;
    }

    @Override
    public void run(String... args) {
        if (instituicaoRepository.count() == 0) {
            instituicaoRepository.save(Instituicao.builder()
                    .nome("Universidade Federal do Maranhão")
                    .sigla("UFMA")
                    .build());
            instituicaoRepository.save(Instituicao.builder()
                    .nome("Universidade Estadual do Maranhão")
                    .sigla("UEMA")
                    .build());
            instituicaoRepository.save(Instituicao.builder()
                    .nome("Instituto Federal do Maranhão")
                    .sigla("IFMA")
                    .build());
        }
    }
}
