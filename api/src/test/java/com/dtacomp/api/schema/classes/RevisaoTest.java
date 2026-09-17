package com.dtacomp.api.schema.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;

public class RevisaoTest {
    @Test 
    public void deveConstruirRevisaoComBuilder() {
        Usuario usuario = Usuario.builder().nome("Teste").email("teste@teste.com").perfil(TipoPerfil.REVISOR).build();
        Conteudo conteudo = Conteudo.builder().titulo("Teste").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build();
        Revisao revisao = Revisao.builder().conteudo(conteudo).revisor(usuario).status(conteudo.getStatus()).parecerHumano("Dá pra melhorar").build();

        assertEquals(conteudo, revisao.getConteudo());
        assertEquals(usuario, revisao.getRevisor());
        assertEquals(conteudo.getStatus(), revisao.getStatus());
        assertEquals("Dá pra melhorar", revisao.getParecerHumano());
    }
}
