package com.dtacomp.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtacomp.api.schema.classes.ConteudoPDF;
import com.dtacomp.api.schema.repositories.ConteudoPDFRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConteudoPDFServiceTest {

    @Mock
    private ConteudoPDFRepository repository;

    @InjectMocks
    private ConteudoPDFService service;

    private UUID id;
    private ConteudoPDF conteudo;

    @BeforeEach
    void setUp(){
        id = UUID.randomUUID();
        conteudo = ConteudoPDF.builder()
                .idConteudo(id)
                .urlArquivo("https://exemplo.com/arquivo.pdf")
                .tamanhoBytes(1024f)
                .build();
    }


    @Test
    void listarTodos() {

        when(repository.findAll()).thenReturn(List.of(conteudo));

        List<ConteudoPDF> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();

    }

    @Test
    void buscarPorId_sucesso() {

        when(repository.findById(id)).thenReturn(Optional.of(conteudo));

        ConteudoPDF resultado = service.buscarPorId(id);

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
        when(repository.save(conteudo)).thenReturn(conteudo);

        ConteudoPDF resultado = service.salvar(conteudo);

        assertNotNull(resultado);
        verify(repository).save(conteudo);
    }

    @Test
    void atualizar_Sucesso() {

        String url = "https://novo.com/arquivo.pdf";

        ConteudoPDF dadosNovos = ConteudoPDF.builder()
                .urlArquivo(url)
                .tamanhoBytes(2048f)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(conteudo));
        when(repository.save(conteudo)).thenReturn(conteudo);

        ConteudoPDF resultado = service.atualizar(id, dadosNovos);

        assertEquals("https://novo.com/arquivo.pdf", resultado.getUrlArquivo());
        assertEquals(2048f, resultado.getTamanhoBytes());
        verify(repository).save(conteudo);
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(conteudo));

        service.remover(id);

        verify(repository).delete(conteudo);
    }
}