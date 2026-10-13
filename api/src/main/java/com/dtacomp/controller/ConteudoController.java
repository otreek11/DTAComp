package com.dtacomp.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.dto.conteudo.ConteudoDetalheResponse;
import com.dtacomp.dto.conteudo.ConteudoPDFRequest;
import com.dtacomp.dto.conteudo.ConteudoResumoResponse;
import com.dtacomp.dto.conteudo.ConteudoTextoRequest;
import com.dtacomp.exception.RequisicaoInvalidaException;
import com.dtacomp.service.ArquivoConteudo;
import com.dtacomp.service.ConteudoConsultaService;
import com.dtacomp.service.ConteudoFiltro;
import com.dtacomp.service.PublicacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/conteudos")
public class ConteudoController {

    private static final int TAMANHO_PADRAO = 20;
    private static final int TAMANHO_MAXIMO = 50;
    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("data", "titulo");

    private final ConteudoConsultaService consultaService;
    private final PublicacaoService publicacaoService;

    public ConteudoController(ConteudoConsultaService consultaService,
                              PublicacaoService publicacaoService) {
        this.consultaService = consultaService;
        this.publicacaoService = publicacaoService;
    }

    @GetMapping
    public PagedModel<ConteudoResumoResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String resumo,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) UUID autorId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) UUID instituicaoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + TAMANHO_PADRAO) int size,
            @RequestParam(defaultValue = "data,desc") String sort) {

        ConteudoFiltro filtro = new ConteudoFiltro(
                q, titulo, resumo, tags, autor,
                autorId,
                null,
                instituicaoId,
                interpretarTipo(tipo),
                StatusConteudo.PUBLICADO,
                de, ate
        );

        return new PagedModel<>(consultaService.pesquisar(filtro, paginar(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ConteudoDetalheResponse detalhar(@PathVariable UUID id) {
        return consultaService.detalhar(id);
    }

    @GetMapping("/{id}/arquivo")
    public ResponseEntity<Resource> arquivo(@PathVariable UUID id) {
        ArquivoConteudo arquivo = consultaService.carregarPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(arquivo.nome()).build().toString())
                .body(arquivo.recurso());
    }

    @PostMapping("/texto")
    public ResponseEntity<ConteudoDetalheResponse> criarTexto(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody ConteudoTextoRequest request) {

        Conteudo conteudo = publicacaoService.submeterTexto(usuario, request);
        return criado(conteudo);
    }

    @PostMapping(path = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConteudoDetalheResponse> criarPDF(
            @AuthenticationPrincipal Usuario usuario,
            @RequestPart(value = "dados", required = false) @Valid ConteudoPDFRequest dados,
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo) {

        Conteudo conteudo = publicacaoService.submeterPDF(usuario, exigirDados(dados), arquivo);
        return criado(conteudo);
    }

    @PutMapping("/{id}/texto")
    @PreAuthorize("@securityService.isAutorDoConteudo(#id)")
    public ConteudoDetalheResponse atualizarTexto(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody ConteudoTextoRequest request) {

        publicacaoService.reenviarTexto(id, usuario, request);
        return consultaService.detalhar(id);
    }

    @PutMapping(path = "/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.isAutorDoConteudo(#id)")
    public ConteudoDetalheResponse atualizarPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuario,
            @RequestPart(value = "dados", required = false) @Valid ConteudoPDFRequest dados,
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo) {

        publicacaoService.reenviarPDF(id, usuario, exigirDados(dados), arquivo);
        return consultaService.detalhar(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAutorDoConteudo(#id)")
    public ResponseEntity<Void> excluir(@PathVariable UUID id,
                                        @AuthenticationPrincipal Usuario usuario) {
        publicacaoService.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<ConteudoDetalheResponse> criado(Conteudo conteudo) {
        return ResponseEntity
                .created(URI.create("/conteudos/" + conteudo.getId()))
                .body(consultaService.detalhar(conteudo.getId()));
    }

    private ConteudoPDFRequest exigirDados(ConteudoPDFRequest dados) {
        if (dados == null) {
            throw new RequisicaoInvalidaException(
                    "A parte 'dados' (JSON, content-type application/json) é obrigatória");
        }
        return dados;
    }

    private TipoConteudo interpretarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return null;
        }
        for (TipoConteudo candidato : TipoConteudo.values()) {
            if (candidato.name().equalsIgnoreCase(tipo.trim())) {
                return candidato;
            }
        }
        throw new RequisicaoInvalidaException("Tipo inválido: use 'texto' ou 'pdf'");
    }

    private Pageable paginar(int page, int size, String sort) {
        String[] partes = sort.split(",");
        String campo = partes[0].trim();
        if (!CAMPOS_ORDENAVEIS.contains(campo)) {
            throw new RequisicaoInvalidaException("Campo de ordenação inválido; use: " + CAMPOS_ORDENAVEIS);
        }

        Sort.Direction direcao = Sort.Direction.DESC;
        if (partes.length > 1) {
            String d = partes[1].trim();
            if (d.equalsIgnoreCase("asc")) {
                direcao = Sort.Direction.ASC;
            } else if (!d.equalsIgnoreCase("desc")) {
                throw new RequisicaoInvalidaException("Direção de ordenação inválida; use asc ou desc");
            }
        }

        int tamanho = Math.min(Math.max(size, 1), TAMANHO_MAXIMO);
        return PageRequest.of(Math.max(page, 0), tamanho,
                Sort.by(direcao, campo).and(Sort.by(Sort.Direction.ASC, "id")));
    }
}
