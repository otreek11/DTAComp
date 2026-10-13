package com.dtacomp.controller;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.dto.conteudo.ConteudoResumoResponse;
import com.dtacomp.dto.revisao.RevisaoRequest;
import com.dtacomp.service.ConteudoConsultaService;
import com.dtacomp.service.ConteudoFiltro;
import com.dtacomp.service.RevisaoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/revisoes")
@PreAuthorize("hasRole('REVISOR')")
public class RevisaoController {

    private final RevisaoService revisaoService;
    private final ConteudoConsultaService consultaService;

    public RevisaoController(RevisaoService revisaoService, ConteudoConsultaService consultaService) {
        this.revisaoService = revisaoService;
        this.consultaService = consultaService;
    }

    @GetMapping("/pendentes")
    public PagedModel<ConteudoResumoResponse> listarPendentes(
            @AuthenticationPrincipal Usuario revisor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ConteudoFiltro filtro = new ConteudoFiltro(
                null, null, null, null, null,
                null, 
                revisor.getId(),
                null,
                null,
                Conteudo.StatusConteudo.REVISAO, 
                null,null
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "data"));
        return new PagedModel<>(consultaService.pesquisar(filtro, pageable));
    }

    @PostMapping
    public ResponseEntity<Void> registrarRevisao(
            @AuthenticationPrincipal Usuario revisor,
            @Valid @RequestBody RevisaoRequest request) {

        revisaoService.registrarRevisao(
                request.idConteudo(),
                revisor,
                request.aprovado(),
                request.parecer()
        );

        return ResponseEntity.ok().build();
    }
}