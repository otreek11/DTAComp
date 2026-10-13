package com.dtacomp.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.dto.usuario.MeusDadosResponse;
import com.dtacomp.dto.usuario.MeusDadosResponse.ConteudoExportado;
import com.dtacomp.dto.usuario.MeusDadosResponse.RevisaoExportada;
import com.dtacomp.exception.ConflitoException;
import com.dtacomp.exception.RequisicaoInvalidaException;

@Service
@Transactional(readOnly = true)
public class ContaService {

    public static final String NOME_ANONIMO = "Usuário removido";
    public static final String DOMINIO_ANONIMO = "@anonimo.invalid";

    private final UsuarioService usuarioService;
    private final InstituicaoService instituicaoService;
    private final UsuarioConteudoService usuarioConteudoService;
    private final RevisaoService revisaoService;
    private final PasswordEncoder passwordEncoder;

    public ContaService(UsuarioService usuarioService,
                        InstituicaoService instituicaoService,
                        UsuarioConteudoService usuarioConteudoService,
                        RevisaoService revisaoService,
                        PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.instituicaoService = instituicaoService;
        this.usuarioConteudoService = usuarioConteudoService;
        this.revisaoService = revisaoService;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario buscarPerfilPublico(UUID idUsuario) {
        Usuario usuario = usuarioService.buscarPorId(idUsuario);
        if (usuario.getEmail() != null && usuario.getEmail().endsWith(DOMINIO_ANONIMO)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + idUsuario);
        }
        return usuario;
    }

    @Transactional
    public Usuario atualizarPerfil(UUID idUsuario, String nome, UUID idInstituicao) {
        Instituicao instituicao;
        try {
            instituicao = instituicaoService.buscarPorId(idInstituicao);
        } catch (IllegalArgumentException e) {
            // instituição vem no corpo da requisição: é dado inválido (400), não rota inexistente (404)
            throw new RequisicaoInvalidaException("Instituição não encontrada: " + idInstituicao);
        }
        return usuarioService.atualizarPerfil(idUsuario, nome.trim(), instituicao);
    }

    @Transactional
    public void alterarSenha(UUID idUsuario, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioService.buscarPorId(idUsuario);
        exigirSenha(usuario, senhaAtual, "Senha atual incorreta");
        usuarioService.alterarSenha(idUsuario, passwordEncoder.encode(novaSenha));
    }

    public MeusDadosResponse exportarDados(UUID idUsuario) {
        Usuario usuario = usuarioService.buscarPorId(idUsuario);

        List<ConteudoExportado> conteudos = usuarioConteudoService.buscarConteudosPorAutor(idUsuario).stream()
                .sorted(Comparator.comparing(Conteudo::getData, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ConteudoExportado::from)
                .toList();

        List<RevisaoExportada> revisoes = revisaoService.buscarPorRevisor(idUsuario).stream()
                .map(RevisaoExportada::from)
                .toList();

        return new MeusDadosResponse(MeusDadosResponse.Conta.from(usuario), conteudos, revisoes, Instant.now());
    }

    @Transactional
    public void excluirConta(UUID idUsuario, String senha) {
        Usuario usuario = usuarioService.buscarPorId(idUsuario);
        exigirSenha(usuario, senha, "Senha incorreta");

        if (usuario.getPerfil() == TipoPerfil.ADMIN && usuarioService.contarPorPerfil(TipoPerfil.ADMIN) <= 1) {
            throw new ConflitoException(
                    "Você é o único administrador; promova outro usuário a admin antes de excluir sua conta");
        }

        usuarioService.anonimizar(
                idUsuario,
                NOME_ANONIMO,
                "removido-" + idUsuario + DOMINIO_ANONIMO,
                // senha aleatória e descartada: ninguém consegue autenticar nesta conta
                passwordEncoder.encode(UUID.randomUUID().toString()));
    }

    private void exigirSenha(Usuario usuario, String senhaInformada, String mensagemErro) {
        if (!passwordEncoder.matches(senhaInformada, usuario.getSenhaHash())) {
            throw new RequisicaoInvalidaException(mensagemErro);
        }
    }
}
