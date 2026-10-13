package com.dtacomp.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dtacomp.api.ApiApplication;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.dto.conteudo.ConteudoResumoResponse;
import com.dtacomp.dto.revisao.RevisaoRequest;
import com.dtacomp.exception.ConflitoException;
import com.dtacomp.service.ConteudoConsultaService;
import com.dtacomp.service.ConteudoFiltro;
import com.dtacomp.service.RevisaoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.dtacomp.controller.TestUsuarios.como;
import static com.dtacomp.controller.TestUsuarios.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
class RevisaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevisaoService revisaoService;

    @MockitoBean
    private ConteudoConsultaService consultaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    // ---------- GET /revisoes/pendentes ----------

    @Test
    void pendentes_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/revisoes/pendentes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pendentes_membroEAdmin_retorna403() throws Exception {
        for (TipoPerfil perfil : new TipoPerfil[] {TipoPerfil.MEMBRO, TipoPerfil.ADMIN}) {
            mockMvc.perform(get("/revisoes/pendentes").with(como(perfil)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void pendentes_revisor_retornaPaginaEMontaFiltro() throws Exception {
        Usuario revisor = usuario(TipoPerfil.REVISOR);
        var resumo = new ConteudoResumoResponse(UUID.randomUUID(), "Titulo", "Resumo", "texto", "revisao",
                LocalDate.of(2026, 1, 1), List.of(), List.of());
        when(consultaService.pesquisar(any(), any())).thenReturn(new PageImpl<>(List.of(resumo)));

        mockMvc.perform(get("/revisoes/pendentes").param("page", "2").param("size", "5").with(como(revisor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Titulo"));

        ArgumentCaptor<ConteudoFiltro> filtro = ArgumentCaptor.forClass(ConteudoFiltro.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(consultaService).pesquisar(filtro.capture(), pageable.capture());

        assertThat(filtro.getValue().status()).isEqualTo(StatusConteudo.REVISAO);
        assertThat(filtro.getValue().excluirIdAutor()).isEqualTo(revisor.getId());
        assertThat(filtro.getValue().idAutor()).isNull();
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageable.getValue().getSort().getOrderFor("data").isAscending()).isTrue();
    }

    // ---------- POST /revisoes ----------

    @Test
    void registrar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/revisoes").contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RevisaoRequest(UUID.randomUUID(), true, null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrar_membro_retorna403() throws Exception {
        mockMvc.perform(post("/revisoes").with(como(TipoPerfil.MEMBRO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RevisaoRequest(UUID.randomUUID(), true, null))))
                .andExpect(status().isForbidden());

        verify(revisaoService, never()).registrarRevisao(any(), any(), any(), any());
    }

    @Test
    void registrar_aprovacao_retorna200() throws Exception {
        UUID idConteudo = UUID.randomUUID();

        mockMvc.perform(post("/revisoes").with(como(TipoPerfil.REVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RevisaoRequest(idConteudo, true, "ok"))))
                .andExpect(status().isOk());

        verify(revisaoService).registrarRevisao(eq(idConteudo), any(Usuario.class), eq(true), eq("ok"));
    }

    @Test
    void registrar_devolucaoComParecer_retorna200() throws Exception {
        UUID idConteudo = UUID.randomUUID();

        mockMvc.perform(post("/revisoes").with(como(TipoPerfil.REVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RevisaoRequest(idConteudo, false, "corrigir referências"))))
                .andExpect(status().isOk());

        verify(revisaoService).registrarRevisao(eq(idConteudo), any(Usuario.class), eq(false),
                eq("corrigir referências"));
    }

    @Test
    void registrar_devolucaoSemParecer_retorna400() throws Exception {
        for (String parecer : new String[] {null, "", "   "}) {
            mockMvc.perform(post("/revisoes").with(como(TipoPerfil.REVISOR))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new RevisaoRequest(UUID.randomUUID(), false, parecer))))
                    .andExpect(status().isBadRequest());
        }
        verify(revisaoService, never()).registrarRevisao(any(), any(), any(), any());
    }

    @Test
    void registrar_camposObrigatoriosAusentes_retorna400() throws Exception {
        mockMvc.perform(post("/revisoes").with(como(TipoPerfil.REVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RevisaoRequest(null, true, null))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/revisoes").with(como(TipoPerfil.REVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RevisaoRequest(UUID.randomUUID(), null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_conteudoForaDeRevisao_retorna409() throws Exception {
        doThrow(new ConflitoException("Conteúdo não está em revisão"))
                .when(revisaoService).registrarRevisao(any(), any(), any(), any());

        mockMvc.perform(post("/revisoes").with(como(TipoPerfil.REVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RevisaoRequest(UUID.randomUUID(), true, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void registrar_conteudoInexistente_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Conteúdo não encontrado"))
                .when(revisaoService).registrarRevisao(any(), any(), any(), any());

        mockMvc.perform(post("/revisoes").with(como(TipoPerfil.REVISOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RevisaoRequest(UUID.randomUUID(), true, null))))
                .andExpect(status().isNotFound());
    }
}
