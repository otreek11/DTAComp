package com.dtacomp.api.schema.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.api.schema.classes.UsuarioConteudo;

@DataJpaTest 
public class UsuarioConteudoRepositoryTest {
    @Autowired
    private UsuarioConteudoRepository usuarioConteudoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired 
    private ConteudoRepository conteudoRepository;

    @Test 
    public void deveEncontrarAutoresDoConteudoEmOrdem() {
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Artigo").tipo(TipoConteudo.TEXTO).build());
        Usuario autor1 = usuarioRepository.save(Usuario.builder().nome("Autor 1").email("a1@teste.com").perfil(TipoPerfil.MEMBRO).build());
        Usuario autor2 = usuarioRepository.save(Usuario.builder().nome("Autor 2").email("a2@teste.com").perfil(TipoPerfil.MEMBRO).build());

        usuarioConteudoRepository.save(criarUsuarioConteudo(autor2, conteudo, 1));
        usuarioConteudoRepository.save(criarUsuarioConteudo(autor1, conteudo, 0));

        List<UsuarioConteudo> autores = usuarioConteudoRepository.findByConteudo_IdOrderByIdxPosicaoAsc(conteudo.getId());
        
        assertThat(autores).hasSize(2);
        assertThat(autores.get(0).getUsuario()).isEqualTo(autor1);
        assertThat(autores.get(1).getUsuario()).isEqualTo(autor2);
    }

    @Test 
    public void deveEncontrarConteudoPorAutor() {
        Usuario autor = usuarioRepository.save(Usuario.builder().nome("Autor").email("autor@teste.com").perfil(TipoPerfil.MEMBRO).build());
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Artigo do Autor").tipo(TipoConteudo.TEXTO).build());
        usuarioConteudoRepository.save(criarUsuarioConteudo(autor, conteudo, 0));

        List<Conteudo> conteudos = usuarioConteudoRepository.findConteudosPorAutor(autor.getId());

        assertThat(conteudos).containsExactly(conteudo);
    }

    private UsuarioConteudo criarUsuarioConteudo(Usuario usuario, Conteudo conteudo, int posicao) {
        UsuarioConteudo.UsuarioConteudoPK pk = new UsuarioConteudo.UsuarioConteudoPK();
        pk.setIdUsuario(usuario.getId());
        pk.setIdConteudo(conteudo.getId());
        return UsuarioConteudo.builder().pk(pk).usuario(usuario).conteudo(conteudo).idxPosicao(posicao).build();
    }
}
