package com.dtacomp.api.schema.classes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class ImagemTest {
    @Test 
    public void deveConstruirImagemComBuilder() {
        UUID idConteudo = UUID.randomUUID();
        UUID idImagem = UUID.randomUUID();
        Conteudo conteudo = Conteudo.builder().id(idConteudo).titulo("Teste").build();
        ConteudoTexto conteudo_texto = ConteudoTexto.builder().idConteudo(idConteudo).conteudo(conteudo).corpoHtml("Lorem Ipsom Dolor").build();
        Imagem imagem = Imagem.builder().id(idImagem).conteudoTexto(conteudo_texto).urlImage("https://exemplo.com/image.png").legenda("Teste").build();
        
        assertThat(imagem.getId()).isEqualTo(idImagem);
        assertThat(imagem.getConteudoTexto()).isEqualTo(conteudo_texto);
        assertThat(imagem.getUrlImage()).isEqualTo("https://exemplo.com/image.png");
        assertThat(imagem.getLegenda()).isEqualTo("Teste");
    }
}
