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

import com.dtacomp.schema.classes.ConteudoTexto;
import com.dtacomp.schema.repositories.ConteudoTextRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConteudoTextoServiceTest {

    @Mock
    private ConteudoTextRepository repository;

    @InjectMocks
    private ConteudoTextoService service;

    private UUID id;
    private ConteudoTexto conteudoTexto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        conteudoTexto = ConteudoTexto.builder()
                .idConteudo(id)
                .corpoHtml("<p>Texto inicial</p>")
                .build();
    }

    @Test
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(conteudoTexto));

        List<ConteudoTexto> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(conteudoTexto));

        ConteudoTexto resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getIdConteudo());
    }

    @Test
    void buscarPorId_NaoEncontrado() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(id));
    }

    @Test
    void salvar() {
        when(repository.save(conteudoTexto)).thenReturn(conteudoTexto);

        ConteudoTexto resultado = service.salvar(conteudoTexto);

        assertNotNull(resultado);
        verify(repository).save(conteudoTexto);
    }

    @Test
    void atualizar_Sucesso() {
        ConteudoTexto novosDados = ConteudoTexto.builder()
                .corpoHtml("<p>Texto Novo</p>")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(conteudoTexto));
        when(repository.save(conteudoTexto)).thenReturn(conteudoTexto);

        ConteudoTexto resultado = service.atualizar(id, novosDados);

        assertEquals("<p>Texto Novo</p>", resultado.getCorpoHtml());
        verify(repository).save(conteudoTexto);
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(conteudoTexto));

        service.remover(id);

        verify(repository).delete(conteudoTexto);
    }
}