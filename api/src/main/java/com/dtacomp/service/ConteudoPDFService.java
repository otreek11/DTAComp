package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.ConteudoPDF;
import com.dtacomp.api.schema.repositories.ConteudoPDFRepository;

@Service
@Transactional(readOnly = true)
public class ConteudoPDFService {

    private final ConteudoPDFRepository repository;

    public ConteudoPDFService(ConteudoPDFRepository repository) {
        this.repository = repository;
    }

    public List<ConteudoPDF> listarTodos() {
        return repository.findAll();
    }

    public ConteudoPDF buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo PDF não encontrado: " + id));
    }

    @Transactional
    public ConteudoPDF salvar(ConteudoPDF conteudoPDF) {
        return repository.save(conteudoPDF);
    }

    @Transactional
    public ConteudoPDF atualizar(UUID id, ConteudoPDF dados) {
        ConteudoPDF conteudoPDF = buscarPorId(id);
        conteudoPDF.setConteudo(dados.getConteudo());
        conteudoPDF.setUrlArquivo(dados.getUrlArquivo());
        conteudoPDF.setTamanhoBytes(dados.getTamanhoBytes());
        return repository.save(conteudoPDF);
    }

    @Transactional
    public void remover(UUID id) {
        ConteudoPDF conteudoPDF = buscarPorId(id);
        repository.delete(conteudoPDF);
    }
}
