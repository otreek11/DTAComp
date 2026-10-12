package com.dtacomp.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.dtacomp.api.ApiApplication;
import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.config.security.SecurityService;
import com.dtacomp.dto.conteudo.ConteudoDetalheResponse;
import com.dtacomp.dto.conteudo.ConteudoPDFRequest;
import com.dtacomp.dto.conteudo.ConteudoResumoResponse;
import com.dtacomp.dto.conteudo.ConteudoTextoRequest;
import com.dtacomp.service.ArquivoConteudo;
import com.dtacomp.service.ConteudoConsultaService;
import com.dtacomp.service.ConteudoFiltro;
import com.dtacomp.service.PublicacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
class ConteudoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConteudoConsultaService consultaService;

    @MockitoBean
    private PublicacaoService publicacaoService;

    @MockitoBean
    private SecurityService securityService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID id;
    private ConteudoDetalheResponse detalhe;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        detalhe = new ConteudoDetalheResponse(id, "Titulo", "Resumo", "texto", "publicado",
                LocalDate.of(2026, 1, 1), List.of("java"), List.of(), null, null, List.of());
        when(consultaService.detalhar(id)).thenReturn(detalhe);
    }

    // ---------- helpers ----------

    private RequestPostProcessor como(TipoPerfil perfil) {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Fulano")
                .email(perfil.name().toLowerCase() + "@teste.com")
                .senhaHash("x")
                .perfil(perfil)
                .build();
        return user(usuario);
    }

    private Conteudo conteudoCriado() {
        return Conteudo.builder().id(id).titulo("Titulo")
                .tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).build();
    }

    private ConteudoTextoRequest textoValido() {
        return new ConteudoTextoRequest("Titulo", "Resumo", "<p>corpo</p>",
                List.of("java"), List.of("autor@teste.com"), List.of());
    }

    private ConteudoPDFRequest pdfValido() {
        return new ConteudoPDFRequest("Titulo", "Resumo", List.of("java"), List.of("autor@teste.com"));
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private MockMultipartFile parteDados(Object dados) throws Exception {
        return new MockMultipartFile("dados", "", MediaType.APPLICATION_JSON_VALUE,
                json(dados).getBytes(StandardCharsets.UTF_8));
    }

    private MockMultipartFile parteArquivo() {
        return new MockMultipartFile("arquivo", "doc.pdf", MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4".getBytes(StandardCharsets.UTF_8));
    }

    // ---------- GET /conteudos ----------

    @Test
    void listar_publicoSemToken_retornaPagina() throws Exception {
        var resumo = new ConteudoResumoResponse(id, "Titulo", "Resumo", "texto", "publicado",
                LocalDate.of(2026, 1, 1), List.of(), List.of());
        when(consultaService.pesquisar(any(), any())).thenReturn(new PageImpl<>(List.of(resumo)));

        mockMvc.perform(get("/conteudos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Titulo"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void listar_montaFiltroEForcaStatusPublicado() throws Exception {
        UUID autorId = UUID.randomUUID();
        UUID instId = UUID.randomUUID();
        when(consultaService.pesquisar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/conteudos")
                        .param("q", "spring").param("tipo", "PDF")
                        .param("autorId", autorId.toString())
                        .param("instituicaoId", instId.toString())
                        .param("tags", "java", "web")
                        .param("de", "2026-01-01").param("ate", "2026-12-31"))
                .andExpect(status().isOk());

        ArgumentCaptor<ConteudoFiltro> captor = ArgumentCaptor.forClass(ConteudoFiltro.class);
        verify(consultaService).pesquisar(captor.capture(), any());
        ConteudoFiltro f = captor.getValue();
        assertThat(f.q()).isEqualTo("spring");
        assertThat(f.tipo()).isEqualTo(TipoConteudo.PDF);
        assertThat(f.status()).isEqualTo(StatusConteudo.PUBLICADO);
        assertThat(f.idAutor()).isEqualTo(autorId);
        assertThat(f.idInstituicao()).isEqualTo(instId);
        assertThat(f.tags()).containsExactly("java", "web");
        assertThat(f.de()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(f.ate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void listar_limitaTamanhoDaPaginaEUsaPadroes() throws Exception {
        when(consultaService.pesquisar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/conteudos").param("size", "999").param("page", "-3"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(consultaService).pesquisar(any(), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getSort().getOrderFor("data").isDescending()).isTrue();
    }

    @Test
    void listar_tipoInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/conteudos").param("tipo", "xyz"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_campoDeOrdenacaoInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/conteudos").param("sort", "senha,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_direcaoDeOrdenacaoInvalida_retorna400() throws Exception {
        mockMvc.perform(get("/conteudos").param("sort", "titulo,xx"))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /conteudos/{id} ----------

    @Test
    void detalhar_publico_retorna200() throws Exception {
        mockMvc.perform(get("/conteudos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.titulo").value("Titulo"));
    }

    @Test
    void detalhar_idMalFormado_retorna400() throws Exception {
        mockMvc.perform(get("/conteudos/{id}", "nao-e-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void detalhar_inexistente_retorna404() throws Exception {
        UUID outro = UUID.randomUUID();
        when(consultaService.detalhar(outro)).thenThrow(new IllegalArgumentException("Conteúdo não encontrado"));

        mockMvc.perform(get("/conteudos/{id}", outro))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /conteudos/{id}/arquivo ----------

    @Test
    void arquivo_retornaPdfInline() throws Exception {
        when(consultaService.carregarPdf(id))
                .thenReturn(new ArquivoConteudo(new ByteArrayResource("%PDF".getBytes()), "doc.pdf"));

        mockMvc.perform(get("/conteudos/{id}/arquivo", id))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"doc.pdf\""));
    }

    // ---------- POST /conteudos/texto ----------

    @Test
    void criarTexto_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/conteudos/texto")
                        .contentType(MediaType.APPLICATION_JSON).content(json(textoValido())))
                .andExpect(status().isUnauthorized());

        verify(publicacaoService, never()).submeterTexto(any(), any());
    }

    @Test
    void criarTexto_perfisPermitidos_retorna201ComLocation() throws Exception {
        when(publicacaoService.submeterTexto(any(), any())).thenReturn(conteudoCriado());

        for (TipoPerfil perfil : TipoPerfil.values()) {
            mockMvc.perform(post("/conteudos/texto").with(como(perfil))
                            .contentType(MediaType.APPLICATION_JSON).content(json(textoValido())))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "/conteudos/" + id))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }
    }

    @Test
    void criarTexto_corpoInvalido_retorna400() throws Exception {
        var invalido = new ConteudoTextoRequest("", null, "", List.of(), List.of("nao-e-email"), List.of());

        mockMvc.perform(post("/conteudos/texto").with(como(TipoPerfil.MEMBRO))
                        .contentType(MediaType.APPLICATION_JSON).content(json(invalido)))
                .andExpect(status().isBadRequest());

        verify(publicacaoService, never()).submeterTexto(any(), any());
    }

    // ---------- POST /conteudos/pdf ----------

    @Test
    void criarPdf_semToken_retorna401() throws Exception {
        mockMvc.perform(multipart("/conteudos/pdf").file(parteDados(pdfValido())).file(parteArquivo()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criarPdf_ok_retorna201() throws Exception {
        when(publicacaoService.submeterPdf(any(), any(), any())).thenReturn(conteudoCriado());

        mockMvc.perform(multipart("/conteudos/pdf").file(parteDados(pdfValido())).file(parteArquivo())
                        .with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/conteudos/" + id));
    }

    @Test
    void criarPdf_semParteDados_retorna400() throws Exception {
        mockMvc.perform(multipart("/conteudos/pdf").file(parteArquivo()).with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isBadRequest());

        verify(publicacaoService, never()).submeterPdf(any(), any(), any());
    }

    @Test
    void criarPdf_dadosInvalidos_retorna400() throws Exception {
        var invalido = new ConteudoPDFRequest("", null, List.of(), List.of());

        mockMvc.perform(multipart("/conteudos/pdf").file(parteDados(invalido)).file(parteArquivo())
                        .with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT /conteudos/{id}/texto ----------

    @Test
    void atualizarTexto_autor_retorna200() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(true);

        mockMvc.perform(put("/conteudos/{id}/texto", id).with(como(TipoPerfil.MEMBRO))
                        .contentType(MediaType.APPLICATION_JSON).content(json(textoValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(publicacaoService).reenviarTexto(eq(id), any(Usuario.class), any());
    }

    @Test
    void atualizarTexto_naoAutor_retorna403() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(false);

        mockMvc.perform(put("/conteudos/{id}/texto", id).with(como(TipoPerfil.MEMBRO))
                        .contentType(MediaType.APPLICATION_JSON).content(json(textoValido())))
                .andExpect(status().isForbidden());

        verify(publicacaoService, never()).reenviarTexto(any(), any(), any());
    }

    @Test
    void atualizarTexto_revisor_retorna403() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(true);

        mockMvc.perform(put("/conteudos/{id}/texto", id).with(como(TipoPerfil.REVISOR))
                        .contentType(MediaType.APPLICATION_JSON).content(json(textoValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void atualizarTexto_semToken_retorna401() throws Exception {
        mockMvc.perform(put("/conteudos/{id}/texto", id)
                        .contentType(MediaType.APPLICATION_JSON).content(json(textoValido())))
                .andExpect(status().isUnauthorized());
    }

    // ---------- PUT /conteudos/{id}/pdf ----------

    @Test
    void atualizarPdf_autor_retorna200() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(true);

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/conteudos/{id}/pdf", id)
                        .file(parteDados(pdfValido())).file(parteArquivo()).with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(publicacaoService).reenviarPdf(eq(id), any(Usuario.class), any(), any());
    }

    @Test
    void atualizarPdf_naoAutor_retorna403() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(false);

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/conteudos/{id}/pdf", id)
                        .file(parteDados(pdfValido())).file(parteArquivo()).with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void atualizarPdf_semToken_retorna401() throws Exception {
        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/conteudos/{id}/pdf", id)
                        .file(parteDados(pdfValido())).file(parteArquivo()))
                .andExpect(status().isUnauthorized());
    }

    // ---------- DELETE /conteudos/{id} ----------

    @Test
    void excluir_autor_retorna204() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(true);

        mockMvc.perform(delete("/conteudos/{id}", id).with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isNoContent());

        verify(publicacaoService).excluir(eq(id), any(Usuario.class));
    }

    @Test
    void excluir_adminNaoAutor_retorna204() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(false);

        mockMvc.perform(delete("/conteudos/{id}", id).with(como(TipoPerfil.ADMIN)))
                .andExpect(status().isNoContent());
    }

    @Test
    void excluir_membroNaoAutor_retorna403() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(false);

        mockMvc.perform(delete("/conteudos/{id}", id).with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isForbidden());

        verify(publicacaoService, never()).excluir(any(), any());
    }

    @Test
    void excluir_revisor_retorna403() throws Exception {
        when(securityService.isAutorDoConteudo(id)).thenReturn(true);

        mockMvc.perform(delete("/conteudos/{id}", id).with(como(TipoPerfil.REVISOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void excluir_semToken_retorna401() throws Exception {
        mockMvc.perform(delete("/conteudos/{id}", id))
                .andExpect(status().isUnauthorized());
    }
}
