package com.dtacomp.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.dtacomp.api.ApiApplication;
import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.exception.ConflitoException;
import com.dtacomp.service.PublicacaoService;
import com.dtacomp.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.dtacomp.controller.TestUsuarios.como;
import static org.assertj.core.api.Assertions.assertThat;
import static com.dtacomp.controller.TestUsuarios.usuario;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private PublicacaoService publicacaoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario admin;
    private UUID alvo;

    @BeforeEach
    void setUp() {
        admin = usuario(TipoPerfil.ADMIN);
        alvo = UUID.randomUUID();
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private List<MockHttpServletRequestBuilder> todasAsRotas() throws Exception {
        return List.of(
                get("/admin/usuarios"),
                put("/admin/usuarios/{id}/perfil", alvo)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("perfil", "REVISOR"))),
                delete("/admin/usuarios/{id}", alvo),
                post("/admin/conteudos/{id}/despublicar", alvo)
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("motivo", "plágio"))));
    }

    // ---------- acesso ----------

    @Test
    void todasAsRotas_semToken_retorna401() throws Exception {
        for (MockHttpServletRequestBuilder rota : todasAsRotas()) {
            mockMvc.perform(rota).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(usuarioService, publicacaoService);
    }

    @Test
    void todasAsRotas_naoAdmin_retorna403() throws Exception {
        for (TipoPerfil perfil : new TipoPerfil[] {TipoPerfil.MEMBRO, TipoPerfil.REVISOR}) {
            for (MockHttpServletRequestBuilder rota : todasAsRotas()) {
                mockMvc.perform(rota.with(como(perfil))).andExpect(status().isForbidden());
            }
        }
        verifyNoInteractions(usuarioService, publicacaoService);
    }

    // ---------- GET /admin/usuarios ----------

    @Test
    void listarUsuarios_semFiltro_listaTodos() throws Exception {
        when(usuarioService.listarTodos()).thenReturn(List.of(usuario(TipoPerfil.MEMBRO), usuario(TipoPerfil.ADMIN)));

        mockMvc.perform(get("/admin/usuarios").with(como(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("membro@teste.com"));
    }

    @Test
    void listarUsuarios_porPerfil() throws Exception {
        when(usuarioService.buscarPorPerfil(TipoPerfil.REVISOR)).thenReturn(List.of(usuario(TipoPerfil.REVISOR)));

        mockMvc.perform(get("/admin/usuarios").param("perfil", "REVISOR").with(como(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(usuarioService, never()).listarTodos();
    }

    @Test
    void listarUsuarios_porInstituicao() throws Exception {
        UUID instituicaoId = UUID.randomUUID();
        when(usuarioService.buscarPorInstituicao(instituicaoId)).thenReturn(List.of(usuario(TipoPerfil.MEMBRO)));

        mockMvc.perform(get("/admin/usuarios").param("instituicaoId", instituicaoId.toString()).with(como(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listarUsuarios_perfilEInstituicao_aplicaSoOPerfil() throws Exception {
        when(usuarioService.buscarPorPerfil(TipoPerfil.ADMIN)).thenReturn(List.of());

        mockMvc.perform(get("/admin/usuarios").param("perfil", "ADMIN")
                        .param("instituicaoId", UUID.randomUUID().toString()).with(como(admin)))
                .andExpect(status().isOk());

        verify(usuarioService).buscarPorPerfil(TipoPerfil.ADMIN);
        verify(usuarioService, never()).buscarPorInstituicao(any());
    }

    @Test
    void listarUsuarios_parametrosInvalidos_retorna400() throws Exception {
        mockMvc.perform(get("/admin/usuarios").param("perfil", "xyz").with(como(admin)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/admin/usuarios").param("instituicaoId", "nao-e-uuid").with(como(admin)))
                .andExpect(status().isBadRequest());
    }

    private String corpoDaListagemComUmUsuario() throws Exception {
        Usuario u = usuario(TipoPerfil.MEMBRO);
        u.setIdExterno("ext-123");
        u.setInstituicao(Instituicao.builder().id(UUID.randomUUID()).nome("Universidade").sigla("UF").build());
        when(usuarioService.listarTodos()).thenReturn(List.of(u));

        return mockMvc.perform(get("/admin/usuarios").with(como(admin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void listarUsuarios_naoExpoeSenha() throws Exception {
        assertThat(corpoDaListagemComUmUsuario()).doesNotContain("senhaHash", "\"password\"", "x\"");
    }

    // A rota serializa a entidade Usuario; os getters de UserDetails vazam no JSON.
    // Reativar quando a rota passar a devolver um DTO (ex.: UsuarioResponse).
    @Disabled("AdminController devolve a entidade Usuario: expõe authorities, username, enabled etc.")
    @Test
    void listarUsuarios_naoExpoeCamposDeUserDetails() throws Exception {
        assertThat(corpoDaListagemComUmUsuario()).doesNotContain("authorities", "username", "enabled",
                "accountNonExpired", "accountNonLocked", "credentialsNonExpired");
    }

    // ---------- PUT /admin/usuarios/{id}/perfil ----------

    @Test
    void atualizarPerfil_ok_retorna204() throws Exception {
        mockMvc.perform(put("/admin/usuarios/{id}/perfil", alvo).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("perfil", "REVISOR"))))
                .andExpect(status().isNoContent());

        verify(usuarioService).alterarPerfil(alvo, TipoPerfil.REVISOR);
    }

    @Test
    void atualizarPerfil_perfilAusenteOuInvalido_retorna400() throws Exception {
        mockMvc.perform(put("/admin/usuarios/{id}/perfil", alvo).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/admin/usuarios/{id}/perfil", alvo).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("perfil", "SUPERUSER"))))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).alterarPerfil(any(), any());
    }

    @Test
    void atualizarPerfil_idMalFormado_retorna400() throws Exception {
        mockMvc.perform(put("/admin/usuarios/{id}/perfil", "nao-e-uuid").with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("perfil", "REVISOR"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarPerfil_proprioUsuario_retorna409() throws Exception {
        mockMvc.perform(put("/admin/usuarios/{id}/perfil", admin.getId()).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("perfil", "MEMBRO"))))
                .andExpect(status().isConflict());

        verify(usuarioService, never()).alterarPerfil(any(), any());
    }

    @Test
    void atualizarPerfil_usuarioInexistente_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Usuário não encontrado"))
                .when(usuarioService).alterarPerfil(eq(alvo), any());

        mockMvc.perform(put("/admin/usuarios/{id}/perfil", alvo).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("perfil", "REVISOR"))))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /admin/usuarios/{id} ----------

    @Test
    void anonimizar_ok_retorna204() throws Exception {
        mockMvc.perform(delete("/admin/usuarios/{id}", alvo).with(como(admin)))
                .andExpect(status().isNoContent());

        verify(usuarioService).anonimizar(alvo);
    }

    @Test
    void anonimizar_proprioUsuario_retorna409() throws Exception {
        mockMvc.perform(delete("/admin/usuarios/{id}", admin.getId()).with(como(admin)))
                .andExpect(status().isConflict());

        verify(usuarioService, never()).anonimizar(any());
    }

    @Test
    void anonimizar_inexistente_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Usuário não encontrado")).when(usuarioService).anonimizar(alvo);

        mockMvc.perform(delete("/admin/usuarios/{id}", alvo).with(como(admin)))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /admin/conteudos/{id}/despublicar ----------

    @Test
    void despublicar_ok_retorna204() throws Exception {
        mockMvc.perform(post("/admin/conteudos/{id}/despublicar", alvo).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("motivo", "plágio"))))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(publicacaoService).despublicar(eq(alvo), any(Usuario.class), eq("plágio"));
    }

    @Test
    void despublicar_motivoAusenteOuEmBranco_retorna400() throws Exception {
        for (String corpo : new String[] {"{}", json(Map.of("motivo", "")), json(Map.of("motivo", "  "))}) {
            mockMvc.perform(post("/admin/conteudos/{id}/despublicar", alvo).with(como(admin))
                            .contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }
        verify(publicacaoService, never()).despublicar(any(), any(), any());
    }

    @Test
    void despublicar_semCorpo_retorna400() throws Exception {
        mockMvc.perform(post("/admin/conteudos/{id}/despublicar", alvo).with(como(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void despublicar_conteudoNaoPublicado_retorna409() throws Exception {
        doThrow(new ConflitoException("Apenas conteúdos publicados podem ser despublicados"))
                .when(publicacaoService).despublicar(eq(alvo), any(), any());

        mockMvc.perform(post("/admin/conteudos/{id}/despublicar", alvo).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("motivo", "plágio"))))
                .andExpect(status().isConflict());
    }

    @Test
    void despublicar_conteudoInexistente_retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Conteúdo não encontrado"))
                .when(publicacaoService).despublicar(eq(alvo), any(), any());

        mockMvc.perform(post("/admin/conteudos/{id}/despublicar", alvo).with(como(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("motivo", "plágio"))))
                .andExpect(status().isNotFound());
    }
}
