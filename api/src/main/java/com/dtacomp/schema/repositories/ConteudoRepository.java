package com.dtacomp.schema.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dtacomp.schema.classes.Conteudo;

public interface ConteudoRepository extends JpaRepository<Conteudo, UUID> {
    List<Conteudo> findByStatus(Conteudo.StatusConteudo status);
    List<Conteudo> findByTipo(Conteudo.TipoConteudo tipo);
    List<Conteudo> findByTituloContainingIgnoreCase(String titulo);

    @Query("SELECT c FROM Conteudo c JOIN c.tags t WHERE t.nome = :nomeTag")
    List<Conteudo> findByTagNome(@Param("nomeTag") String nomeTag);
}
