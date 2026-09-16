package com.dtacomp.schema.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.schema.classes.UsuarioConteudo;
import com.dtacomp.schema.classes.UsuarioConteudo.UsuarioConteudoPK;

public interface UsuarioConteudoRepository
    extends JpaRepository<UsuarioConteudo, UsuarioConteudoPK>{}
