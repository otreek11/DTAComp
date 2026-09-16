package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.schema.classes.Instituicao;
import com.dtacomp.schema.repositories.IntituicaoRepository;

@Service
@Transactional(readOnly = true)
public class InstituicaoService {

    private final IntituicaoRepository repository;

    public InstituicaoService(IntituicaoRepository repository) {
        this.repository = repository;
    }

    public List<Instituicao> listarTodos() {
        return repository.findAll();
    }

    public Instituicao buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instituição não encontrada: " + id));
    }

    @Transactional
    public Instituicao salvar(Instituicao instituicao) {
        return repository.save(instituicao);
    }

    @Transactional
    public Instituicao atualizar(UUID id, Instituicao dados) {
        Instituicao instituicao = buscarPorId(id);
        instituicao.setNome(dados.getNome());
        instituicao.setSigla(dados.getSigla());
        return repository.save(instituicao);
    }

    @Transactional
    public void remover(UUID id) {
        Instituicao instituicao = buscarPorId(id);
        repository.delete(instituicao);
    }
}
