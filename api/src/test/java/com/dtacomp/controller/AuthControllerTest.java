package com.dtacomp.controller;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.repositories.InstituicaoRepository;
import com.dtacomp.dto.auth.LoginRequest;
import com.dtacomp.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dtacomp.api.ApiApplication;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    private Instituicao instituicao;

    @BeforeEach
    void setUp() {
        instituicao = instituicaoRepository.save(
                Instituicao.builder()
                        .nome("Universidade Federal")
                        .sigla("UF")
                        .build()
        );
    }

    @Test
    void registerAndLoginFlow() throws Exception {
        String uniqueEmail = "user_" + UUID.randomUUID() + "@teste.com";

        RegisterRequest registerReq = new RegisterRequest(
                "Novo Usuario",
                uniqueEmail,
                "senhaForte123",
                instituicao.getId()
        );

        // 1. Registro com sucesso (201)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.perfil").value("membro"));

        // 2. Registro duplicado -> Conflito (409)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isConflict());

        // 3. Login com sucesso (200)
        LoginRequest loginReq = new LoginRequest(uniqueEmail, "senhaForte123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value(uniqueEmail));

        // 4. Login com senha errada (401)
        LoginRequest badLogin = new LoginRequest(uniqueEmail, "senhaErrada");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicHelloEndpointIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }
}
