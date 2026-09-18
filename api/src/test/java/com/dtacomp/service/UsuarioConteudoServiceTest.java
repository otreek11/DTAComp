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

import com.dtacomp.schema.classes.Conteudo;
import com.dtacomp.schema.classes.UsuarioConteudo;
import com.dtacomp.schema.classes.UsuarioConteudo.UsuarioConteudoPK;
import com.dtacomp.schema.repositories.UsuarioConteudoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioConteudoServiceTest {

    @Mock
    private UsuarioConteudoRepository repository;

    @InjectMocks
    private UsuarioConteudoService service;

    private UsuarioConteudoPK pk;
    private UsuarioConteudo usuarioConteudo;

    @BeforeEach
    void setUp() {
        pk = new UsuarioConteudoPK();
        pk.setIdUsuario(UUID.randomUUID());
        pk.setIdConteudo(UUID.randomUUID());

        usuarioConteudo = UsuarioConteudo.builder()
                .pk(pk)
                .idxPosicao(1)
                .build();
    }

    @Test
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(usuarioConteudo));

        List<UsuarioConteudo> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_Sucesso() {
        when(repository.findById(pk)).thenReturn(Optional.of(usuarioConteudo));

        UsuarioConteudo resultado = service.buscarPorId(pk);

        assertNotNull(resultado);
        assertEquals(pk, resultado.getPk());
    }

    @Test
    void buscarPorId_NaoEncontrado() {
        when(repository.findById(pk)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(pk));
    }

    @Test
    void buscarAutores() {
        UUID idConteudo = pk.getIdConteudo();
        when(repository.findByConteudo_IdOrderByIdxPosicaoAsc(idConteudo)).thenReturn(List.of(usuarioConteudo));

        List<UsuarioConteudo> resultado = service.buscarAutores(idConteudo);

        assertEquals(1, resultado.size());
        verify(repository).findByConteudo_IdOrderByIdxPosicaoAsc(idConteudo);
    }

    @Test
    void buscarConteudosPorAutor() {
        UUID idUsuario = pk.getIdUsuario();
        Conteudo conteudo = Conteudo.builder().id(pk.getIdConteudo()).titulo("Artigo").build();
        when(repository.findConteudosPorAutor(idUsuario)).thenReturn(List.of(conteudo));

        List<Conteudo> resultado = service.buscarConteudosPorAutor(idUsuario);

        assertEquals(1, resultado.size());
        assertEquals("Artigo", resultado.get(0).getTitulo());
        verify(repository).findConteudosPorAutor(idUsuario);
    }

    @Test
    void salvar() {
        when(repository.save(usuarioConteudo)).thenReturn(usuarioConteudo);

        UsuarioConteudo resultado = service.salvar(usuarioConteudo);

        assertNotNull(resultado);
        verify(repository).save(usuarioConteudo);
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(pk)).thenReturn(Optional.of(usuarioConteudo));

        service.remover(pk);

        verify(repository).delete(usuarioConteudo);
    }
}