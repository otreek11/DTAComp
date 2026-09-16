package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.schema.classes.ConteudoTexto;
import com.dtacomp.schema.repositories.ConteudoTextRepository;

@Service
@Transactional(readOnly = true)
public class ConteudoTextoService {

    private final ConteudoTextRepository repository;

    public ConteudoTextoService(ConteudoTextRepository repository) {
        this.repository = repository;
    }

    public List<ConteudoTexto> listarTodos() {
        return repository.findAll();
    }

    public ConteudoTexto buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo de texto não encontrado: " + id));
    }

    @Transactional
    public ConteudoTexto salvar(ConteudoTexto conteudoTexto) {
        return repository.save(conteudoTexto);
    }

    @Transactional
    public ConteudoTexto atualizar(UUID id, ConteudoTexto dados) {
        ConteudoTexto conteudoTexto = buscarPorId(id);
        conteudoTexto.setConteudo(dados.getConteudo());
        conteudoTexto.setCorpoHtml(dados.getCorpoHtml());
        conteudoTexto.setImagens(dados.getImagens());
        return repository.save(conteudoTexto);
    }

    @Transactional
    public void remover(UUID id) {
        ConteudoTexto conteudoTexto = buscarPorId(id);
        repository.delete(conteudoTexto);
    }
}
