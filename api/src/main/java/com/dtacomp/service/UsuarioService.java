package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.schema.classes.Usuario;
import com.dtacomp.schema.repositories.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    public Usuario buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
    }

    public Usuario buscarPorEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
    }

    public boolean emailExiste(String email) {
        return repository.existsByEmail(email);
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        return repository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(UUID id, Usuario dados) {
        Usuario usuario = buscarPorId(id);

        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail());
        usuario.setSenhaHash(dados.getSenhaHash());
        usuario.setPerfil(dados.getPerfil());
        usuario.setInstituicao(dados.getInstituicao());
        usuario.setIdExterno(dados.getIdExterno());

        return repository.save(usuario);
    }

    @Transactional
    public void remover(UUID id) {
        Usuario usuario = buscarPorId(id);
        repository.delete(usuario);
    }

    public List<Usuario> buscarPorPerfil(Usuario.TipoPerfil perfil) {
        return repository.findByPerfil(perfil);
    }

    public List<Usuario> buscarPorInstituicao(UUID idInstituicao) {
        return repository.findByInstituicao_Id(idInstituicao);
    }
}
