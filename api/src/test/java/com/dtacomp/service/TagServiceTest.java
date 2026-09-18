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

import com.dtacomp.api.schema.classes.Tag;
import com.dtacomp.api.schema.repositories.TagRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository repository;

    @InjectMocks
    private TagService service;

    private UUID id;
    private Tag tag;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        tag = Tag.builder()
                .id(id)
                .nome("Java")
                .build();
    }

    @Test
    void listarTodos() {
        when(repository.findAll()).thenReturn(List.of(tag));

        List<Tag> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void buscarPorId_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(tag));

        Tag resultado = service.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    void buscarPorId_NaoEncontrado() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorId(id));
    }

    @Test
    void buscarPorNome_Sucesso() {
        when(repository.findByNome("Java")).thenReturn(Optional.of(tag));

        Tag resultado = service.buscarPorNome("Java");

        assertNotNull(resultado);
        assertEquals("Java", resultado.getNome());
    }

    @Test
    void buscarPorNome_NaoEncontrado() {
        when(repository.findByNome("Inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.buscarPorNome("Inexistente"));
    }

    @Test
    void nomeExiste() {
        when(repository.existsByNome("Java")).thenReturn(true);
        when(repository.existsByNome("Python")).thenReturn(false);

        assertTrue(service.nomeExiste("Java"));
        assertFalse(service.nomeExiste("Python"));
        verify(repository).existsByNome("Java");
        verify(repository).existsByNome("Python");
    }

    @Test
    void salvar() {
        when(repository.save(tag)).thenReturn(tag);

        Tag resultado = service.salvar(tag);

        assertNotNull(resultado);
        verify(repository).save(tag);
    }

    @Test
    void atualizar_Sucesso() {
        Tag novosDados = Tag.builder()
                .nome("Spring Boot")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(tag));
        when(repository.save(tag)).thenReturn(tag);

        Tag resultado = service.atualizar(id, novosDados);

        assertEquals("Spring Boot", resultado.getNome());
        verify(repository).save(tag);
    }

    @Test
    void remover_Sucesso() {
        when(repository.findById(id)).thenReturn(Optional.of(tag));

        service.remover(id);

        verify(repository).delete(tag);
    }
}