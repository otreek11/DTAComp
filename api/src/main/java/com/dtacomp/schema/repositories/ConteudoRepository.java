package com.dtacomp.schema.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.schema.classes.Conteudo;

public interface ConteudoRepository 
    extends JpaRepository<Conteudo, UUID> {}
