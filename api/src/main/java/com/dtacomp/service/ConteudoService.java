package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.repositories.ConteudoRepository;

@Service
@Transactional(readOnly = true)
public class ConteudoService {

    private final ConteudoRepository repository;

    public ConteudoService(ConteudoRepository repository) {
        this.repository = repository;
    }

    public List<Conteudo> listarTodos() {
        return repository.findAll();
    }

    public Conteudo buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo não encontrado: " + id));
    }

    @Transactional
    public Conteudo salvar(Conteudo conteudo) {
        return repository.save(conteudo);
    }

    @Transactional
    public Conteudo atualizar(UUID id, Conteudo dados) {
        Conteudo conteudo = buscarPorId(id);

        conteudo.setTitulo(dados.getTitulo());
        conteudo.setResumo(dados.getResumo());
        conteudo.setTipo(dados.getTipo());
        conteudo.setStatus(dados.getStatus());
        conteudo.setData(dados.getData());
        conteudo.setIdExterno(dados.getIdExterno());
        conteudo.setTags(dados.getTags());

        return repository.save(conteudo);
    }

    @Transactional
    public void remover(UUID id) {
        Conteudo conteudo = buscarPorId(id);
        repository.delete(conteudo);
    }

    public List<Conteudo> buscarPorTitulo(String titulo) {
        return repository.findByTituloContainingIgnoreCase(titulo);
    }

    public List<Conteudo> buscarPorStatus(Conteudo.StatusConteudo status) {
        return repository.findByStatus(status);
    }

    public List<Conteudo> buscarPorTipo(Conteudo.TipoConteudo tipo) {
        return repository.findByTipo(tipo);
    }

    public List<Conteudo> buscarPorTag(String nomeTag) {
        return repository.findByTagNome(nomeTag);
    }
}
