package com.dtacomp.schema.classes;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ConteudoPDFTest {
    @Test 
    public void deveConstruirComBuilder() {
        UUID idConteudo = UUID.randomUUID();
        Conteudo conteudo = Conteudo.builder().id(idConteudo).titulo("Teste").build();
        ConteudoPDF conteudo_pdf = ConteudoPDF.builder().idConteudo(idConteudo).conteudo(conteudo).urlArquivo("https://exemplo.com/arquivo.pdf").tamanhoBytes(1024f).build();

        assertThat(conteudo_pdf.getIdConteudo()).isEqualTo(idConteudo);
        assertThat(conteudo_pdf.getConteudo()).isEqualTo(conteudo);
        assertThat(conteudo_pdf.getUrlArquivo()).isEqualTo("https://exemplo.com/arquivo.pdf");
        assertThat(conteudo_pdf.getTamanhoBytes()).isEqualTo(1024f);
    }
}
