package com.dtacomp.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.repositories.InstituicaoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstituicaoServiceTest {

    @Mock
    private InstituicaoRepository repository;

    @InjectMocks
    private InstituicaoService service;

    private UUID id;
    private Instituicao instituicao;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        instituicao = Instituicao.builder()
                .id(id)
                .nome("Universidade Federal do Maranhão")
                .sigla("UFMA")
                .build();
    }

    @Test
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(instituicao));

        List<Instituicao> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(instituicao));

        Instituicao resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    void buscarPorId_NaoEncontrado() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(id));
    }

    @Test
    void salvar() {
        when(repository.save(instituicao)).thenReturn(instituicao);

        Instituicao resultado = service.salvar(instituicao);

        assertNotNull(resultado);
        verify(repository).save(instituicao);
    }

    @Test
    void atualizar_Sucesso() {
        Instituicao novosDados = Instituicao.builder()
                .nome("Instituto de Tecnologia de Aeronáutica")
                .sigla("ITA")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(instituicao));
        when(repository.save(instituicao)).thenReturn(instituicao);

        Instituicao resultado = service.atualizar(id, novosDados);

        assertEquals("Instituto de Tecnologia de Aeronáutica", resultado.getNome());
        assertEquals("ITA", resultado.getSigla());
        verify(repository).save(instituicao);
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(instituicao));

        service.remover(id);

        verify(repository).delete(instituicao);
    }
}