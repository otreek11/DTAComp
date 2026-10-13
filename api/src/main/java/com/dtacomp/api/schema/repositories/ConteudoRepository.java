package com.dtacomp.api.schema.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dtacomp.api.schema.classes.Conteudo;

import jakarta.persistence.LockModeType;

public interface ConteudoRepository 
    extends JpaRepository<Conteudo, UUID>, JpaSpecificationExecutor<Conteudo> {
    List<Conteudo> findByStatus(Conteudo.StatusConteudo status);
    List<Conteudo> findByTipo(Conteudo.TipoConteudo tipo);
    List<Conteudo> findByTituloContainingIgnoreCase(String titulo);

    @Query("SELECT c FROM Conteudo c JOIN c.tags t WHERE t.nome = :nomeTag")
    List<Conteudo> findByTagNome(@Param("nomeTag") String nomeTag);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Conteudo c WHERE c.id = :id")
    Optional<Conteudo> findByIdParaAtualizar(@Param("id") UUID id);
}
