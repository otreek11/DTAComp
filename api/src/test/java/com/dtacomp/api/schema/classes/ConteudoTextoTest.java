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
        ConteudoTexto conteudo_texto = ConteudoTexto.builder().idConteudo(idConteudo).conteudo(conteudo).corpoHtml("Lorem Ipsom Dolor").build();

        assertThat(conteudo_texto.getIdConteudo()).isEqualTo(idConteudo);
        assertThat(conteudo_texto.getConteudo()).isEqualTo(conteudo);
        assertThat(conteudo_texto.getCorpoHtml()).isEqualTo("Lorem Ipsom Dolor");
    }
}
