package com.dtacomp.api.schema.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class InstituicaoTest {
    @Test 
    public void deveConstruirInstituicaoComBuilder() {
        Instituicao instituicao = Instituicao.builder().nome("Instituto Teste").sigla("IT").build();
        assertEquals("Instituto Teste", instituicao.getNome());
        assertEquals("IT", instituicao.getSigla());
    }
}
