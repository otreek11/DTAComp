package com.dtacomp.service.armazenamento;

import java.io.InputStream;

import org.springframework.core.io.Resource;


public interface ArmazenamentoService {
    String salvar(String pasta, String extensao, InputStream conteudo);

    Resource carregar(String chave);

    boolean existe(String chave);

    void remover(String chave);
}
