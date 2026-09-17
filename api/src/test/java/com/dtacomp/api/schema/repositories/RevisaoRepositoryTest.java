package com.dtacomp.api.schema.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.classes.Revisao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;

@DataJpaTest 
public class RevisaoRepositoryTest {
    @Autowired
    private RevisaoRepository revisaoRepository;
    @Autowired 
    private UsuarioRepository usuarioRepository;
    @Autowired 
    private ConteudoRepository conteudoRepository;

    @Test 
    public void deveEncontrarRevisaoPorRevisorId() {
        Usuario usuario = usuarioRepository.save(Usuario.builder().nome("Teste").email("teste@teste.com").perfil(TipoPerfil.REVISOR).build());
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Teste").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        Revisao revisao = revisaoRepository.save(Revisao.builder().conteudo(conteudo).revisor(usuario).status(conteudo.getStatus()).parecerHumano("Dá pra melhorar").build());

        List<Revisao> revisores = revisaoRepository.findByRevisor_Id(usuario.getId());

        assertThat(revisores).contains(revisao);
        assertThat(revisores.get(0).getStatus()).isEqualTo(StatusConteudo.REVISAO);
        assertThat(revisores.get(0).getConteudo()).isEqualTo(conteudo);
        assertThat(revisores.get(0).getRevisor()).isEqualTo(usuario);
        assertThat(revisores.get(0).getParecerHumano()).isEqualTo("Dá pra melhorar");
    }

    @Test 
    public void deveEncontrarRevisaoPorConteudoId() {
        Usuario usuario = usuarioRepository.save(Usuario.builder().nome("Teste").email("teste@teste.com").perfil(TipoPerfil.REVISOR).build());
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Teste").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        Revisao revisao = revisaoRepository.save(Revisao.builder().conteudo(conteudo).revisor(usuario).status(conteudo.getStatus()).parecerHumano("Dá pra melhorar").build());

        List<Revisao> revisores = revisaoRepository.findByConteudo_Id(conteudo.getId());

        assertThat(revisores).contains(revisao);
        assertThat(revisores.get(0).getStatus()).isEqualTo(StatusConteudo.REVISAO);
        assertThat(revisores.get(0).getConteudo()).isEqualTo(conteudo);
        assertThat(revisores.get(0).getRevisor()).isEqualTo(usuario);
        assertThat(revisores.get(0).getParecerHumano()).isEqualTo("Dá pra melhorar");
    }

    @Test 
    public void deveEncontrarRevisaoPorRevisorIdEStatus() {
        Usuario usuario = usuarioRepository.save(Usuario.builder().nome("Teste").email("teste@teste.com").perfil(TipoPerfil.REVISOR).build());
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Teste").resumo("resumo").tipo(TipoConteudo.TEXTO).status(StatusConteudo.REVISAO).data(LocalDate.of(2026,9,16)).build());
        Revisao revisao = revisaoRepository.save(Revisao.builder().conteudo(conteudo).revisor(usuario).status(conteudo.getStatus()).parecerHumano("Dá pra melhorar").build());

        List<Revisao> revisores = revisaoRepository.findByRevisor_IdAndStatus(usuario.getId(), StatusConteudo.REVISAO);

        assertThat(revisores).contains(revisao);
        assertThat(revisores.get(0).getStatus()).isEqualTo(StatusConteudo.REVISAO);
        assertThat(revisores.get(0).getConteudo()).isEqualTo(conteudo);
        assertThat(revisores.get(0).getRevisor()).isEqualTo(usuario);
        assertThat(revisores.get(0).getParecerHumano()).isEqualTo("Dá pra melhorar");
    }
}
