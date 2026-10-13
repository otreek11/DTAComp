package com.dtacomp.controller;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dtacomp.api.ApiApplication;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.service.armazenamento.ArmazenamentoService;

import static com.dtacomp.controller.TestUsuarios.como;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.storage.max-image-size=64")
class ArquivoControllerTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    private static final byte[] WEBP = {0x52, 0x49, 0x46, 0x46, 0x00, 0x00};

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArmazenamentoService armazenamento;

    private MockMultipartFile arquivo(String contentType, byte[] bytes) {
        return new MockMultipartFile("arquivo", "img", contentType, bytes);
    }

    // ---------- POST ----------

    @Test
    void upload_semToken_retorna401() throws Exception {
        mockMvc.perform(multipart("/arquivos/imagens").file(arquivo("image/png", PNG)))
                .andExpect(status().isUnauthorized());

        verify(armazenamento, never()).salvar(any(), any(), any());
    }

    @Test
    void upload_formatosValidos_retorna200ComUrl() throws Exception {
        record Caso(String contentType, byte[] bytes, String extensao) {}
        for (Caso c : new Caso[] {
                new Caso("image/png", PNG, "png"),
                new Caso("image/jpeg", JPEG, "jpg"),
                new Caso("image/webp", WEBP, "webp")}) {
            String nome = UUID.randomUUID() + "." + c.extensao();
            when(armazenamento.salvar(eq("imagens"), eq(c.extensao()), any())).thenReturn("imagens/" + nome);

            mockMvc.perform(multipart("/arquivos/imagens").file(arquivo(c.contentType(), c.bytes()))
                            .with(como(TipoPerfil.MEMBRO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url").value("/arquivos/imagens/" + nome));
        }
    }

    @Test
    void upload_arquivoVazio_retorna400() throws Exception {
        mockMvc.perform(multipart("/arquivos/imagens").file(arquivo("image/png", new byte[0]))
                        .with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_tipoNaoPermitido_retorna400() throws Exception {
        for (String tipo : new String[] {"image/gif", "application/pdf"}) {
            mockMvc.perform(multipart("/arquivos/imagens").file(arquivo(tipo, PNG))
                            .with(como(TipoPerfil.MEMBRO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void upload_conteudoNaoCorrespondeAoTipo_retorna400() throws Exception {
        byte[] lixo = "nao sou imagem".getBytes(StandardCharsets.UTF_8);
        for (String tipo : new String[] {"image/png", "image/jpeg", "image/webp"}) {
            mockMvc.perform(multipart("/arquivos/imagens").file(arquivo(tipo, lixo))
                            .with(como(TipoPerfil.MEMBRO)))
                    .andExpect(status().isBadRequest());
        }
        verify(armazenamento, never()).salvar(any(), any(), any());
    }

    @Test
    void upload_acimaDoTamanhoMaximo_retorna400() throws Exception {
        byte[] grande = new byte[200];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);

        mockMvc.perform(multipart("/arquivos/imagens").file(arquivo("image/png", grande))
                        .with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_semParteArquivo_retorna400() throws Exception {
        mockMvc.perform(multipart("/arquivos/imagens").with(como(TipoPerfil.MEMBRO)))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET ----------

    @Test
    void get_semToken_eNomeValido_retornaImagemPublica() throws Exception {
        String nome = UUID.randomUUID() + ".png";
        when(armazenamento.existe("imagens/" + nome)).thenReturn(true);
        when(armazenamento.carregar("imagens/" + nome)).thenReturn(new ByteArrayResource(PNG));

        mockMvc.perform(get("/arquivos/imagens/{nome}", nome))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void get_contentTypePorExtensao() throws Exception {
        String[][] casos = {{"jpg", "image/jpeg"}, {"jpeg", "image/jpeg"}, {"webp", "image/webp"}};
        for (String[] c : casos) {
            String nome = UUID.randomUUID() + "." + c[0];
            when(armazenamento.existe("imagens/" + nome)).thenReturn(true);
            when(armazenamento.carregar("imagens/" + nome)).thenReturn(new ByteArrayResource(JPEG));

            mockMvc.perform(get("/arquivos/imagens/{nome}", nome))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(c[1]));
        }
    }

    @Test
    void get_nomeForaDoPadrao_retorna403() throws Exception {
        for (String nome : new String[] {"a.txt", "segredo.png", UUID.randomUUID() + ".gif"}) {
            mockMvc.perform(get("/arquivos/imagens/{nome}", nome))
                    .andExpect(status().isForbidden());
        }
        verify(armazenamento, never()).existe(any());
    }

    @Test
    void get_inexistente_retorna404() throws Exception {
        String nome = UUID.randomUUID() + ".png";
        when(armazenamento.existe("imagens/" + nome)).thenReturn(false);

        mockMvc.perform(get("/arquivos/imagens/{nome}", nome))
                .andExpect(status().isNotFound());
    }
}
