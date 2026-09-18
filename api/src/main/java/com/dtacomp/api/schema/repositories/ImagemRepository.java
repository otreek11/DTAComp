package com.dtacomp.api.schema.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.api.schema.classes.Imagem;

public interface ImagemRepository extends JpaRepository<Imagem, UUID> {
    List<Imagem> findByConteudoTexto_IdConteudo(UUID idConteudoTexto);
}
