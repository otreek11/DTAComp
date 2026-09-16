package com.dtacomp.schema.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.schema.classes.Tag;

public interface TagRepository
    extends JpaRepository<Tag, UUID>{}
