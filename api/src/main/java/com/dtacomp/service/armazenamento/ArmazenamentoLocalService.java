package com.dtacomp.service.armazenamento;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ArmazenamentoLocalService implements ArmazenamentoService {

    private static final Pattern PASTA_OU_EXTENSAO = Pattern.compile("[a-z0-9]{1,10}");

    private final Path raiz;

    public ArmazenamentoLocalService(@Value("${app.storage.root:./storage}") String raiz) {
        this.raiz = Path.of(raiz).toAbsolutePath().normalize();
    }

    @Override
    public String salvar(String pasta, String extensao, InputStream conteudo) {
        String pastaNorm = pasta == null ? "" : pasta.toLowerCase(Locale.ROOT);
        String extNorm = extensao == null ? "" : extensao.toLowerCase(Locale.ROOT);
        if (!PASTA_OU_EXTENSAO.matcher(pastaNorm).matches()
                || !PASTA_OU_EXTENSAO.matcher(extNorm).matches()) {
            throw new IllegalStateException("Pasta ou extensão de armazenamento inválida");
        }

        String chave = pastaNorm + "/" + UUID.randomUUID() + "." + extNorm;
        Path destino = resolver(chave);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(conteudo, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar arquivo", e);
        }
        return chave;
    }

    @Override
    public Resource carregar(String chave) {
        Path caminho = resolverOuNull(chave);
        if (caminho == null || !Files.isRegularFile(caminho)) {
            throw new IllegalArgumentException("Arquivo não encontrado");
        }
        return new FileSystemResource(caminho);
    }

    @Override
    public boolean existe(String chave) {
        Path caminho = resolverOuNull(chave);
        return caminho != null && Files.isRegularFile(caminho);
    }

    @Override
    public void remover(String chave) {
        Path caminho = resolverOuNull(chave);
        if (caminho == null) {
            return;
        }
        try {
            Files.deleteIfExists(caminho);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao remover arquivo", e);
        }
    }

    private Path resolverOuNull(String chave) {
        if (chave == null || chave.isBlank()) {
            return null;
        }
        Path caminho = raiz.resolve(chave).normalize();
        return caminho.startsWith(raiz) && !caminho.equals(raiz) ? caminho : null;
    }

    private Path resolver(String chave) {
        Path caminho = resolverOuNull(chave);
        if (caminho == null) {
            throw new IllegalStateException("Chave de armazenamento inválida");
        }
        return caminho;
    }
}
