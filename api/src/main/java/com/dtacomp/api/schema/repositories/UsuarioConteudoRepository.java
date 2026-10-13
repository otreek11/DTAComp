package com.dtacomp.api.schema.repositories;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.UsuarioConteudo;
import com.dtacomp.api.schema.classes.UsuarioConteudo.UsuarioConteudoPK;

public interface UsuarioConteudoRepository extends JpaRepository<UsuarioConteudo, UsuarioConteudoPK> {
    // autores de uma publicacao, em ordem de autoria
    List<UsuarioConteudo> findByConteudo_IdOrderByIdxPosicaoAsc(UUID idConteudo);

    // autores de varios conteudos de uma vez (evita N+1 em listagens)
    List<UsuarioConteudo> findByConteudo_IdInOrderByIdxPosicaoAsc(Collection<UUID> idsConteudo);

    // publicacoes de um autor
    @Query("SELECT uc.conteudo FROM UsuarioConteudo uc WHERE uc.usuario.id = :idUsuario")
    List<Conteudo> findConteudosPorAutor(@Param("idUsuario") UUID idUsuario);

    void deleteByConteudo_Id(UUID id);
}
