package com.dtacomp.api.schema.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.api.schema.classes.Tag;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNome(String nome);
    boolean existsByNome(String nome);
}
