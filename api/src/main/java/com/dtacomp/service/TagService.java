package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.Tag;
import com.dtacomp.api.schema.repositories.TagRepository;
import com.dtacomp.exception.ConflitoException;

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

    public List<Tag> listarOrdenadasPorNome() {
        return repository.findAll(Sort.by(Sort.Order.asc("nome").ignoreCase()));
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
    public Tag criar(String nome) {
        String limpo = nome.trim();
        if (repository.existsByNomeIgnoreCase(limpo)) {
            throw new ConflitoException("Já existe uma tag com o nome: " + limpo);
        }
        return repository.save(Tag.builder().nome(limpo).build());
    }

    @Transactional
    public Tag renomear(UUID id, String nome) {
        Tag tag = buscarPorId(id);
        String limpo = nome.trim();
        if (repository.existsByNomeIgnoreCaseAndIdNot(limpo, id)) {
            throw new ConflitoException("Já existe uma tag com o nome: " + limpo);
        }
        tag.setNome(limpo);
        return repository.save(tag);
    }

    @Transactional
    public void excluir(UUID id) {
        Tag tag = buscarPorId(id);
        long emUso = repository.contarConteudosComTag(id);
        if (emUso > 0) {
            throw new ConflitoException("A tag está em uso por " + emUso
                    + " conteúdo(s) e não pode ser excluída (para corrigir o nome, renomeie-a)");
        }
        repository.delete(tag);
    }

    @Transactional
    public void remover(UUID id) {
        Tag tag = buscarPorId(id);
        repository.delete(tag);
    }
}
