package com.dtacomp.schema.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dtacomp.schema.classes.Conteudo;
import com.dtacomp.schema.classes.UsuarioConteudo;
import com.dtacomp.schema.classes.UsuarioConteudo.UsuarioConteudoPK;

public interface UsuarioConteudoRepository extends JpaRepository<UsuarioConteudo, UsuarioConteudoPK> {
    // autores de uma publicacao, em ordem de autoria
    List<UsuarioConteudo> findByConteudo_IdOrderByIdxPosicaoAsc(UUID idConteudo);

    // publicacoes de um autor
    @Query("SELECT uc.conteudo FROM UsuarioConteudo uc WHERE uc.usuario.id = :idUsuario")
    List<Conteudo> findConteudosPorAutor(@Param("idUsuario") UUID idUsuario);
}
