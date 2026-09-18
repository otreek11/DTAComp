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

import com.dtacomp.schema.classes.Imagem;
import com.dtacomp.schema.repositories.ImagemRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImagemServiceTest {

    @Mock
    private ImagemRepository repository;

    @InjectMocks
    private ImagemService service;

    private UUID id;
    private Imagem imagem;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        imagem = Imagem.builder()
                .id(id)
                .urlImage("https://exemplo.com/imagem.png")
                .legenda("Legenda inicial")
                .build();
    }

    @Test
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(imagem));

        List<Imagem> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(imagem));

        Imagem resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    void buscarPorId_NaoEncontrado() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(id));
    }

    @Test
    void buscarPorConteudoTexto() {
        UUID idConteudoTexto = UUID.randomUUID();
        when(repository.findByConteudoTexto_IdConteudo(idConteudoTexto)).thenReturn(List.of(imagem));

        List<Imagem> resultado = service.buscarPorConteudoTexto(idConteudoTexto);

        assertEquals(1, resultado.size());
        verify(repository).findByConteudoTexto_IdConteudo(idConteudoTexto);
    }

    @Test
    void salvar() {
        when(repository.save(imagem)).thenReturn(imagem);

        Imagem resultado = service.salvar(imagem);

        assertNotNull(resultado);
        verify(repository).save(imagem);
    }

    @Test
    void atualizar_Sucesso() {
        Imagem novosDados = Imagem.builder()
                .urlImage("https://exemplo.com/imagem_nova.png")
                .legenda("Nova Legenda")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(imagem));
        when(repository.save(imagem)).thenReturn(imagem);

        Imagem resultado = service.atualizar(id, novosDados);

        assertEquals("https://exemplo.com/imagem_nova.png", resultado.getUrlImage());
        assertEquals("Nova Legenda", resultado.getLegenda());
        verify(repository).save(imagem);
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(imagem));

        service.remover(id);

        verify(repository).delete(imagem);
    }
}