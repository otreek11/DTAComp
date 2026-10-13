package com.dtacomp.api.schema.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dtacomp.api.schema.classes.Tag;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNome(String nome);
    boolean existsByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);

    @Query("SELECT COUNT(c) FROM Conteudo c JOIN c.tags t WHERE t.id = :idTag")
    long contarConteudosComTag(@Param("idTag") UUID idTag);

    Optional<Tag> findByNomeIgnoreCase(String limpo);
}
