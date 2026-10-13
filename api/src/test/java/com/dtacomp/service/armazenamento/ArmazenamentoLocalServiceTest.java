package com.dtacomp.service.armazenamento;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.*;

class ArmazenamentoLocalServiceTest {

    @TempDir
    Path raiz;

    private ArmazenamentoLocalService service;

    @BeforeEach
    void setUp() {
        service = new ArmazenamentoLocalService(raiz.toString());
    }

    @Test
    void salvarGeraChaveRelativa() throws Exception {
        String chave = service.salvar("pdf", "PDF", new ByteArrayInputStream("%PDF-1.4".getBytes()));

        assertTrue(chave.matches("pdf/[0-9a-f-]{36}\\.pdf"), chave);
        assertTrue(Files.isRegularFile(raiz.resolve(chave)));
        assertTrue(service.existe(chave));
    }

    @Test
    void carregarDevolveConteudo() throws Exception {
        String chave = service.salvar("imagens", "png", new ByteArrayInputStream(new byte[] {1, 2, 3}));

        Resource recurso = service.carregar(chave);

        assertArrayEquals(new byte[] {1, 2, 3}, recurso.getInputStream().readAllBytes());
    }

    @Test
    void carregarInexistente() {
        assertThrows(IllegalArgumentException.class, () -> service.carregar("pdf/nao-existe.pdf"));
    }

    @Test
    void removerEIdempotente() {
        String chave = service.salvar("pdf", "pdf", new ByteArrayInputStream(new byte[] {1}));

        service.remover(chave);
        assertFalse(service.existe(chave));
        assertDoesNotThrow(() -> service.remover(chave));
    }

    @Test
    void rejeitaPathTraversal() throws Exception {
        Path fora = Files.createTempFile("fora-da-raiz", ".txt");
        try {
            String relativa = raiz.relativize(fora).toString();

            assertFalse(service.existe(relativa));
            assertFalse(service.existe("../segredo.txt"));
            assertFalse(service.existe(fora.toAbsolutePath().toString()));
            assertThrows(IllegalArgumentException.class, () -> service.carregar(relativa));

            service.remover(relativa);
            assertTrue(Files.exists(fora), "arquivo fora da raiz não pode ser removido");
        } finally {
            Files.deleteIfExists(fora);
        }
    }

    @Test
    void rejeitaPastaOuExtensaoInvalida() {
        assertThrows(IllegalStateException.class,
                () -> service.salvar("../pdf", "pdf", new ByteArrayInputStream(new byte[0])));
        assertThrows(IllegalStateException.class,
                () -> service.salvar("pdf", "p/df", new ByteArrayInputStream(new byte[0])));
    }
}
