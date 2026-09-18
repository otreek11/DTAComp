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

import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.api.schema.repositories.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService service;

    private UUID id;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        usuario = Usuario.builder()
                .id(id)
                .nome("Maria Silva")
                .email("maria@teste.com")
                .senhaHash("hash123")
                .perfil(TipoPerfil.MEMBRO)
                .idExterno("EXT-001")
                .build();
    }

    @Test
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(usuario));

        Usuario resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    void buscarPorId_NaoEncontrado() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(id));
    }

    @Test
    void buscarPorEmail_Sucesso() {
        when(repository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = service.buscarPorEmail("maria@teste.com");

        assertNotNull(resultado);
        assertEquals("maria@teste.com", resultado.getEmail());
    }

    @Test
    void buscarPorEmail_NaoEncontrado() {
        when(repository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorEmail("naoexiste@teste.com"));
    }

    @Test
    void emailExiste() {
        when(repository.existsByEmail("maria@teste.com")).thenReturn(true);
        when(repository.existsByEmail("outro@teste.com")).thenReturn(false);

        assertTrue(service.emailExiste("maria@teste.com"));
        assertFalse(service.emailExiste("outro@teste.com"));
        verify(repository).existsByEmail("maria@teste.com");
        verify(repository).existsByEmail("outro@teste.com");
    }

    @Test
    void salvar() {
        when(repository.save(usuario)).thenReturn(usuario);

        Usuario resultado = service.salvar(usuario);

        assertNotNull(resultado);
        verify(repository).save(usuario);
    }

    @Test
    void atualizar_Sucesso() {
        Usuario novosDados = Usuario.builder()
                .nome("Maria Silva Atualizada")
                .email("maria.nova@teste.com")
                .senhaHash("novaHash456")
                .perfil(TipoPerfil.ADMIN)
                .idExterno("EXT-002")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        Usuario resultado = service.atualizar(id, novosDados);

        assertEquals("Maria Silva Atualizada", resultado.getNome());
        assertEquals("maria.nova@teste.com", resultado.getEmail());
        assertEquals(TipoPerfil.ADMIN, resultado.getPerfil());
        verify(repository).save(usuario);
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(usuario));

        service.remover(id);

        verify(repository).delete(usuario);
    }

    @Test
    void buscarPorPerfil() {
        when(repository.findByPerfil(TipoPerfil.MEMBRO)).thenReturn(List.of(usuario));

        List<Usuario> resultado = service.buscarPorPerfil(TipoPerfil.MEMBRO);

        assertEquals(1, resultado.size());
        assertEquals(TipoPerfil.MEMBRO, resultado.get(0).getPerfil());
        verify(repository).findByPerfil(TipoPerfil.MEMBRO);
    }

    @Test
    void buscarPorInstituicao() {
        UUID idInstituicao = UUID.randomUUID();
        when(repository.findByInstituicao_Id(idInstituicao)).thenReturn(List.of(usuario));

        List<Usuario> resultado = service.buscarPorInstituicao(idInstituicao);

        assertEquals(1, resultado.size());
        verify(repository).findByInstituicao_Id(idInstituicao);
    }
}