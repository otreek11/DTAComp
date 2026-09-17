package com.dtacomp.api.schema.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.api.schema.classes.ConteudoPDF;

public interface ConteudoPDFRepository 
    extends JpaRepository<ConteudoPDF, UUID> {}
