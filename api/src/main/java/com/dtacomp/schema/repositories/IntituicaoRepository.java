package com.dtacomp.schema.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.schema.classes.Instituicao;

public interface IntituicaoRepository 
    extends JpaRepository<Instituicao, UUID> {}
