package com.dtacomp.api.schema.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;

import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

public class UsuarioTest {
    @Test
    public void deveConstruirUsuarioComBuilder() {
        Usuario usuario = Usuario.builder().nome("Teste").email("teste@teste.com").perfil(TipoPerfil.MEMBRO).build();
        assertEquals("Teste", usuario.getNome());
        assertEquals("teste@teste.com", usuario.getEmail());
        assertEquals("membro", usuario.getPerfil().getDescricao());
    } 

    @ParameterizedTest 
    @MethodSource("proverDadosPerfil")
    public void tipoPerfilAdminDeveTerNumEDescricaoCorretos(int num_esperado, String descricao_esperada, TipoPerfil tipo_perfil) {
        int num = tipo_perfil.getNum();
        String descricao = tipo_perfil.getDescricao();

        assertThat(num).isEqualTo(num_esperado);
        assertThat(descricao).isEqualTo(descricao_esperada);
    }

    private static Stream<Arguments> proverDadosPerfil() {
        return Stream.of(
            Arguments.of(0, "membro", TipoPerfil.MEMBRO),
            Arguments.of(1, "revisor", TipoPerfil.REVISOR),
            Arguments.of(2, "admin", TipoPerfil.ADMIN)
        );
    }
}
