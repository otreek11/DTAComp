package com.dtacomp.schema.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtacomp.schema.classes.Conteudo;
import com.dtacomp.schema.classes.Revisao;

public interface RevisaoRepository extends JpaRepository<Revisao, UUID> {
    List<Revisao> findByRevisor_Id(UUID idRevisor);
    List<Revisao> findByConteudo_Id(UUID idConteudo);
    List<Revisao> findByRevisor_IdAndStatus(UUID idRevisor, Conteudo.StatusConteudo status);
}
