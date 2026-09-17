package com.dtacomp.api.schema.classes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.ConteudoTexto;

public class ConteudoTextoTest {
    @Test 
    public void deveConstruirComBuilder() {
        UUID idConteudo = UUID.randomUUID();
        Conteudo conteudo = Conteudo.builder().id(idConteudo).titulo("Teste").build();
        ConteudoTexto conteudo_pdf = ConteudoTexto.builder().idConteudo(idConteudo).conteudo(conteudo).corpoHtml("Lorem Ipsom Dolor").build();

        assertThat(conteudo_pdf.getIdConteudo()).isEqualTo(idConteudo);
        assertThat(conteudo_pdf.getConteudo()).isEqualTo(conteudo);
        assertThat(conteudo_pdf.getCorpoHtml()).isEqualTo("Lorem Ipsom Dolor");
    }
}
