package com.dtacomp.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.exception.ConflitoException;
import com.dtacomp.service.PublicacaoService;
import com.dtacomp.service.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;
    private final PublicacaoService publicacaoService;

    public AdminController(UsuarioService usuarioService, PublicacaoService publicacaoService) {
        this.usuarioService = usuarioService;
        this.publicacaoService = publicacaoService;
    }

    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios(
            @RequestParam(required = false) TipoPerfil perfil,
            @RequestParam(required = false) UUID instituicaoId) {
        
        if (perfil != null) {
            return usuarioService.buscarPorPerfil(perfil);
        }
        if (instituicaoId != null) {
            return usuarioService.buscarPorInstituicao(instituicaoId);
        }
        return usuarioService.listarTodos();
    }

    @PutMapping("/usuarios/{id}/perfil")
    public ResponseEntity<Void> atualizarPerfil(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario admin,
            @Valid @RequestBody PerfilUpdateRequest request) {
        
        if (admin.getId().equals(id)) {
            throw new ConflitoException("Administrador não pode alterar o próprio perfil");
        }
        
        usuarioService.alterarPerfil(id, request.perfil());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> anonimizarUsuario(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario admin) {
        
        if (admin.getId().equals(id)) {
            throw new ConflitoException("Administrador não pode anonimizar a si mesmo");
        }
        
        usuarioService.anonimizar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conteudos/{id}/despublicar")
    public ResponseEntity<Void> despublicarConteudo(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario admin,
            @Valid @RequestBody DespublicarRequest request) {
        
        publicacaoService.despublicar(id, admin, request.motivo());
        return ResponseEntity.noContent().build();
    }

    public record PerfilUpdateRequest(
            @NotNull(message = "Perfil é obrigatório")
            TipoPerfil perfil
    ) {}

    public record DespublicarRequest(
            @NotBlank(message = "Motivo é obrigatório")
            String motivo
    ) {}
}