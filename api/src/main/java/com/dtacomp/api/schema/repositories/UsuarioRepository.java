package com.dtacomp.api.schema.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.Usuario.TipoPerfil;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByPerfil(Usuario.TipoPerfil perfil);
    List<Usuario> findByInstituicao_Id(UUID idInstituicao);
    Collection<Usuario> findByEmailIn(List<String> normalizados);
    long countByPerfil(TipoPerfil perfil);
} 
