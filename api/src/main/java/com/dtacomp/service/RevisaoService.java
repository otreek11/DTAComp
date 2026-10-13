package com.dtacomp.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Revisao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.repositories.ConteudoRepository;
import com.dtacomp.api.schema.repositories.RevisaoRepository;
import com.dtacomp.exception.ConflitoException;

@Service
@Transactional(readOnly = true)
public class RevisaoService {

    private final RevisaoRepository revisaoRepository;
    private final ConteudoRepository conteudoRepository;

    public RevisaoService(RevisaoRepository revisaoRepository, ConteudoRepository conteudoRepository) {
        this.revisaoRepository = revisaoRepository;
        this.conteudoRepository = conteudoRepository;
    }

    public List<Revisao> listarTodos() {
        return revisaoRepository.findAll();
    }

    public Revisao buscarPorId(UUID id) {
        return revisaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Revisão não encontrada: " + id));
    }

    @Transactional
    public Revisao salvar(Revisao revisao) {
        return revisaoRepository.save(revisao);
    }

    @Transactional
    public Revisao atualizar(UUID id, Revisao dados) {
        Revisao revisao = buscarPorId(id);

        revisao.setConteudo(dados.getConteudo());
        revisao.setRevisor(dados.getRevisor());
        revisao.setStatus(dados.getStatus());
        revisao.setParecerHumano(dados.getParecerHumano());

        return revisaoRepository.save(revisao);
    }

    @Transactional
    public void remover(UUID id) {
        Revisao revisao = buscarPorId(id);
        revisaoRepository.delete(revisao);
    }

    public List<Revisao> buscarPorRevisor(UUID idRevisor) {
        return revisaoRepository.findByRevisor_Id(idRevisor);
    }

    public List<Revisao> buscarPorConteudo(UUID idConteudo) {
        return revisaoRepository.findByConteudo_Id(idConteudo);
    }

    public List<Revisao> buscarPorRevisorEStatus(UUID idRevisor, Conteudo.StatusConteudo status) {
        return revisaoRepository.findByRevisor_IdAndStatus(idRevisor, status);
    }

    @Transactional
    public void registrarRevisao(UUID idConteudo, Usuario revisor, 
                                  Boolean aprovado, String parecer) {
        
        Conteudo conteudo = conteudoRepository.findByIdParaAtualizar(idConteudo)
                .orElseThrow(() -> new IllegalArgumentException("Conteúdo não encontrado"));

        if (conteudo.getStatus() != Conteudo.StatusConteudo.REVISAO) {
            throw new ConflitoException("Conteúdo não está em revisão");
        }

        Conteudo.StatusConteudo status = (aprovado) ? Conteudo.StatusConteudo.PUBLICADO : Conteudo.StatusConteudo.DEVOLVIDO;

        Revisao revisao = Revisao.builder()
                .conteudo(conteudo)
                .revisor(revisor)
                .status(status)
                .parecerHumano(parecer)
                .dataRevisao(LocalDate.now())
                .build();

        revisaoRepository.save(revisao);

        if (aprovado) {
            conteudo.setStatus(Conteudo.StatusConteudo.PUBLICADO);
        } else {
            conteudo.setStatus(Conteudo.StatusConteudo.DEVOLVIDO);
        }

        conteudoRepository.save(conteudo);
    }
}
