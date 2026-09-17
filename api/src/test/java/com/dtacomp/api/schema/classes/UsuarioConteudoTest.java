package com.dtacomp.api.schema.classes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.dtacomp.api.schema.classes.UsuarioConteudo.UsuarioConteudoPK;

public class UsuarioConteudoTest {
    @Test 
    public void deveConstruirComBuilder() {
        UUID idUsuario = UUID.randomUUID();
        UUID idConteudo = UUID.randomUUID();
        Usuario usuario = Usuario.builder().id(idUsuario).nome("Autor").build();
        Conteudo conteudo = Conteudo.builder().id(idConteudo).titulo("Artigo").build();
        UsuarioConteudoPK pk = new UsuarioConteudoPK();
        pk.setIdUsuario(idUsuario);
        pk.setIdConteudo(idConteudo);

        UsuarioConteudo usuarioConteudo = UsuarioConteudo.builder().pk(pk).usuario(usuario).conteudo(conteudo).idxPosicao(0).build();

        assertThat(usuarioConteudo.getPk().getIdUsuario()).isEqualTo(idUsuario);
        assertThat(usuarioConteudo.getPk().getIdConteudo()).isEqualTo(idConteudo);
        assertThat(usuarioConteudo.getUsuario()).isEqualTo(usuario);
        assertThat(usuarioConteudo.getConteudo()).isEqualTo(conteudo);
        assertThat(usuarioConteudo.getIdxPosicao()).isEqualTo(0);
    }
}
