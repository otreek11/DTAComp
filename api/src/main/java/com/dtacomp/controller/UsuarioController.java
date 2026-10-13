package com.dtacomp.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.dto.conteudo.ConteudoResumoResponse;
import com.dtacomp.dto.usuario.AlterarSenhaRequest;
import com.dtacomp.dto.usuario.AtualizarPerfilRequest;
import com.dtacomp.dto.usuario.ExcluirContaRequest;
import com.dtacomp.dto.usuario.MeusDadosResponse;
import com.dtacomp.dto.usuario.PerfilPublicoResponse;
import com.dtacomp.dto.usuario.UsuarioResponse;
import com.dtacomp.exception.RequisicaoInvalidaException;
import com.dtacomp.service.ConteudoConsultaService;
import com.dtacomp.service.ConteudoFiltro;
import com.dtacomp.service.ContaService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("data", "titulo");

    private final ContaService contaService;
    private final ConteudoConsultaService consultaService;

    public UsuarioController(ContaService contaService, ConteudoConsultaService consultaService) {
        this.contaService = contaService;
        this.consultaService = consultaService;
    }


    @GetMapping("/me/conteudos")
    public PagedModel<ConteudoResumoResponse> meusConteudos(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Paginacao.TAMANHO_PADRAO) int size,
            @RequestParam(defaultValue = "data,desc") String sort) {

        ConteudoFiltro filtro = new ConteudoFiltro(
                null, null, null, null, null,
                usuario.getId(),
                null,
                null,
                null,
                interpretarStatus(status),
                null, null
        );

        return new PagedModel<>(consultaService.pesquisar(
                filtro, Paginacao.de(page, size, sort, CAMPOS_ORDENAVEIS)));
    }

    @PutMapping("/me")
    public UsuarioResponse atualizarPerfil(@AuthenticationPrincipal Usuario usuario,
                                           @Valid @RequestBody AtualizarPerfilRequest request) {
        Usuario atualizado = contaService.atualizarPerfil(usuario.getId(), request.nome(), request.idInstituicao());
        return UsuarioResponse.from(atualizado);
    }

    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(@AuthenticationPrincipal Usuario usuario,
                                             @Valid @RequestBody AlterarSenhaRequest request) {
        contaService.alterarSenha(usuario.getId(), request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/dados")
    public MeusDadosResponse meusDados(@AuthenticationPrincipal Usuario usuario) {
        return contaService.exportarDados(usuario.getId());
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> excluirConta(@AuthenticationPrincipal Usuario usuario,
                                             @Valid @RequestBody ExcluirContaRequest request) {
        contaService.excluirConta(usuario.getId(), request.senha());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/perfil")
    public PerfilPublicoResponse perfilPublico(@PathVariable UUID id) {
        return PerfilPublicoResponse.from(contaService.buscarPerfilPublico(id));
    }

    private StatusConteudo interpretarStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        for (StatusConteudo candidato : StatusConteudo.values()) {
            if (candidato.name().equalsIgnoreCase(status.trim())) {
                return candidato;
            }
        }
        throw new RequisicaoInvalidaException("Status inválido: use 'revisao', 'devolvido' ou 'publicado'");
    }
}
