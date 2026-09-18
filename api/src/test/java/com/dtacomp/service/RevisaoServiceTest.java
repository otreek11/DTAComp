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

import com.dtacomp.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.schema.classes.Revisao;
import com.dtacomp.schema.repositories.RevisaoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevisaoServiceTest {

    @Mock
    private RevisaoRepository repository;

    @InjectMocks
    private RevisaoService service;

    private UUID id;
    private Revisao revisao;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        revisao = Revisao.builder()
                .id(id)
                .status(StatusConteudo.REVISAO)
                .parecerHumano("Texto revisado sem inconformidades.")
                .build();
    }

    @Test
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(revisao));

        List<Revisao> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(revisao));

        Revisao resultado = service.buscarPorId(id);

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
        when(repository.save(revisao)).thenReturn(revisao);

        Revisao resultado = service.salvar(revisao);

        assertNotNull(resultado);
        verify(repository).save(revisao);
    }

    @Test
    void atualizar_Sucesso() {
        Revisao novosDados = Revisao.builder()
                .status(StatusConteudo.PUBLICADO)
                .parecerHumano("Aprovado para publicação.")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(revisao));
        when(repository.save(revisao)).thenReturn(revisao);

        Revisao resultado = service.atualizar(id, novosDados);

        assertEquals(StatusConteudo.PUBLICADO, resultado.getStatus());
        assertEquals("Aprovado para publicação.", resultado.getParecerHumano());
        verify(repository).save(revisao);
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(revisao));

        service.remover(id);

        verify(repository).delete(revisao);
    }

    @Test
    void buscarPorRevisor() {
        UUID idRevisor = UUID.randomUUID();
        when(repository.findByRevisor_Id(idRevisor)).thenReturn(List.of(revisao));

        List<Revisao> resultado = service.buscarPorRevisor(idRevisor);

        assertEquals(1, resultado.size());
        verify(repository).findByRevisor_Id(idRevisor);
    }

    @Test
    void buscarPorConteudo() {
        UUID idConteudo = UUID.randomUUID();
        when(repository.findByConteudo_Id(idConteudo)).thenReturn(List.of(revisao));

        List<Revisao> resultado = service.buscarPorConteudo(idConteudo);

        assertEquals(1, resultado.size());
        verify(repository).findByConteudo_Id(idConteudo);
    }

    @Test
    void buscarPorRevisorEStatus() {
        UUID idRevisor = UUID.randomUUID();
        when(repository.findByRevisor_IdAndStatus(idRevisor, StatusConteudo.REVISAO)).thenReturn(List.of(revisao));

        List<Revisao> resultado = service.buscarPorRevisorEStatus(idRevisor, StatusConteudo.REVISAO);

        assertEquals(1, resultado.size());
        assertEquals(StatusConteudo.REVISAO, resultado.get(0).getStatus());
        verify(repository).findByRevisor_IdAndStatus(idRevisor, StatusConteudo.REVISAO);
    }
}