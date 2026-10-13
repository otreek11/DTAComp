package com.dtacomp.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.dto.usuario.MeusDadosResponse;
import com.dtacomp.exception.ConflitoException;
import com.dtacomp.exception.RequisicaoInvalidaException;
import com.dtacomp.service.ContaService;
import com.dtacomp.service.ConteudoConsultaService;
import com.dtacomp.service.ConteudoFiltro;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContaService contaService;

    @MockitoBean
    private ConteudoConsultaService consultaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario logado;
    private Instituicao instituicao;

    @BeforeEach
    void setUp() {
        logado = usuario(TipoPerfil.MEMBRO);
        instituicao = Instituicao.builder().id(UUID.randomUUID()).nome("Universidade Federal").sigla("UF").build();
        logado.setInstituicao(instituicao);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    // ---------- GET /usuarios/me/conteudos ----------

    @Test
    void meusConteudos_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/usuarios/me/conteudos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meusConteudos_filtraPeloUsuarioLogadoEInterpretaStatus() throws Exception {
        when(consultaService.pesquisar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/usuarios/me/conteudos").param("status", "Devolvido").with(como(logado)))
                .andExpect(status().isOk());

        ArgumentCaptor<ConteudoFiltro> captor = ArgumentCaptor.forClass(ConteudoFiltro.class);
        verify(consultaService).pesquisar(captor.capture(), any());
        assertThat(captor.getValue().idAutor()).isEqualTo(logado.getId());
        assertThat(captor.getValue().status()).isEqualTo(StatusConteudo.DEVOLVIDO);
    }

    @Test
    void meusConteudos_semStatus_naoFiltraPorStatus() throws Exception {
        when(consultaService.pesquisar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/usuarios/me/conteudos").with(como(logado)))
                .andExpect(status().isOk());

        ArgumentCaptor<ConteudoFiltro> captor = ArgumentCaptor.forClass(ConteudoFiltro.class);
        verify(consultaService).pesquisar(captor.capture(), any());
        assertThat(captor.getValue().status()).isNull();
    }

    @Test
    void meusConteudos_limitaTamanhoDaPagina() throws Exception {
        when(consultaService.pesquisar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/usuarios/me/conteudos").param("size", "999").with(como(logado)))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(consultaService).pesquisar(any(), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    void meusConteudos_statusEOrdenacaoInvalidos_retorna400() throws Exception {
        mockMvc.perform(get("/usuarios/me/conteudos").param("status", "xyz").with(como(logado)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/usuarios/me/conteudos").param("sort", "senha").with(como(logado)))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT /usuarios/me ----------

    @Test
    void atualizarPerfil_semToken_retorna401() throws Exception {
        mockMvc.perform(put("/usuarios/me").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Novo", "idInstituicao", instituicao.getId()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void atualizarPerfil_ok_retorna200() throws Exception {
        Usuario atualizado = usuario(TipoPerfil.MEMBRO);
        atualizado.setNome("Novo Nome");
        atualizado.setInstituicao(instituicao);
        when(contaService.atualizarPerfil(logado.getId(), "Novo Nome", instituicao.getId())).thenReturn(atualizado);

        mockMvc.perform(put("/usuarios/me").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Novo Nome", "idInstituicao", instituicao.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.instituicao.sigla").value("UF"));
    }

    @Test
    void atualizarPerfil_corpoInvalido_retorna400() throws Exception {
        mockMvc.perform(put("/usuarios/me").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", " ", "idInstituicao", instituicao.getId()))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/usuarios/me").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Novo"))))
                .andExpect(status().isBadRequest());

        verify(contaService, never()).atualizarPerfil(any(), any(), any());
    }

    @Test
    void atualizarPerfil_instituicaoInexistente_retorna400() throws Exception {
        when(contaService.atualizarPerfil(any(), any(), any()))
                .thenThrow(new RequisicaoInvalidaException("Instituição não encontrada"));

        mockMvc.perform(put("/usuarios/me").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Novo", "idInstituicao", UUID.randomUUID()))))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT /usuarios/me/senha ----------

    @Test
    void alterarSenha_semToken_retorna401() throws Exception {
        mockMvc.perform(put("/usuarios/me/senha").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senhaAtual", "antiga123", "novaSenha", "novaSenha123"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void alterarSenha_ok_retorna204() throws Exception {
        mockMvc.perform(put("/usuarios/me/senha").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senhaAtual", "antiga123", "novaSenha", "novaSenha123"))))
                .andExpect(status().isNoContent());

        verify(contaService).alterarSenha(logado.getId(), "antiga123", "novaSenha123");
    }

    @Test
    void alterarSenha_novaSenhaForaDosLimites_retorna400() throws Exception {
        for (String nova : new String[] {"", "curta", "x".repeat(73)}) {
            mockMvc.perform(put("/usuarios/me/senha").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("senhaAtual", "antiga123", "novaSenha", nova))))
                    .andExpect(status().isBadRequest());
        }
        verify(contaService, never()).alterarSenha(any(), any(), any());
    }

    @Test
    void alterarSenha_senhaAtualIncorreta_retorna400() throws Exception {
        doThrow(new RequisicaoInvalidaException("Senha atual incorreta"))
                .when(contaService).alterarSenha(any(), any(), any());

        mockMvc.perform(put("/usuarios/me/senha").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senhaAtual", "errada123", "novaSenha", "novaSenha123"))))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /usuarios/me/dados ----------

    @Test
    void meusDados_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/usuarios/me/dados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meusDados_autenticado_retorna200() throws Exception {
        var conta = MeusDadosResponse.Conta.from(logado);
        when(contaService.exportarDados(logado.getId()))
                .thenReturn(new MeusDadosResponse(conta, List.of(), List.of(), Instant.now()));

        mockMvc.perform(get("/usuarios/me/dados").with(como(logado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conta.email").value(logado.getEmail()))
                .andExpect(jsonPath("$.conteudos").isArray());
    }

    // ---------- DELETE /usuarios/me ----------

    @Test
    void excluirConta_semToken_retorna401() throws Exception {
        mockMvc.perform(delete("/usuarios/me").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senha", "senha123"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void excluirConta_ok_retorna204() throws Exception {
        mockMvc.perform(delete("/usuarios/me").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senha", "senha123"))))
                .andExpect(status().isNoContent());

        verify(contaService).excluirConta(logado.getId(), "senha123");
    }

    @Test
    void excluirConta_senhaEmBranco_retorna400() throws Exception {
        mockMvc.perform(delete("/usuarios/me").with(como(logado)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senha", ""))))
                .andExpect(status().isBadRequest());

        verify(contaService, never()).excluirConta(any(), any());
    }

    @Test
    void excluirConta_unicoAdmin_retorna409() throws Exception {
        doThrow(new ConflitoException("Você é o único administrador"))
                .when(contaService).excluirConta(any(), eq("senha123"));

        mockMvc.perform(delete("/usuarios/me").with(como(TipoPerfil.ADMIN)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senha", "senha123"))))
                .andExpect(status().isConflict());
    }

    // ---------- GET /usuarios/{id}/perfil ----------

    @Test
    void perfilPublico_semToken_retorna200SemEmail() throws Exception {
        UUID id = logado.getId();
        when(contaService.buscarPerfilPublico(id)).thenReturn(logado);

        mockMvc.perform(get("/usuarios/{id}/perfil", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Fulano"))
                .andExpect(jsonPath("$.instituicao.sigla").value("UF"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void perfilPublico_idMalFormado_retorna400() throws Exception {
        mockMvc.perform(get("/usuarios/{id}/perfil", "nao-e-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void perfilPublico_inexistente_retorna404() throws Exception {
        UUID id = UUID.randomUUID();
        when(contaService.buscarPerfilPublico(id)).thenThrow(new IllegalArgumentException("Usuário não encontrado"));

        mockMvc.perform(get("/usuarios/{id}/perfil", id))
                .andExpect(status().isNotFound());
    }
}
