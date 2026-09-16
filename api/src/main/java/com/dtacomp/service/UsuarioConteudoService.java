package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.schema.classes.Conteudo;
import com.dtacomp.schema.classes.UsuarioConteudo;
import com.dtacomp.schema.classes.UsuarioConteudo.UsuarioConteudoPK;
import com.dtacomp.schema.repositories.UsuarioConteudoRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioConteudoService {

    private final UsuarioConteudoRepository repository;

    public UsuarioConteudoService(UsuarioConteudoRepository repository) {
        this.repository = repository;
    }

    public List<UsuarioConteudo> listarTodos() {
        return repository.findAll();
    }

    public UsuarioConteudo buscarPorId(UsuarioConteudoPK id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Relação usuário/conteúdo não encontrada"));
    }

    public List<UsuarioConteudo> buscarAutores(UUID idConteudo) {
        return repository.findByConteudo_IdOrderByIdxPosicaoAsc(idConteudo);
    }

    public List<Conteudo> buscarConteudosPorAutor(UUID idUsuario) {
        return repository.findConteudosPorAutor(idUsuario);
    }

    @Transactional
    public UsuarioConteudo salvar(UsuarioConteudo usuarioConteudo) {
        return repository.save(usuarioConteudo);
    }

    @Transactional
    public void remover(UsuarioConteudoPK id) {
        UsuarioConteudo usuarioConteudo = buscarPorId(id);
        repository.delete(usuarioConteudo);
    }
}
