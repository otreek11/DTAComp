package com.dtacomp.service;

import java.time.LocalDate;
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

import com.dtacomp.schema.classes.Conteudo;
import com.dtacomp.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.schema.repositories.ConteudoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConteudoServiceTest {

    @Mock
    private ConteudoRepository repository;

    @InjectMocks
    private ConteudoService service;

    private UUID id;
    private Conteudo conteudo;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        conteudo = Conteudo.builder()
                .id(id)
                .titulo("Introdução à Computação")
                .resumo("Resumo sobre fundamentos")
                .tipo(TipoConteudo.TEXTO)
                .status(StatusConteudo.REVISAO)
                .data(LocalDate.now())
                .idExterno("EXT-123")
                .build();
    }

    @Test
    @DisplayName("Deve retornar todos os conteúdos cadastrados")
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(conteudo));

        List<Conteudo> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Introdução à Computação", resultado.get(0).getTitulo());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve retornar o Conteúdo quando o ID existir")
    void buscarPorId_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(conteudo));

        Conteudo resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o ID não for encontrado")
    void buscarPorId_NaoEncontrado() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(id));
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve salvar e retornar o conteúdo")
    void salvar() {
        when(repository.save(conteudo)).thenReturn(conteudo);

        Conteudo resultado = service.salvar(conteudo);

        assertNotNull(resultado);
        assertEquals("Introdução à Computação", resultado.getTitulo());
        verify(repository).save(conteudo);
    }

    @Test
    @DisplayName("Deve atualizar os campos do conteúdo existente com sucesso")
    void atualizar_Sucesso() {
        Conteudo novosDados = Conteudo.builder()
                .titulo("Novo Titulo")
                .resumo("Novo resumo")
                .tipo(TipoConteudo.PDF)
                .status(StatusConteudo.PUBLICADO)
                .data(LocalDate.of(2026, 12, 1))
                .idExterno("EXT-999")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(conteudo));
        when(repository.save(conteudo)).thenReturn(conteudo);

        Conteudo resultado = service.atualizar(id, novosDados);

        assertEquals("Novo Titulo", resultado.getTitulo());
        assertEquals("Novo resumo", resultado.getResumo());
        assertEquals(TipoConteudo.PDF, resultado.getTipo());
        assertEquals(StatusConteudo.PUBLICADO, resultado.getStatus());
        assertEquals("EXT-999", resultado.getIdExterno());
        verify(repository).save(conteudo);
    }

    @Test
    @DisplayName("Deve falhar ao tentar atualizar conteúdo inexistente")
    void atualizar_NaoEncontrado() {
        Conteudo novosDados = Conteudo.builder().build();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(id, novosDados));
        verify(repository, never()).save(any());
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(conteudo));

        service.remover(id);

        verify(repository).delete(conteudo);
    }

    @Test
    void remover_NaoEncontrado() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.remover(id));
        verify(repository, never()).delete(any());
    }

    @Test
    void buscarPorTitulo() {
        when(repository.findByTituloContainingIgnoreCase("computação")).thenReturn(List.of(conteudo));

        List<Conteudo> resultado = service.buscarPorTitulo("computação");

        assertEquals(1, resultado.size());
        verify(repository).findByTituloContainingIgnoreCase("computação");
    }

    @Test
    void buscarPorStatus() {
        when(repository.findByStatus(StatusConteudo.REVISAO)).thenReturn(List.of(conteudo));

        List<Conteudo> resultado = service.buscarPorStatus(StatusConteudo.REVISAO);

        assertEquals(1, resultado.size());
        assertEquals(StatusConteudo.REVISAO, resultado.get(0).getStatus());
        verify(repository).findByStatus(StatusConteudo.REVISAO);
    }

    @Test
    void buscarPorTipo() {
        when(repository.findByTipo(TipoConteudo.TEXTO)).thenReturn(List.of(conteudo));

        List<Conteudo> resultado = service.buscarPorTipo(TipoConteudo.TEXTO);

        assertEquals(1, resultado.size());
        assertEquals(TipoConteudo.TEXTO, resultado.get(0).getTipo());
        verify(repository).findByTipo(TipoConteudo.TEXTO);
    }

    @Test
    void buscarPorTag() {
        when(repository.findByTagNome("Java")).thenReturn(List.of(conteudo));

        List<Conteudo> resultado = service.buscarPorTag("Java");

        assertEquals(1, resultado.size());
        verify(repository).findByTagNome("Java");
    }
}