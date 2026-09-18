package com.dtacomp.api.schema.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.api.schema.classes.ConteudoTexto;

public interface ConteudoTextRepository
    extends JpaRepository<ConteudoTexto, UUID> {}
