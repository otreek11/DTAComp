package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.Imagem;
import com.dtacomp.api.schema.repositories.ImagemRepository;

@Service
@Transactional(readOnly = true)
public class ImagemService {

    private final ImagemRepository repository;

    public ImagemService(ImagemRepository repository) {
        this.repository = repository;
    }

    public List<Imagem> listarTodos() {
        return repository.findAll();
    }

    public Imagem buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imagem não encontrada: " + id));
    }

    public List<Imagem> buscarPorConteudoTexto(UUID idConteudoTexto) {
        return repository.findByConteudoTexto_IdConteudo(idConteudoTexto);
    }

    @Transactional
    public Imagem salvar(Imagem imagem) {
        return repository.save(imagem);
    }

    @Transactional
    public Imagem atualizar(UUID id, Imagem dados) {
        Imagem imagem = buscarPorId(id);
        imagem.setConteudoTexto(dados.getConteudoTexto());
        imagem.setUrlImage(dados.getUrlImage());
        imagem.setLegenda(dados.getLegenda());
        return repository.save(imagem);
    }

    @Transactional
    public void remover(UUID id) {
        Imagem imagem = buscarPorId(id);
        repository.delete(imagem);
    }
}
