package com.dtacomp.api.schema.repositories;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dtacomp.api.schema.classes.Instituicao;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;
import com.dtacomp.api.schema.repositories.IntituicaoRepository;
import com.dtacomp.api.schema.repositories.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
public class UsuarioRepositoryTest {
    @Autowired 
    private UsuarioRepository usuarioRepository;
    @Autowired
    private IntituicaoRepository instituicaoRepository;

    @Test 
    public void deveEncontrarUsuarioPorEmail() {
        //Arrange
        usuarioRepository.save(Usuario.builder().email("teste@teste.com").nome("Teste").perfil(Usuario.TipoPerfil.MEMBRO).build());
        
        //Act
        Optional<Usuario> encontrado = usuarioRepository.findByEmail("teste@teste.com");

        //Assert
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Teste");
    } 

    @Test
    public void existsByEmailDeveRetornarFalseQuandoNaoExiste() {
        //Act
        boolean existe = usuarioRepository.existsByEmail("naoExiste@teste.com");

        //Assert
        assertThat(existe).isFalse();
    }

    @Test 
    public void deveEncontrarUsuariosPorPerfil() {
        //Arrange
        usuarioRepository.save(Usuario.builder().email("membroa@teste.com").nome("Membro A").perfil(TipoPerfil.MEMBRO).build());
        usuarioRepository.save(Usuario.builder().email("membrob@teste.com").nome("Membro B").perfil(TipoPerfil.MEMBRO).build());
        usuarioRepository.save(Usuario.builder().email("revisor@teste.com").nome("Revisor").perfil(TipoPerfil.REVISOR).build());
        
        //Act
        List<Usuario> membros = usuarioRepository.findByPerfil(TipoPerfil.MEMBRO);

        //Assert
        assertThat(membros).hasSize(2).extracting(Usuario::getEmail).contains("membroa@teste.com");
    }

    @Test 
    public void deveEncontrarUsuariosPorInstituicao() {
        //Arrange
        Instituicao instituicao = instituicaoRepository.save(Instituicao.builder().nome("Intituicao Teste").sigla("IT").build());
        usuarioRepository.save(Usuario.builder().nome("Teste").email("teste@teste.com").perfil(TipoPerfil.MEMBRO).instituicao(instituicao).build());
        usuarioRepository.save(Usuario.builder().nome("Outro").email("outro@teste.com").perfil(TipoPerfil.MEMBRO).build());
        //Act
        List<Usuario> resultado = usuarioRepository.findByInstituicao_Id(instituicao.getId());

        //Assert
        assertThat(resultado).hasSize(1).extracting(Usuario::getEmail).containsExactly("teste@teste.com");

    }

}
