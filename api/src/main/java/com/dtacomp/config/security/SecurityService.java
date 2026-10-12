package com.dtacomp.config.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.repositories.UsuarioConteudoRepository;
import com.dtacomp.api.schema.repositories.UsuarioRepository;

@Component("securityService")
public class SecurityService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConteudoRepository usuarioConteudoRepository;

    public SecurityService(UsuarioRepository usuarioRepository,
                           UsuarioConteudoRepository usuarioConteudoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioConteudoRepository = usuarioConteudoRepository;
    }

    public Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        if (auth.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }

        return usuarioRepository.findByEmail(auth.getName()).orElse(null);
    }

    public boolean isCurrentUser(UUID idUsuario) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return false;
        }
        return usuario.getId().equals(idUsuario);
    }

    public boolean isAutorDoConteudo(UUID idConteudo) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return false;
        }

        return usuarioConteudoRepository.findByConteudo_IdOrderByIdxPosicaoAsc(idConteudo)
                .stream()
                .anyMatch(uc -> uc.getUsuario().getId().equals(usuario.getId()));
    }

    public boolean isAutorRevisorOuAdmin(UUID idConteudo) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return false;
        }
        if (usuario.getPerfil() == Usuario.TipoPerfil.REVISOR
                || usuario.getPerfil() == Usuario.TipoPerfil.ADMIN) {
            return true;
        }
        return isAutorDoConteudo(idConteudo);
    }

    public boolean podeVisualizarConteudo(Conteudo conteudo) {
        return conteudo.getStatus() == Conteudo.StatusConteudo.PUBLICADO
                || isAutorRevisorOuAdmin(conteudo.getId());
    }
}
