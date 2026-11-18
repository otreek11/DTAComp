package com.dtacomp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.dtacomp.api.ApiApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Front hospedado em outro domínio (Vercel) chamando a API (Render). */
@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.cors.allowed-origins=https://dtacomp.exemplo.com, https://*.vercel.app")
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightDeOrigemPermitidaRetornaCabecalhosCors() throws Exception {
        mockMvc.perform(options("/conteudos")
                        .header("Origin", "https://dtacomp.exemplo.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://dtacomp.exemplo.com"));
    }

    @Test
    void preflightAceitaPadraoDeOrigem() throws Exception {
        mockMvc.perform(options("/tags")
                        .header("Origin", "https://dtacomp-git-feature.vercel.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://dtacomp-git-feature.vercel.app"));
    }

    @Test
    void preflightDeOrigemDesconhecidaEhNegado() throws Exception {
        mockMvc.perform(options("/conteudos")
                        .header("Origin", "https://malicioso.exemplo.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
