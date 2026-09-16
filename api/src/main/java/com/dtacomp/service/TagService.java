package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.schema.classes.Tag;
import com.dtacomp.schema.repositories.TagRepository;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository repository;

    public TagService(TagRepository repository) {
        this.repository = repository;
    }

    public List<Tag> listarTodos() {
        return repository.findAll();
    }

    public Tag buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag não encontrada: " + id));
    }

    public Tag buscarPorNome(String nome) {
        return repository.findByNome(nome)
                .orElseThrow(() -> new IllegalArgumentException("Tag não encontrada: " + nome));
    }

    public boolean nomeExiste(String nome) {
        return repository.existsByNome(nome);
    }

    @Transactional
    public Tag salvar(Tag tag) {
        return repository.save(tag);
    }

    @Transactional
    public Tag atualizar(UUID id, Tag dados) {
        Tag tag = buscarPorId(id);
        tag.setNome(dados.getNome());
        return repository.save(tag);
    }

    @Transactional
    public void remover(UUID id) {
        Tag tag = buscarPorId(id);
        repository.delete(tag);
    }
}
