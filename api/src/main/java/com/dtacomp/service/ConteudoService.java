package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.repositories.ConteudoRepository;
import com.dtacomp.api.schema.specifications.ConteudoSpecifications;

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

    public Page<Conteudo> pesquisar(ConteudoFiltro filtro, Pageable pageable) {
        Specification<Conteudo> spec = ConteudoSpecifications.comStatus(filtro.status())
                .and(ConteudoSpecifications.comTipo(filtro.tipo()))
                .and(ConteudoSpecifications.textoLivre(filtro.q()))
                .and(ConteudoSpecifications.tituloContem(filtro.titulo()))
                .and(ConteudoSpecifications.resumoContem(filtro.resumo()))
                .and(ConteudoSpecifications.comAlgumaTag(filtro.tags()))
                .and(ConteudoSpecifications.autorNomeContem(filtro.autor()))
                .and(ConteudoSpecifications.comAutor(filtro.idAutor()))
                .and(ConteudoSpecifications.semAutor(filtro.excluirIdAutor()))
                .and(ConteudoSpecifications.comAutorDaInstituicao(filtro.idInstituicao()))
                .and(ConteudoSpecifications.dataDe(filtro.de()))
                .and(ConteudoSpecifications.dataAte(filtro.ate()));

        return repository.findAll(spec, pageable);
    }
}
