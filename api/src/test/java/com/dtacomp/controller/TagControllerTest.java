package com.dtacomp.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dtacomp.api.ApiApplication;
import com.dtacomp.api.schema.classes.Tag;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.exception.ConflitoException;
import com.dtacomp.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.dtacomp.controller.TestUsuarios.como;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String nome(String valor) throws Exception {
        return objectMapper.writeValueAsString(Map.of("nome", valor));
    }

    private Tag tag(String nome) {
        return Tag.builder().id(UUID.randomUUID()).nome(nome).build();
    }

    // ---------- GET /tags ----------

    @Test
    void listar_publicoSemToken_retornaTagsNaOrdemDoServico() throws Exception {
        when(tagService.listarOrdenadasPorNome()).thenReturn(List.of(tag("java"), tag("web")));

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("java"))
                .andExpect(jsonPath("$[1].nome").value("web"))
                .andExpect(jsonPath("$[0].id").isString());
    }

    @Test
    void listar_semTags_retornaListaVazia() throws Exception {
        when(tagService.listarOrdenadasPorNome()).thenReturn(List.of());

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---------- POST /tags ----------

    @Test
    void criar_semToken_retorna401() throws Exception {
        mockMvc.perform(post("/tags").contentType(MediaType.APPLICATION_JSON).content(nome("java")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criar_naoAdmin_retorna403() throws Exception {
        for (TipoPerfil perfil : new TipoPerfil[] {TipoPerfil.MEMBRO, TipoPerfil.REVISOR}) {
            mockMvc.perform(post("/tags").with(como(perfil))
                            .contentType(MediaType.APPLICATION_JSON).content(nome("java")))
                    .andExpect(status().isForbidden());
        }
        verify(tagService, never()).criar(any());
    }

    @Test
    void criar_admin_retorna201() throws Exception {
        when(tagService.criar("java")).thenReturn(tag("java"));

        mockMvc.perform(post("/tags").with(como(TipoPerfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(nome("java")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("java"));
    }

    @Test
    void criar_nomeInvalido_retorna400() throws Exception {
        for (String invalido : new String[] {"", "   ", "a,b", "a\u0001b", "x".repeat(256)}) {
            mockMvc.perform(post("/tags").with(como(TipoPerfil.ADMIN))
                            .contentType(MediaType.APPLICATION_JSON).content(nome(invalido)))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(post("/tags").with(como(TipoPerfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(tagService, never()).criar(any());
    }

    @Test
    void criar_nomeDuplicado_retorna409() throws Exception {
        when(tagService.criar("java")).thenThrow(new ConflitoException("Já existe uma tag com o nome: java"));

        mockMvc.perform(post("/tags").with(como(TipoPerfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(nome("java")))
                .andExpect(status().isConflict());
    }

    // ---------- PUT /tags/{id} ----------

    @Test
    void renomear_semToken_retorna401() throws Exception {
        mockMvc.perform(put("/tags/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(nome("novo")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void renomear_naoAdmin_retorna403() throws Exception {
        mockMvc.perform(put("/tags/{id}", UUID.randomUUID()).with(como(TipoPerfil.MEMBRO))
                        .contentType(MediaType.APPLICATION_JSON).content(nome("novo")))
                .andExpect(status().isForbidden());

        verify(tagService, never()).renomear(any(), any());
    }

    @Test
    void renomear_admin_retorna200() throws Exception {
        Tag renomeada = tag("novo");
        when(tagService.renomear(renomeada.getId(), "novo")).thenReturn(renomeada);

        mockMvc.perform(put("/tags/{id}", renomeada.getId()).with(como(TipoPerfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(nome("novo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(renomeada.getId().toString()))
                .andExpect(jsonPath("$.nome").value("novo"));
    }

    @Test
    void renomear_corpoOuIdInvalidos_retorna400() throws Exception {
        mockMvc.perform(put("/tags/{id}", UUID.randomUUID()).with(como(TipoPerfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(nome("")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/tags/{id}", "nao-e-uuid").with(como(TipoPerfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(nome("novo")))
                .andExpect(status().isBadRequest());

        verify(tagService, never()).renomear(any(), any());
    }

    @Test
    void renomear_inexistente_retorna404() throws Exception {
        UUID id = UUID.randomUUID();
        when(tagService.renomear(id, "novo")).thenThrow(new IllegalArgumentException("Tag não encontrada"));

        mockMvc.perform(put("/tags/{id}", id).with(como(TipoPerfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(nome("novo")))
                .andExpect(status().isNotFound());
    }

    @Test
    void renomear_nomeJaExiste_retorna409() throws Exception {
        UUID id = UUID.randomUUID();
        when(tagService.renomear(id, "novo")).thenThrow(new ConflitoException("Já existe"));

        mockMvc.perform(put("/tags/{id}", id).with(como(TipoPerfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(nome("novo")))
                .andExpect(status().isConflict());
    }

    // ---------- DELETE /tags/{id} ----------

    @Test
    void excluir_semToken_retorna401() throws Exception {
        mockMvc.perform(delete("/tags/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void excluir_naoAdmin_retorna403() throws Exception {
        mockMvc.perform(delete("/tags/{id}", UUID.randomUUID()).with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isForbidden());

        verify(tagService, never()).excluir(any());
    }

    @Test
    void excluir_admin_retorna204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/tags/{id}", id).with(como(TipoPerfil.ADMIN)))
                .andExpect(status().isNoContent());

        verify(tagService).excluir(id);
    }

    @Test
    void excluir_inexistente_retorna404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalArgumentException("Tag não encontrada")).when(tagService).excluir(id);

        mockMvc.perform(delete("/tags/{id}", id).with(como(TipoPerfil.ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluir_tagEmUso_retorna409() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ConflitoException("A tag está em uso")).when(tagService).excluir(id);

        mockMvc.perform(delete("/tags/{id}", id).with(como(TipoPerfil.ADMIN)))
                .andExpect(status().isConflict());
    }
}
