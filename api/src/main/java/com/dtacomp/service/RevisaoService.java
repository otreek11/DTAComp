package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.schema.classes.Conteudo;
import com.dtacomp.schema.classes.Revisao;
import com.dtacomp.schema.repositories.RevisaoRepository;

@Service
@Transactional(readOnly = true)
public class RevisaoService {

    private final RevisaoRepository repository;

    public RevisaoService(RevisaoRepository repository) {
        this.repository = repository;
    }

    public List<Revisao> listarTodos() {
        return repository.findAll();
    }

    public Revisao buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Revisão não encontrada: " + id));
    }

    @Transactional
    public Revisao salvar(Revisao revisao) {
        return repository.save(revisao);
    }

    @Transactional
    public Revisao atualizar(UUID id, Revisao dados) {
        Revisao revisao = buscarPorId(id);

        revisao.setConteudo(dados.getConteudo());
        revisao.setRevisor(dados.getRevisor());
        revisao.setStatus(dados.getStatus());
        revisao.setParecerHumano(dados.getParecerHumano());

        return repository.save(revisao);
    }

    @Transactional
    public void remover(UUID id) {
        Revisao revisao = buscarPorId(id);
        repository.delete(revisao);
    }

    public List<Revisao> buscarPorRevisor(UUID idRevisor) {
        return repository.findByRevisor_Id(idRevisor);
    }

    public List<Revisao> buscarPorConteudo(UUID idConteudo) {
        return repository.findByConteudo_Id(idConteudo);
    }

    public List<Revisao> buscarPorRevisorEStatus(UUID idRevisor, Conteudo.StatusConteudo status) {
        return repository.findByRevisor_IdAndStatus(idRevisor, status);
    }
}
