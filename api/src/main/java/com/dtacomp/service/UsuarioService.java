package com.dtacomp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.repositories.UsuarioRepository;

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
    public Usuario atualizarPerfil(UUID id, String nome, Instituicao instituicao) {
        Usuario usuario = buscarPorId(id);
        usuario.setNome(nome);
        usuario.setInstituicao(instituicao);
        return repository.save(usuario);
    }

    @Transactional
    public void alterarSenha(UUID id, String novoSenhaHash) {
        Usuario usuario = buscarPorId(id);
        usuario.setSenhaHash(novoSenhaHash);
        repository.save(usuario);
    }

    @Transactional
    public Usuario anonimizar(UUID id, String nomeAnonimo, String emailAnonimo, String senhaHashAleatoria) {
        Usuario usuario = buscarPorId(id);
        usuario.setNome(nomeAnonimo);
        usuario.setEmail(emailAnonimo);
        usuario.setSenhaHash(senhaHashAleatoria);
        usuario.setIdExterno(null);
        usuario.setPerfil(Usuario.TipoPerfil.MEMBRO);
        return repository.save(usuario);
    }

    @Transactional
    public void anonimizar(UUID id) {
        Usuario usuario = buscarPorId(id);
        usuario.setNome("Usuário removido");
        usuario.setEmail("removido-" + id + "@anonimo.invalid");
        usuario.setSenhaHash(UUID.randomUUID().toString());
        usuario.setIdExterno(null);
        usuario.setPerfil(Usuario.TipoPerfil.MEMBRO);
        repository.save(usuario);
    }

    @Transactional
    public void alterarPerfil(UUID id, Usuario.TipoPerfil perfil) {
        Usuario usuario = buscarPorId(id);
        usuario.setPerfil(perfil);
        repository.save(usuario);
    }


    public long contarPorPerfil(Usuario.TipoPerfil perfil) {
        return repository.countByPerfil(perfil);
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
