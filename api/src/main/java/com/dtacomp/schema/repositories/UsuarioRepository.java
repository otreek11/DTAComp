package com.dtacomp.schema.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.schema.classes.Usuario;

public interface UsuarioRepository 
    extends JpaRepository<Usuario, UUID> {} 
